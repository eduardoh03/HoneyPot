package com.eduardo.HoneyPot.controller;

import com.eduardo.HoneyPot.model.AttackLog;
import com.eduardo.HoneyPot.service.HoneyPotService;
import com.eduardo.HoneyPot.repository.AttackLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/api/honeypot")
@Slf4j
@CrossOrigin(origins = "*")
public class HoneyPotController {
    
    @Autowired
    private HoneyPotService honeyPotService;
    
    @Autowired
    private AttackLogRepository attackLogRepository;
    
    // Controle da honeypot
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startHoneyPot() {
        try {
            honeyPotService.startHoneyPot();
            return ResponseEntity.ok(Map.of("status", "success", "message", "Honeypot iniciado com sucesso"));
        } catch (Exception e) {
            log.error("Erro ao iniciar honeypot: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "error", "message", "Erro ao iniciar honeypot: " + e.getMessage()));
        }
    }
    
    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopHoneyPot() {
        try {
            honeyPotService.stopHoneyPot();
            return ResponseEntity.ok(Map.of("status", "success", "message", "Honeypot parado com sucesso"));
        } catch (Exception e) {
            log.error("Erro ao parar honeypot: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "error", "message", "Erro ao parar honeypot: " + e.getMessage()));
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        boolean isRunning = honeyPotService.isRunning();
        return ResponseEntity.ok(Map.of(
            "running", isRunning,
            "status", isRunning ? "Ativo" : "Parado",
            "timestamp", LocalDateTime.now()
        ));
    }
    
    @GetMapping("/test-mongo")
    public ResponseEntity<Map<String, Object>> testMongoConnection() {
        try {
            // Testar conexão com MongoDB
            long count = attackLogRepository.count();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Conexão MongoDB OK",
                "totalLogs", count,
                "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Erro na conexão MongoDB: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Erro na conexão MongoDB: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    // Consulta de logs
    @GetMapping("/logs")
    public ResponseEntity<List<AttackLog>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        try {
            List<AttackLog> logs = attackLogRepository.findAll();
            if (logs.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            
            // Ordenar por timestamp (mais recente primeiro)
            logs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
            
            // Aplicar paginação
            int start = page * size;
            int end = Math.min(start + size, logs.size());
            
            if (start >= logs.size()) {
                return ResponseEntity.ok(List.of());
            }
            
            List<AttackLog> paginatedLogs = logs.subList(start, end);
            return ResponseEntity.ok(paginatedLogs);
            
        } catch (Exception e) {
            log.error("Erro ao buscar logs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/logs/ip/{ip}")
    public ResponseEntity<List<AttackLog>> getLogsByIp(@PathVariable String ip) {
        try {
            List<AttackLog> logs = attackLogRepository.findBySourceIpOrderByTimestampDesc(ip);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Erro ao buscar logs por IP {}: {}", ip, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/logs/protocol/{protocol}")
    public ResponseEntity<List<AttackLog>> getLogsByProtocol(@PathVariable String protocol) {
        try {
            List<AttackLog> logs = attackLogRepository.findByProtocolOrderByTimestampDesc(protocol.toUpperCase());
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
            List<AttackLog> logs = attackLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Erro ao buscar logs por período {} - {}: {}", start, end, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/logs/username/{username}")
    public ResponseEntity<List<AttackLog>> getLogsByUsername(@PathVariable String username) {
        try {
            List<AttackLog> logs = attackLogRepository.findByUsername(username);
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
            long totalLogs = attackLogRepository.count();
            long sshLogs = attackLogRepository.countByProtocol("SSH");
            long telnetLogs = attackLogRepository.countByProtocol("TELNET");
            
            return ResponseEntity.ok(Map.of(
                "totalLogs", totalLogs,
                "sshLogs", sshLogs,
                "telnetLogs", telnetLogs,
                "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Erro ao buscar estatísticas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/stats/top-ips")
    public ResponseEntity<Map<String, Object>> getTopIps(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<AttackLog> allLogs = attackLogRepository.findAllSourceIps();
            
            if (allLogs.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "message", "Nenhum ataque registrado ainda",
                    "topIps", List.of(),
                    "limit", limit,
                    "timestamp", LocalDateTime.now()
                ));
            }
            
            // Agrupar por IP e contar ocorrências
            Map<String, Long> ipCounts = allLogs.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    AttackLog::getSourceIp,
                    java.util.stream.Collectors.counting()
                ));
            
            // Ordenar por contagem e pegar os top N
            List<Map<String, Object>> topIps = new ArrayList<>();
            ipCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .forEach(entry -> {
                    LocalDateTime lastAttack = allLogs.stream()
                        .filter(log -> log.getSourceIp().equals(entry.getKey()))
                        .map(AttackLog::getTimestamp)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);
                    
                    Map<String, Object> ipInfo = new HashMap<>();
                    ipInfo.put("ip", entry.getKey());
                    ipInfo.put("count", entry.getValue());
                    ipInfo.put("lastAttack", lastAttack);
                    topIps.add(ipInfo);
                });
            
            return ResponseEntity.ok(Map.of(
                "message", "Top " + limit + " IPs mais ativos",
                "topIps", topIps,
                "limit", limit,
                "total", topIps.size(),
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("Erro ao buscar top IPs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Erro ao buscar top IPs: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    @GetMapping("/stats/top-credentials")
    public ResponseEntity<Map<String, Object>> getTopCredentials() {
        // Implementar lógica para top credenciais
        return ResponseEntity.ok(Map.of("message", "Funcionalidade em desenvolvimento"));
    }
    
    // Limpar logs (cuidado!)
    @DeleteMapping("/logs")
    public ResponseEntity<Map<String, String>> clearAllLogs() {
        try {
            attackLogRepository.deleteAll();
            return ResponseEntity.ok(Map.of("status", "success", "message", "Todos os logs foram removidos"));
        } catch (Exception e) {
            log.error("Erro ao limpar logs: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "error", "message", "Erro ao limpar logs: " + e.getMessage()));
        }
    }
}
