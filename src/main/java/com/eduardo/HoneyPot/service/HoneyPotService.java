package com.eduardo.HoneyPot.service;

import com.eduardo.HoneyPot.model.AttackLog;
import com.eduardo.HoneyPot.repository.AttackLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class HoneyPotService {
    
    @Autowired
    private AttackLogRepository attackLogRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Value("${honeypot.ssh.port}")
    private int sshPort;
    
    @Value("${honeypot.telnet.port}")
    private int telnetPort;
    
    @Value("${honeypot.ssh.banner}")
    private String sshBanner;
    
    @Value("${honeypot.telnet.banner}")
    private String telnetBanner;
    
    private ExecutorService executorService;
    private ServerSocket sshServer;
    private ServerSocket telnetServer;
    private boolean isRunning = false;
    
    public void startHoneyPot() {
        if (isRunning) {
            log.warn("Honeypot já está rodando!");
            return;
        }
        
        executorService = Executors.newCachedThreadPool();
        
        try {
            // Iniciar servidor SSH
            sshServer = new ServerSocket(sshPort);
            log.info("Servidor SSH honeypot iniciado na porta {}", sshPort);
            
            // Iniciar servidor Telnet
            telnetServer = new ServerSocket(telnetPort);
            log.info("Servidor Telnet honeypot iniciado na porta {}", telnetPort);
            
            isRunning = true;
            
            // Aceitar conexões SSH
            executorService.submit(() -> acceptSSHConnections());
            
            // Aceitar conexões Telnet
            executorService.submit(() -> acceptTelnetConnections());
            
        } catch (IOException e) {
            log.error("Erro ao iniciar honeypot: {}", e.getMessage());
            stopHoneyPot();
        }
    }
    
    public void stopHoneyPot() {
        isRunning = false;
        
        try {
            if (sshServer != null && !sshServer.isClosed()) {
                sshServer.close();
            }
            if (telnetServer != null && !telnetServer.isClosed()) {
                telnetServer.close();
            }
        } catch (IOException e) {
            log.error("Erro ao fechar servidores: {}", e.getMessage());
        }
        
        if (executorService != null) {
            executorService.shutdown();
        }
        
        // Criar notificação de sistema
        notificationService.createSystemNotification("INFO", "Honeypot Parado", 
            "Honeypot SSH/Telnet foi parado com sucesso");
        
        log.info("Honeypot parado");
    }
    
    private void acceptSSHConnections() {
        while (isRunning && !sshServer.isClosed()) {
            try {
                Socket clientSocket = sshServer.accept();
                String clientIp = clientSocket.getInetAddress().getHostAddress();
                log.info("Nova conexão SSH de: {}", clientIp);
                
                // Criar notificação de nova conexão
                notificationService.createAttackNotification("INFO", "Nova Conexão SSH", 
                    "Nova tentativa de conexão SSH detectada", clientIp, "SSH", null);
                
                executorService.submit(() -> handleSSHConnection(clientSocket));
                
            } catch (IOException e) {
                if (isRunning) {
                    log.error("Erro ao aceitar conexão SSH: {}", e.getMessage());
                }
            }
        }
    }
    
    private void acceptTelnetConnections() {
        while (isRunning && !telnetServer.isClosed()) {
            try {
                Socket clientSocket = telnetServer.accept();
                String clientIp = clientSocket.getInetAddress().getHostAddress();
                log.info("Nova conexão Telnet de: {}", clientIp);
                
                // Criar notificação de nova conexão
                notificationService.createAttackNotification("INFO", "Nova Conexão Telnet", 
                    "Nova tentativa de conexão Telnet detectada", clientIp, "TELNET", null);
                
                executorService.submit(() -> handleTelnetConnection(clientSocket));
                
            } catch (IOException e) {
                if (isRunning) {
                    log.error("Erro ao aceitar conexão Telnet: {}", e.getMessage());
                }
            }
        }
    }
    
    private void handleSSHConnection(Socket clientSocket) {
        String clientIp = clientSocket.getInetAddress().getHostAddress();
        AttackLog attackLog = new AttackLog(clientIp, sshPort, "SSH");
        attackLog.setBanner(sshBanner);
        
        attackLog.setUsername("conexao_ssh_" + System.currentTimeMillis());
        attackLog.setPassword("capturado");
        attackLog.setSuccessful(false);
        try {
            attackLogRepository.save(attackLog);
            log.info("SSH [{}]: Log inicial salvo com sucesso", clientIp);
            
            // Criar notificação de credenciais capturadas
            notificationService.createAttackNotification("SUCCESS", "Credenciais SSH Capturadas", 
                "Novas credenciais SSH foram capturadas e registradas", clientIp, "SSH", attackLog.getUsername());
        } catch (Exception e) {
            log.error("SSH [{}]: ERRO ao salvar log inicial: {}", clientIp, e.getMessage());
            
            // Criar notificação de erro
            notificationService.createAttackNotification("ERROR", "Erro ao Salvar Log SSH", 
                "Falha ao salvar log de ataque SSH: " + e.getMessage(), clientIp, "SSH", null);
        }
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            // Enviar banner SSH
            out.println(sshBanner);
            
            // Simular handshake SSH (simplificado)
            String line;
            boolean hasInteraction = false;
            
            while ((line = in.readLine()) != null && isRunning) {
                log.info("SSH [{}]: {}", clientIp, line);
                hasInteraction = true;
                
                // Simular resposta SSH para handshake
                if (line.contains("SSH")) {
                    out.println("SSH-2.0-OpenSSH_8.2p1 Ubuntu-4ubuntu0.5");
                }
                
                // Capturar qualquer tentativa de login (simplificado)
                if (line.trim().length() > 0 && !line.contains("SSH")) {
                    // Tentativa de usuário ou senha
                    if (attackLog.getUsername() == null) {
                        attackLog.setUsername(line.trim());
                        log.info("SSH [{}]: Tentativa de usuário: {}", clientIp, line.trim());
                    } else {
                        attackLog.setPassword(line.trim());
                        log.info("SSH [{}]: Tentativa de senha: {}", clientIp, line.trim());
                        attackLog.setSuccessful(false);
                        attackLogRepository.save(attackLog);
                        log.info("SSH [{}]: Log salvo no banco", clientIp);
                        break;
                    }
                }
            }
            
            if (attackLog.getPassword() == null) {
                if (attackLog.getUsername() == null) {
                    attackLog.setUsername("tentativa_conexao");
                    attackLog.setPassword("sem_dados");
                } else {
                    attackLog.setPassword("conexao_incompleta");
                }
                attackLog.setSuccessful(false);
                attackLogRepository.save(attackLog);
                log.info("SSH [{}]: Log de conexão SSH salvo - username: {}", clientIp, attackLog.getUsername());
            }
            
        } catch (IOException e) {
            log.error("Erro na conexão SSH com {}: {}", clientIp, e.getMessage());
            attackLog.setUsername("erro_conexao");
            attackLog.setPassword("erro: " + e.getMessage());
            attackLog.setSuccessful(false);
            attackLogRepository.save(attackLog);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                log.error("Erro ao fechar conexão SSH: {}", e.getMessage());
            }
        }
    }
    
    private void handleTelnetConnection(Socket clientSocket) {
        String clientIp = clientSocket.getInetAddress().getHostAddress();
        AttackLog attackLog = new AttackLog(clientIp, telnetPort, "TELNET");
        attackLog.setBanner(telnetBanner);
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            // Enviar banner Telnet
            out.println(telnetBanner);
            out.println("login: ");
            
            String line;
            while ((line = in.readLine()) != null && isRunning) {
                log.info("TELNET [{}]: {}", clientIp, line);
                
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // Simular login
                if (attackLog.getUsername() == null) {
                    attackLog.setUsername(line.trim());
                    out.println("Password: ");
                } else if (attackLog.getPassword() == null) {
                    attackLog.setPassword(line.trim());
                    attackLog.setSuccessful(false);
                    attackLogRepository.save(attackLog);
                    
                    // Simular shell fake realista
                    out.println("Welcome to Ubuntu 20.04.3 LTS (GNU/Linux 5.4.0-74-generic x86_64)");
                    out.println("Last login: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy")));
                    out.println("root@ubuntu-server:~# ");
                    
                    // Simular shell interativo
                    while ((line = in.readLine()) != null && isRunning) {
                        log.info("TELNET COMMAND [{}]: {}", clientIp, line);
                        
                        String command = line.trim();
                        if (command.isEmpty()) {
                            out.println("root@ubuntu-server:~# ");
                            continue;
                        }
                        
                        attackLog.addCommand(command);
                        attackLogRepository.save(attackLog);
                        
                        // Processar comando
                        String response = processFakeCommand(command, clientIp);
                        if (response != null) {
                            out.println(response);
                        }
                        
                        // Verificar se deve sair
                        if (command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("logout")) {
                            out.println("logout");
                            break;
                        }
                        
                        out.println("root@ubuntu-server:~# ");
                    }
                    break;
                }
            }
            
        } catch (IOException e) {
            log.error("Erro na conexão Telnet com {}: {}", clientIp, e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                log.error("Erro ao fechar conexão Telnet: {}", e.getMessage());
            }
        }
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Processa comandos fake de forma realista
     */
    private String processFakeCommand(String command, String clientIp) {
        String[] parts = command.split("\\s+");
        String cmd = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? java.util.Arrays.copyOfRange(parts, 1, parts.length) : new String[0];
        
        try {
            switch (cmd) {
                case "ls":
                    return processLsCommand(args);
                case "pwd":
                    return "/root";
                case "uname":
                    return processUnameCommand(args);
                case "whoami":
                    return "root";
                case "id":
                    return "uid=0(root) gid=0(root) groups=0(root)";
                case "ps":
                    return processPsCommand(args);
                case "top":
                    return processTopCommand();
                case "df":
                    return processDfCommand();
                case "free":
                    return processFreeCommand();
                case "cat":
                    return processCatCommand(args);
                case "head":
                    return processHeadCommand(args);
                case "tail":
                    return processTailCommand(args);
                case "grep":
                    return processGrepCommand(args);
                case "find":
                    return processFindCommand(args);
                case "netstat":
                    return processNetstatCommand(args);
                case "ss":
                    return processSsCommand(args);
                case "iptables":
                    return processIptablesCommand(args);
                case "systemctl":
                    return processSystemctlCommand(args);
                case "service":
                    return processServiceCommand(args);
                case "psql":
                    return "psql: could not connect to server: No such file or directory";
                case "mysql":
                    return "ERROR 2002 (HY000): Can't connect to local MySQL server through socket '/var/run/mysqld/mysqld.sock' (2)";
                case "wget":
                case "curl":
                    return processDownloadCommand(cmd, args);
                case "chmod":
                case "chown":
                    return "";
                case "mkdir":
                case "rmdir":
                    return "";
                case "touch":
                case "rm":
                    return "";
                case "cp":
                case "mv":
                    return "";
                case "echo":
                    return processEchoCommand(args);
                case "date":
                    return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy"));
                case "uptime":
                    return processUptimeCommand();
                case "w":
                    return processWCommand();
                case "last":
                    return processLastCommand();
                case "history":
                    return processHistoryCommand();
                case "clear":
                    return "\033[2J\033[H"; // ANSI clear screen
                case "exit":
                case "logout":
                    return null;
                default:
                    return "bash: " + cmd + ": command not found";
            }
        } catch (Exception e) {
            log.warn("Erro ao processar comando fake '{}' de {}: {}", command, clientIp, e.getMessage());
            return "bash: " + cmd + ": command not found";
        }
    }
    
    // Métodos auxiliares para comandos fake
    private String processLsCommand(String[] args) {
        StringBuilder output = new StringBuilder();
        
        // Simular diretórios e arquivos
        String[] files = {
            "anaconda-ks.cfg", "install.log", "install.log.syslog",
            "Desktop", "Documents", "Downloads", "Music", "Pictures", "Videos",
            ".bash_history", ".bash_profile", ".bashrc", ".ssh", ".cache"
        };
        
        if (args.length > 0 && args[0].equals("-la")) {
            output.append("total 72\n");
            output.append("dr-xr-x---. 2 root root 4096 Jan 15 10:30 .\n");
            output.append("dr-xr-xr-x. 3 root root 4096 Jan 15 10:30 ..\n");
            for (String file : files) {
                String permissions = file.startsWith(".") ? "drwxr-xr-x" : "-rw-r--r--";
                String owner = "root";
                String group = "root";
                String size = String.valueOf((int)(Math.random() * 1000) + 100);
                String date = "Jan 15 10:30";
                output.append(String.format("%s 1 %s %s %s %s %s\n", permissions, owner, group, size, date, file));
            }
        } else {
            for (String file : files) {
                output.append(file).append("  ");
            }
        }
        
        return output.toString();
    }
    
    private String processUnameCommand(String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "-a":
                    return "Linux ubuntu-server 5.4.0-74-generic #83-Ubuntu SMP Sat May 8 02:35:39 UTC 2021 x86_64 x86_64 x86_64 GNU/Linux";
                case "-r":
                    return "5.4.0-74-generic";
                case "-m":
                    return "x86_64";
                case "-s":
                    return "Linux";
                case "-n":
                    return "ubuntu-server";
                default:
                    return "Linux";
            }
        }
        return "Linux";
    }
    
    private String processPsCommand(String[] args) {
        StringBuilder output = new StringBuilder();
        output.append("  PID TTY          TIME CMD\n");
        output.append(" 1234 pts/0    00:00:00 bash\n");
        output.append(" 1235 pts/0    00:00:00 ps\n");
        output.append(" 1236 pts/0    00:00:00 telnetd\n");
        output.append(" 1237 pts/0    00:00:00 inetd\n");
        return output.toString();
    }
    
    private String processTopCommand() {
        StringBuilder output = new StringBuilder();
        output.append("top - 10:30:45 up 2 days, 15:23,  1 user,  load average: 0.52, 0.58, 0.59\n");
        output.append("Tasks: 123 total,   1 running, 122 sleeping,   0 stopped,   0 zombie\n");
        output.append("%Cpu(s):  2.3 us,  1.7 sy,  0.0 ni, 95.8 id,  0.2 wa,  0.0 hi,  0.0 si,  0.0 st\n");
        output.append("MiB Mem :   2048.0 total,    512.0 free,    768.0 used,    768.0 buff/cache\n");
        output.append("MiB Swap:   1024.0 total,   1024.0 free,      0.0 used.    768.0 avail Mem\n");
        output.append("\n");
        output.append("  PID USER      PR  NI    VIRT    RES    SHR S  %CPU  %MEM     TIME+ COMMAND\n");
        output.append(" 1234 root      20   0   12345   6789   1234 S   0.0   0.3   0:00.01 bash\n");
        output.append(" 1235 root      20   0   12345   6789   1234 S   0.0   0.3   0:00.00 ps\n");
        return output.toString();
    }
    
    private String processDfCommand() {
        StringBuilder output = new StringBuilder();
        output.append("Filesystem     1K-blocks    Used Available Use% Mounted on\n");
        output.append("/dev/sda1       20961280 1234567  19726713   6% /\n");
        output.append("tmpfs             1024000       0   1024000   0% /dev/shm\n");
        output.append("/dev/sda2       104857600 12345678 92511922  12% /home\n");
        return output.toString();
    }
    
    private String processFreeCommand() {
        StringBuilder output = new StringBuilder();
        output.append("              total        used        free      shared  buff/cache   available\n");
        output.append("Mem:        2097152      786432      524288       10240      786432     1310720\n");
        output.append("Swap:       1048576           0     1048576\n");
        return output.toString();
    }
    
    private String processCatCommand(String[] args) {
        if (args.length == 0) return "cat: missing file argument";
        
        String filename = args[0];
        if (filename.equals("/etc/passwd")) {
            return "root:x:0:0:root:/root:/bin/bash\nbin:x:1:1:bin:/bin:/sbin/nologin\ndaemon:x:2:2:daemon:/sbin:/sbin/nologin";
        } else if (filename.equals("/etc/hosts")) {
            return "127.0.0.1 localhost\n127.0.1.1 ubuntu-server\n::1 localhost ip6-localhost ip6-loopback";
        } else if (filename.equals("/proc/version")) {
            return "Linux version 5.4.0-74-generic (buildd@lgw01-amd64-038) (gcc version 9.4.0 (Ubuntu 9.4.0-1ubuntu1~20.04.1)) #83-Ubuntu SMP Sat May 8 02:35:39 UTC 2021";
        } else {
            return "cat: " + filename + ": No such file or directory";
        }
    }
    
    private String processHeadCommand(String[] args) {
        if (args.length == 0) return "head: missing operand";
        return processCatCommand(args); // Simples para o fake
    }
    
    private String processTailCommand(String[] args) {
        if (args.length == 0) return "tail: missing operand";
        return processCatCommand(args); // Simples para o fake
    }
    
    private String processGrepCommand(String[] args) {
        if (args.length < 2) return "grep: missing operand";
        return "grep: " + args[1] + ": No such file or directory";
    }
    
    private String processFindCommand(String[] args) {
        if (args.length == 0) return "find: missing path";
        return ""; // Retorna vazio para simular busca sem resultados
    }
    
    private String processNetstatCommand(String[] args) {
        StringBuilder output = new StringBuilder();
        output.append("Active Internet connections (w/o servers)\n");
        output.append("Proto Recv-Q Send-Q Local Address           Foreign Address         State\n");
        output.append("tcp        0      0 192.168.1.100:22        192.168.1.50:12345     ESTABLISHED\n");
        output.append("tcp        0      0 192.168.1.100:80        192.168.1.50:54321     ESTABLISHED\n");
        return output.toString();
    }
    
    private String processSsCommand(String[] args) {
        return processNetstatCommand(args); // Similar ao netstat
    }
    
    private String processIptablesCommand(String[] args) {
        if (args.length > 0 && args[0].equals("-L")) {
            StringBuilder output = new StringBuilder();
            output.append("Chain INPUT (policy ACCEPT)\n");
            output.append("target     prot opt source               destination\n");
            output.append("ACCEPT     all  --  0.0.0.0/0            0.0.0.0/0\n");
            output.append("Chain FORWARD (policy ACCEPT)\n");
            output.append("target     prot opt source               destination\n");
            output.append("Chain OUTPUT (policy ACCEPT)\n");
            output.append("target     prot opt source               destination\n");
            return output.toString();
        }
        return "";
    }
    
    private String processSystemctlCommand(String[] args) {
        if (args.length > 0 && args[0].equals("status")) {
            return "● systemd\n   Loaded: loaded (/lib/systemd/system/systemd; vendor preset: enabled)\n   Active: active (running) since Mon 2021-01-15 10:30:00 UTC; 2 days ago";
        }
        return "";
    }
    
    private String processServiceCommand(String[] args) {
        if (args.length > 0 && args[0].equals("--status-all")) {
            StringBuilder output = new StringBuilder();
            output.append(" [ + ]  acpid\n");
            output.append(" [ + ]  apache2\n");
            output.append(" [ + ]  cron\n");
            output.append(" [ + ]  dbus\n");
            output.append(" [ + ]  ssh\n");
            output.append(" [ - ]  telnet\n");
            return output.toString();
        }
        return "";
    }
    
    private String processDownloadCommand(String cmd, String[] args) {
        if (args.length == 0) return cmd + ": missing URL";
        return cmd + ": " + args[0] + ": Connection refused";
    }
    
    private String processEchoCommand(String[] args) {
        if (args.length == 0) return "";
        return String.join(" ", args);
    }
    
    private String processUptimeCommand() {
        return " 10:30:45 up 2 days, 15:23,  1 user,  load average: 0.52, 0.58, 0.59";
    }
    
    private String processWCommand() {
        StringBuilder output = new StringBuilder();
        output.append(" 10:30:45 up 2 days, 15:23,  1 user,  load average: 0.52, 0.58, 0.59\n");
        output.append("USER     TTY      FROM             LOGIN@   IDLE   JCPU   PCPU WHAT\n");
        output.append("root     pts/0    192.168.1.50     Mon10   15:23m  0.01s  0.01s -bash\n");
        return output.toString();
    }
    
    private String processLastCommand() {
        StringBuilder output = new StringBuilder();
        output.append("root     pts/0    192.168.1.50     Mon Jan 15 10:30   still logged in\n");
        output.append("root     pts/0    192.168.1.50     Mon Jan 15 09:15 - 10:30  (01:15)\n");
        output.append("reboot   system boot  5.4.0-74-generic Mon Jan 15 09:00 - 10:30  (01:30)\n");
        return output.toString();
    }
    
    private String processHistoryCommand() {
        StringBuilder output = new StringBuilder();
        output.append("    1  ls\n");
        output.append("    2  pwd\n");
        output.append("    3  whoami\n");
        output.append("    4  ps aux\n");
        output.append("    5  netstat -tuln\n");
        output.append("    6  cat /etc/passwd\n");
        output.append("    7  history\n");
        return output.toString();
    }
}
