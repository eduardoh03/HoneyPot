package com.eduardo.HoneyPot.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandExecution {
    
    private LocalDateTime timestamp;
    private String command;
    
    public CommandExecution(String command) {
        this.timestamp = LocalDateTime.now();
        this.command = command;
    }
}
