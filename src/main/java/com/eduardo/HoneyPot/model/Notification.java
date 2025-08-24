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
@Document(collection = "notifications")
public class Notification {
    
    @Id
    private String id;
    
    @Indexed
    private LocalDateTime timestamp;
    
    @Indexed
    private String type; // INFO, WARNING, ERROR, SUCCESS, ALERT
    
    @Indexed
    private String category; // SYSTEM, SECURITY, ATTACK, PERFORMANCE
    
    private String title;
    
    private String message;
    
    private String details;
    
    @Indexed
    private boolean read = false;
    
    @Indexed
    private String sourceIp;
    
    private String protocol;
    
    private String username;
    
    private int priority; // 1=Baixa, 2=Média, 3=Alta, 4=Crítica
    
    private String actionUrl;
    
    private boolean actionable = false;
    
    public Notification(String type, String category, String title, String message) {
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.category = category;
        this.title = title;
        this.message = message;
        this.priority = getDefaultPriority(type);
    }
    
    public Notification(String type, String category, String title, String message, String sourceIp, String protocol) {
        this(type, category, title, message);
        this.sourceIp = sourceIp;
        this.protocol = protocol;
        this.actionable = true;
    }
    
    private int getDefaultPriority(String type) {
        switch (type.toUpperCase()) {
            case "ERROR":
            case "ALERT":
                return 4; // Crítica
            case "WARNING":
                return 3; // Alta
            case "SUCCESS":
                return 1; // Baixa
            default:
                return 2; // Média
        }
    }
    
    public boolean isHighPriority() {
        return priority >= 3;
    }
    
    public boolean isCritical() {
        return priority == 4;
    }
}
