package com.eduardo.HoneyPot.controller;

import com.eduardo.HoneyPot.model.AttackLog;
import com.eduardo.HoneyPot.service.StatisticsService;
import com.eduardo.HoneyPot.service.LogService;
import com.eduardo.HoneyPot.service.ManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/honeypot")
@Slf4j
@CrossOrigin(origins = "*")
public class HoneyPotController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    @Autowired
    private LogService logService;
    
    @Autowired
    private ManagementService managementService;
    
    // Controle da honeypot
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startHoneyPot() {
        try {
            Map<String, String> result = managementService.startHoneyPot();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erro ao iniciar honeypot: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
    
    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopHoneyPot() {
        try {
            Map<String, String> result = managementService.stopHoneyPot();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erro ao parar honeypot: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        try {
            Map<String, Object> status = managementService.getStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Erro ao buscar status: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/restart")
    public ResponseEntity<Map<String, String>> restartHoneyPot() {
        try {
            Map<String, String> result = managementService.restartHoneyPot();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erro ao reiniciar honeypot: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        try {
            Map<String, Object> health = managementService.getHealthInfo();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Erro ao buscar informações de saúde: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Consulta de logs
    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        try {
            Map<String, Object> result = logService.getAllLogs(page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erro ao buscar logs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/logs/ip/{ip}")
    public ResponseEntity<List<AttackLog>> getLogsByIp(@PathVariable String ip) {
        try {
            List<AttackLog> logs = logService.getLogsByIp(ip);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Erro ao buscar logs por IP {}: {}", ip, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/logs/protocol/{protocol}")
    public ResponseEntity<List<AttackLog>> getLogsByProtocol(@PathVariable String protocol) {
        try {
            List<AttackLog> logs = logService.getLogsByProtocol(protocol);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Erro ao buscar logs por protocolo {}: {}", protocol, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/logs/date-range")
    public ResponseEntity<List<AttackLog>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        try {
            List<AttackLog> logs = logService.getLogsByDateRange(start, end);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Erro ao buscar logs por período {} - {}: {}", start, end, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/logs/username/{username}")
    public ResponseEntity<List<AttackLog>> getLogsByUsername(@PathVariable String username) {
        try {
            List<AttackLog> logs = logService.getLogsByUsername(username);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Erro ao buscar logs por usuário {}: {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Estatísticas
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = statisticsService.getGeneralStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erro ao buscar estatísticas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/stats/top-ips")
    public ResponseEntity<Map<String, Object>> getTopIps(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Map<String, Object> topIps = statisticsService.getTopAttackingIps(limit);
            return ResponseEntity.ok(topIps);
        } catch (Exception e) {
            log.error("Erro ao buscar top IPs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Erro ao buscar top IPs: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    @GetMapping("/stats/top-credentials")
    public ResponseEntity<Map<String, Object>> getTopCredentials(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Map<String, Object> topCredentials = statisticsService.getTopCredentials(limit);
            return ResponseEntity.ok(topCredentials);
        } catch (Exception e) {
            log.error("Erro ao buscar top credenciais: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Erro ao buscar top credenciais: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    // Limpar logs (cuidado!)
    @DeleteMapping("/logs")
    public ResponseEntity<Map<String, String>> clearAllLogs() {
        try {
            Map<String, String> result = logService.clearAllLogs();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erro ao limpar logs: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
