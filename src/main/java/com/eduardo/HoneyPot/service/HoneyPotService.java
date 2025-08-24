package com.eduardo.HoneyPot.service;

import com.eduardo.HoneyPot.model.AttackLog;
import com.eduardo.HoneyPot.repository.AttackLogRepository;
import com.eduardo.HoneyPot.service.NotificationService;
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
                    
                    // Simular shell fake
                    out.println("Welcome to Ubuntu 20.04.3 LTS (GNU/Linux 5.4.0-74-generic x86_64)");
                    out.println("Last login: " + java.time.LocalDateTime.now());
                    out.println("$ ");
                    
                    // Simular comandos
                    while ((line = in.readLine()) != null && isRunning) {
                        log.info("TELNET COMMAND [{}]: {}", clientIp, line);
                        
                        String command = line.trim();
                        attackLog.addCommand(command);
                        attackLogRepository.save(attackLog);
                        
                        switch (command.toLowerCase()) {
                            case "ls":
                                out.println("bin  boot  dev  etc  home  lib  media  mnt  opt  proc  root  run  sbin  srv  sys  tmp  usr  var");
                                break;
                            case "pwd":
                                out.println("/root");
                                break;
                            case "uname":
                                out.println("Linux");
                                break;
                            case "echo":
                                out.println("echo");
                                break;
                            case "exit":
                            case "logout":
                                return;
                            default:
                                out.println("bash: " + command + ": command not found");
                        }
                        out.println("$ ");
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
}
