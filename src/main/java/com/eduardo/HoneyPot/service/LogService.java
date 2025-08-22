package com.eduardo.HoneyPot.service;

import com.eduardo.HoneyPot.model.AttackLog;
import com.eduardo.HoneyPot.repository.AttackLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {
    
    private final AttackLogRepository attackLogRepository;
    
    /**
     * Busca todos os logs com paginação
     */
    public Map<String, Object> getAllLogs(int page, int size) {
        try {
            List<AttackLog> allLogs = attackLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
            
            if (allLogs.isEmpty()) {
                return Map.of(
                    "logs", List.of(),
                    "totalPages", 0,
                    "totalElements", 0L,
                    "currentPage", page,
                    "size", size,
                    "timestamp", LocalDateTime.now()
                );
            }
            
            // Implementar paginação manual
            int start = page * size;
            int end = Math.min(start + size, allLogs.size());
            List<AttackLog> pageContent = allLogs.subList(start, end);
            
            int totalPages = (int) Math.ceil((double) allLogs.size() / size);
            
            return Map.of(
                "logs", pageContent,
                "totalPages", totalPages,
                "totalElements", (long) allLogs.size(),
                "currentPage", page,
                "size", size,
                "timestamp", LocalDateTime.now()
            );
            
        } catch (Exception e) {
            log.error("Erro ao buscar todos os logs: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar logs", e);
        }
    }
    
    /**
     * Busca logs por IP
     */
    public List<AttackLog> getLogsByIp(String ip) {
        try {
            return attackLogRepository.findBySourceIpOrderByTimestampDesc(ip);
        } catch (Exception e) {
            log.error("Erro ao buscar logs por IP {}: {}", ip, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar logs por IP: " + ip, e);
        }
    }
    
    /**
     * Busca logs por protocolo
     */
    public List<AttackLog> getLogsByProtocol(String protocol) {
        try {
            return attackLogRepository.findByProtocolOrderByTimestampDesc(protocol.toUpperCase());
        } catch (Exception e) {
            log.error("Erro ao buscar logs por protocolo {}: {}", protocol, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar logs por protocolo: " + protocol, e);
        }
    }
    
    /**
     * Busca logs por período
     */
    public List<AttackLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        try {
            return attackLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
        } catch (Exception e) {
            log.error("Erro ao buscar logs por período {} - {}: {}", start, end, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar logs por período", e);
        }
    }
    
    /**
     * Busca logs por username
     */
    public List<AttackLog> getLogsByUsername(String username) {
        try {
            return attackLogRepository.findByUsername(username);
        } catch (Exception e) {
            log.error("Erro ao buscar logs por usuário {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar logs por usuário: " + username, e);
        }
    }
    
    /**
     * Remove todos os logs
     */
    public Map<String, String> clearAllLogs() {
        try {
            long count = attackLogRepository.count();
            attackLogRepository.deleteAll();
            log.info("Todos os {} logs foram removidos", count);
            
            return Map.of(
                "status", "success", 
                "message", "Todos os " + count + " logs foram removidos",
                "deletedCount", String.valueOf(count)
            );
        } catch (Exception e) {
            log.error("Erro ao limpar logs: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao limpar logs", e);
        }
    }
    
    /**
     * Salva um novo log de ataque
     */
    public AttackLog saveAttackLog(AttackLog attackLog) {
        try {
            if (attackLog.getTimestamp() == null) {
                attackLog.setTimestamp(LocalDateTime.now());
            }
            
            AttackLog savedLog = attackLogRepository.save(attackLog);
            log.debug("Log de ataque salvo: IP={}, Protocolo={}, Usuário={}", 
                     savedLog.getSourceIp(), savedLog.getProtocol(), savedLog.getUsername());
            
            return savedLog;
        } catch (Exception e) {
            log.error("Erro ao salvar log de ataque: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar log de ataque", e);
        }
    }
    
    /**
     * Conta logs por IP
     */
    public long countLogsByIp(String ip) {
        try {
            return attackLogRepository.countBySourceIp(ip);
        } catch (Exception e) {
            log.error("Erro ao contar logs por IP {}: {}", ip, e.getMessage(), e);
            throw new RuntimeException("Erro ao contar logs por IP: " + ip, e);
        }
    }
    
    /**
     * Conta logs por protocolo
     */
    public long countLogsByProtocol(String protocol) {
        try {
            return attackLogRepository.countByProtocol(protocol);
        } catch (Exception e) {
            log.error("Erro ao contar logs por protocolo {}: {}", protocol, e.getMessage(), e);
            throw new RuntimeException("Erro ao contar logs por protocolo: " + protocol, e);
        }
    }
    
    /**
     * Verifica se existem logs no sistema
     */
    public boolean hasLogs() {
        try {
            return attackLogRepository.count() > 0;
        } catch (Exception e) {
            log.error("Erro ao verificar existência de logs: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Busca logs mais recentes
     */
    public List<AttackLog> getRecentLogs(int limit) {
        try {
            List<AttackLog> allLogs = attackLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
            return allLogs.stream().limit(limit).toList();
        } catch (Exception e) {
            log.error("Erro ao buscar logs recentes: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar logs recentes", e);
        }
    }
}
