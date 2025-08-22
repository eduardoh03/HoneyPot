package com.eduardo.HoneyPot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagementService {
    
    private final HoneyPotService honeyPotService;
    
    /**
     * Inicia a honeypot
     */
    public Map<String, String> startHoneyPot() {
        try {
            if (honeyPotService.isRunning()) {
                return Map.of(
                    "status", "warning",
                    "message", "Honeypot já está em execução",
                    "timestamp", LocalDateTime.now().toString()
                );
            }
            
            honeyPotService.startHoneyPot();
            log.info("Honeypot iniciada via API");
            
            return Map.of(
                "status", "success",
                "message", "Honeypot iniciada com sucesso",
                "timestamp", LocalDateTime.now().toString()
            );
            
        } catch (Exception e) {
            log.error("Erro ao iniciar honeypot via API: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao iniciar honeypot: " + e.getMessage(), e);
        }
    }
    
    /**
     * Para a honeypot
     */
    public Map<String, String> stopHoneyPot() {
        try {
            if (!honeyPotService.isRunning()) {
                return Map.of(
                    "status", "warning",
                    "message", "Honeypot já está parada",
                    "timestamp", LocalDateTime.now().toString()
                );
            }
            
            honeyPotService.stopHoneyPot();
            log.info("Honeypot parada via API");
            
            return Map.of(
                "status", "success",
                "message", "Honeypot parada com sucesso",
                "timestamp", LocalDateTime.now().toString()
            );
            
        } catch (Exception e) {
            log.error("Erro ao parar honeypot via API: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao parar honeypot: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifica o status da honeypot
     */
    public Map<String, Object> getStatus() {
        try {
            boolean isRunning = honeyPotService.isRunning();
            String status = isRunning ? "RUNNING" : "STOPPED";
            String message = isRunning ? "Honeypot está ativa e capturando conexões" : "Honeypot está parada";
            
            return Map.of(
                "status", status,
                "running", isRunning,
                "message", message,
                "timestamp", LocalDateTime.now(),
                "services", Map.of(
                    "ssh", Map.of(
                        "active", isRunning,
                        "description", "SSH Honeypot Service"
                    ),
                    "telnet", Map.of(
                        "active", isRunning,
                        "description", "Telnet Honeypot Service"
                    )
                )
            );
            
        } catch (Exception e) {
            log.error("Erro ao verificar status da honeypot: {}", e.getMessage(), e);
            
            return Map.of(
                "status", "ERROR",
                "running", false,
                "message", "Erro ao verificar status: " + e.getMessage(),
                "timestamp", LocalDateTime.now(),
                "error", e.getMessage()
            );
        }
    }
    
    /**
     * Reinicia a honeypot
     */
    public Map<String, String> restartHoneyPot() {
        try {
            log.info("Reiniciando honeypot via API");
            
            // Para a honeypot se estiver rodando
            if (honeyPotService.isRunning()) {
                honeyPotService.stopHoneyPot();
                Thread.sleep(2000); // Aguarda 2 segundos para garantir parada completa
            }
            
            // Inicia novamente
            honeyPotService.startHoneyPot();
            
            return Map.of(
                "status", "success",
                "message", "Honeypot reiniciada com sucesso",
                "timestamp", LocalDateTime.now().toString()
            );
            
        } catch (Exception e) {
            log.error("Erro ao reiniciar honeypot via API: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao reiniciar honeypot: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtém informações de saúde do sistema
     */
    public Map<String, Object> getHealthInfo() {
        try {
            boolean isRunning = honeyPotService.isRunning();
            
            return Map.of(
                "status", isRunning ? "UP" : "DOWN",
                "honeypot", Map.of(
                    "status", isRunning ? "ACTIVE" : "INACTIVE",
                    "uptime", "N/A", // Poderia implementar controle de uptime
                    "lastStarted", "N/A" // Poderia implementar timestamp de início
                ),
                "system", Map.of(
                    "timestamp", LocalDateTime.now(),
                    "java", Map.of(
                        "version", System.getProperty("java.version"),
                        "vendor", System.getProperty("java.vendor")
                    ),
                    "memory", Map.of(
                        "total", Runtime.getRuntime().totalMemory(),
                        "free", Runtime.getRuntime().freeMemory(),
                        "used", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                    )
                )
            );
            
        } catch (Exception e) {
            log.error("Erro ao buscar informações de saúde: {}", e.getMessage(), e);
            
            return Map.of(
                "status", "ERROR",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            );
        }
    }
}
