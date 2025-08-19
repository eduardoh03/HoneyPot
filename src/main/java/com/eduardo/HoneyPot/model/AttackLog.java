package com.eduardo.HoneyPot.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

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
    
    private String command;
    
    private String sessionId;
    
    private String banner;
    
    private boolean successful;
    
    public AttackLog(String sourceIp, int port, String protocol) {
        this.timestamp = LocalDateTime.now();
        this.sourceIp = sourceIp;
        this.port = port;
        this.protocol = protocol;
        this.sessionId = java.util.UUID.randomUUID().toString();
    }
}
