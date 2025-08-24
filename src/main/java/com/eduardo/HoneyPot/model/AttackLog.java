package com.eduardo.HoneyPot.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "attack_logs")
public class AttackLog {
    
    @Id
    private String id;
    
    @Indexed
    private LocalDateTime timestamp;
    
    @Indexed
    private String sourceIp;
    
    private int port;
    
    private String protocol; // SSH ou TELNET
    
    private String username;
    
    private String password;
    
    private List<CommandExecution> commands = new ArrayList<>();
    
    private String sessionId;
    
    private String banner;
    
    private boolean successful;
    
    public AttackLog(String sourceIp, int port, String protocol) {
        this.timestamp = LocalDateTime.now();
        this.sourceIp = sourceIp;
        this.port = port;
        this.protocol = protocol;
        this.sessionId = java.util.UUID.randomUUID().toString();
        this.commands = new ArrayList<>();
    }
    
    /**
     * Adiciona um novo comando à lista de comandos executados
     */
    public void addCommand(String command) {
        this.commands.add(new CommandExecution(command));
    }
    
    /**
     * Retorna o comando mais recente executado
     */
    public String getLatestCommand() {
        if (commands == null || commands.isEmpty()) {
            return null;
        }
        return commands.get(commands.size() - 1).getCommand();
    }
    
    /**
     * Retorna o número total de comandos executados
     */
    public int getCommandCount() {
        return commands != null ? commands.size() : 0;
    }
}
