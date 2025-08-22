package com.eduardo.HoneyPot.service;

import com.eduardo.HoneyPot.model.AttackLog;
import com.eduardo.HoneyPot.repository.AttackLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    
    private final AttackLogRepository attackLogRepository;
    
    /**
     * Busca estatísticas gerais da honeypot
     */
    public Map<String, Object> getGeneralStats() {
        try {
            long totalLogs = attackLogRepository.count();
            long sshLogs = attackLogRepository.countByProtocol("SSH");
            long telnetLogs = attackLogRepository.countByProtocol("TELNET");
            
            return Map.of(
                "totalLogs", totalLogs,
                "sshLogs", sshLogs,
                "telnetLogs", telnetLogs,
                "timestamp", LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("Erro ao buscar estatísticas gerais: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar estatísticas gerais", e);
        }
    }
    
    /**
     * Busca os top IPs atacantes
     */
    public Map<String, Object> getTopAttackingIps(int limit) {
        try {
            List<AttackLog> allLogs = attackLogRepository.findAllSourceIps();
            
            if (allLogs.isEmpty()) {
                return Map.of(
                    "message", "Nenhum ataque registrado ainda",
                    "topIps", List.of(),
                    "limit", limit,
                    "timestamp", LocalDateTime.now()
                );
            }
            
            // Agrupar por IP e contar ocorrências
            Map<String, Long> ipCounts = allLogs.stream()
                .collect(Collectors.groupingBy(
                    AttackLog::getSourceIp,
                    Collectors.counting()
                ));
            
            // Criar lista de informações dos IPs
            List<Map<String, Object>> topIps = new ArrayList<>();
            ipCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .forEach(entry -> {
                    LocalDateTime lastAttack = findLastAttackByIp(allLogs, entry.getKey());
                    
                    Map<String, Object> ipInfo = new HashMap<>();
                    ipInfo.put("ip", entry.getKey());
                    ipInfo.put("count", entry.getValue());
                    ipInfo.put("lastAttack", lastAttack);
                    topIps.add(ipInfo);
                });
            
            return Map.of(
                "message", "Top " + limit + " IPs mais ativos",
                "topIps", topIps,
                "limit", limit,
                "total", topIps.size(),
                "timestamp", LocalDateTime.now()
            );
            
        } catch (Exception e) {
            log.error("Erro ao buscar top IPs: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar top IPs atacantes", e);
        }
    }
    
    /**
     * Busca as top credenciais tentadas
     */
    public Map<String, Object> getTopCredentials(int limit) {
        try {
            List<AttackLog> allLogs = attackLogRepository.findAll();
            
            if (allLogs.isEmpty()) {
                return Map.of(
                    "message", "Nenhuma credencial registrada ainda",
                    "topCredentials", List.of(),
                    "limit", limit,
                    "timestamp", LocalDateTime.now()
                );
            }
            
            // Agrupar por combinação username/password
            Map<String, Long> credentialCounts = allLogs.stream()
                .filter(log -> log.getUsername() != null && log.getPassword() != null)
                .collect(Collectors.groupingBy(
                    log -> log.getUsername() + ":" + log.getPassword(),
                    Collectors.counting()
                ));
            
            // Criar lista de informações das credenciais
            List<Map<String, Object>> topCredentials = new ArrayList<>();
            credentialCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .forEach(entry -> {
                    String[] parts = entry.getKey().split(":", 2);
                    String username = parts[0];
                    String password = parts.length > 1 ? parts[1] : "";
                    
                    Map<String, Object> credInfo = new HashMap<>();
                    credInfo.put("username", username);
                    credInfo.put("password", password);
                    credInfo.put("count", entry.getValue());
                    topCredentials.add(credInfo);
                });
            
            return Map.of(
                "message", "Top " + limit + " credenciais mais tentadas",
                "topCredentials", topCredentials,
                "limit", limit,
                "total", topCredentials.size(),
                "timestamp", LocalDateTime.now()
            );
            
        } catch (Exception e) {
            log.error("Erro ao buscar top credenciais: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar top credenciais", e);
        }
    }
    
    /**
     * Método auxiliar para encontrar o último ataque de um IP específico
     */
    private LocalDateTime findLastAttackByIp(List<AttackLog> logs, String ip) {
        return logs.stream()
            .filter(log -> log.getSourceIp().equals(ip))
            .map(AttackLog::getTimestamp)
            .max(LocalDateTime::compareTo)
            .orElse(null);
    }
    
    /**
     * Busca estatísticas por protocolo
     */
    public Map<String, Object> getStatsByProtocol() {
        try {
            long sshCount = attackLogRepository.countByProtocol("SSH");
            long telnetCount = attackLogRepository.countByProtocol("TELNET");
            long totalCount = attackLogRepository.count();
            
            double sshPercentage = totalCount > 0 ? (double) sshCount / totalCount * 100 : 0;
            double telnetPercentage = totalCount > 0 ? (double) telnetCount / totalCount * 100 : 0;
            
            return Map.of(
                "protocols", Map.of(
                    "SSH", Map.of(
                        "count", sshCount,
                        "percentage", Math.round(sshPercentage * 100.0) / 100.0
                    ),
                    "TELNET", Map.of(
                        "count", telnetCount,
                        "percentage", Math.round(telnetPercentage * 100.0) / 100.0
                    )
                ),
                "total", totalCount,
                "timestamp", LocalDateTime.now()
            );
            
        } catch (Exception e) {
            log.error("Erro ao buscar estatísticas por protocolo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar estatísticas por protocolo", e);
        }
    }
    
    /**
     * Busca estatísticas de ataques por hora nas últimas 24 horas
     */
    public Map<String, Object> getTimelineStats() {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            
            // Buscar todos os logs das últimas 24 horas
            List<AttackLog> recentLogs = attackLogRepository.findByTimestampBetweenOrderByTimestampAsc(startTime, endTime);
            
            // Criar array de 24 horas
            List<String> hours = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();
            
            for (int i = 23; i >= 0; i--) {
                LocalDateTime hourTime = endTime.minusHours(i);
                String hourLabel = String.format("%02d:00", hourTime.getHour());
                hours.add(hourLabel);
                
                // Contar ataques nesta hora específica
                LocalDateTime hourStart = hourTime.withMinute(0).withSecond(0).withNano(0);
                LocalDateTime hourEnd = hourStart.plusHours(1);
                
                long attacksInHour = recentLogs.stream()
                    .filter(log -> {
                        LocalDateTime logTime = log.getTimestamp();
                        return !logTime.isBefore(hourStart) && logTime.isBefore(hourEnd);
                    })
                    .count();
                
                counts.add((int) attacksInHour);
            }
            
            return Map.of(
                "hours", hours,
                "counts", counts,
                "totalAttacks", recentLogs.size(),
                "period", "Últimas 24 horas",
                "startTime", startTime,
                "endTime", endTime,
                "timestamp", LocalDateTime.now()
            );
            
        } catch (Exception e) {
            log.error("Erro ao buscar estatísticas de timeline: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar estatísticas de timeline", e);
        }
    }
}
