package com.eduardo.HoneyPot.controller;

import com.eduardo.HoneyPot.model.AttackLog;
import com.eduardo.HoneyPot.service.StatisticsService;
import com.eduardo.HoneyPot.service.LogService;
import com.eduardo.HoneyPot.service.ManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "HoneyPot API", description = "API para gerenciamento e monitoramento da honeypot SSH/Telnet")
public class HoneyPotController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    @Autowired
    private LogService logService;
    
    @Autowired
    private ManagementService managementService;
    
    // Controle da honeypot
    @Operation(
        summary = "Iniciar Honeypot",
        description = "Inicia a honeypot SSH/Telnet para capturar tentativas de ataque",
        tags = {"Controle"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Honeypot iniciada com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Sucesso",
                    value = """
                        {
                          "status": "success",
                          "message": "Honeypot iniciada com sucesso",
                          "timestamp": "2025-08-22T07:53:07.021874654"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Erro",
                    value = """
                        {
                          "status": "error",
                          "message": "Erro ao iniciar honeypot: Porta já em uso"
                        }
                        """
                )
            )
        )
    })
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
    
    @Operation(
        summary = "Parar Honeypot",
        description = "Para a honeypot SSH/Telnet, interrompendo a captura de ataques",
        tags = {"Controle"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Honeypot parada com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Sucesso",
                    value = """
                        {
                          "status": "success",
                          "message": "Honeypot parada com sucesso",
                          "timestamp": "2025-08-22T07:53:07.021874654"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Erro",
                    value = """
                        {
                          "status": "error",
                          "message": "Erro ao parar honeypot: Serviço não está rodando"
                        }
                        """
                )
            )
        )
    })
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
    
    @Operation(
        summary = "Status da Honeypot",
        description = "Retorna o status atual da honeypot, incluindo informações sobre os serviços SSH e Telnet",
        tags = {"Controle"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Status da honeypot",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Honeypot Ativa",
                    value = """
                        {
                          "status": "RUNNING",
                          "running": true,
                          "message": "Honeypot está ativa e capturando conexões",
                          "timestamp": "2025-08-22T07:53:07.021874654",
                          "services": {
                            "ssh": {
                              "active": true,
                              "description": "SSH Honeypot Service"
                            },
                            "telnet": {
                              "active": true,
                              "description": "Telnet Honeypot Service"
                            }
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor"
        )
    })
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
    
    @Operation(
        summary = "Reiniciar Honeypot",
        description = "Para e reinicia a honeypot SSH/Telnet, útil para aplicar novas configurações",
        tags = {"Controle"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Honeypot reiniciada com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Sucesso",
                    value = """
                        {
                          "status": "success",
                          "message": "Honeypot reiniciada com sucesso",
                          "timestamp": "2025-08-22T07:53:07.021874654"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Erro",
                    value = """
                        {
                          "status": "error",
                          "message": "Erro ao reiniciar honeypot: Timeout na parada"
                        }
                        """
                )
            )
        )
    })
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
    
    @Operation(
        summary = "Health Check",
        description = "Retorna informações de saúde do sistema, incluindo métricas de memória e Java",
        tags = {"Monitoramento"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Informações de saúde do sistema",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Sistema Saudável",
                    value = """
                        {
                          "status": "UP",
                          "honeypot": {
                            "status": "ACTIVE",
                            "lastStarted": "N/A",
                            "uptime": "N/A"
                          },
                          "system": {
                            "timestamp": "2025-08-22T07:53:11.306302238",
                            "java": {
                              "version": "24.0.2",
                              "vendor": "Oracle Corporation"
                            },
                            "memory": {
                              "total": 73400320,
                              "free": 37372288,
                              "used": 36028032
                            }
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor"
        )
    })
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
    @Operation(
        summary = "Listar Logs de Ataques",
        description = "Retorna logs de ataques capturados com paginação e ordenação por timestamp",
        tags = {"Logs"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Logs paginados",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Logs com Dados",
                    value = """
                        {
                          "timestamp": "2025-08-22T07:53:30.502907486",
                          "logs": [
                            {
                              "id": "68a4ed3b5c716c8fc6323124",
                              "timestamp": "2025-08-19T18:31:39.993",
                              "sourceIp": "127.0.0.1",
                              "port": 2222,
                              "protocol": "SSH",
                              "username": "root",
                              "password": "admin123",
                              "command": null,
                              "sessionId": "d57ff685-5274-46fc-925c-49433cc74857",
                              "banner": "SSH-2.0-OpenSSH_8.2p1 Ubuntu-4ubuntu0.5",
                              "successful": false
                            }
                          ],
                          "totalElements": 8,
                          "currentPage": 0,
                          "size": 3,
                          "totalPages": 3
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "200",
            description = "Lista vazia",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Sem Logs",
                    value = """
                        {
                          "logs": [],
                          "totalPages": 0,
                          "totalElements": 0,
                          "currentPage": 0,
                          "size": 100,
                          "timestamp": "2025-08-22T07:53:30.502907486"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor"
        )
    })
    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getAllLogs(
            @Parameter(description = "Número da página (começa em 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página", example = "100")
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
    @Operation(
        summary = "Estatísticas Gerais",
        description = "Retorna estatísticas gerais da honeypot, incluindo contagem de logs por protocolo",
        tags = {"Estatísticas"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Estatísticas gerais",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Estatísticas com Dados",
                    value = """
                        {
                          "sshLogs": 6,
                          "telnetLogs": 2,
                          "totalLogs": 8,
                          "timestamp": "2025-08-22T07:53:16.517300799"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor"
        )
    })
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
    
    @Operation(
        summary = "Top IPs Atacantes",
        description = "Retorna os IPs que mais tentaram ataques, ordenados por número de tentativas",
        tags = {"Estatísticas"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Top IPs atacantes",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Top IPs com Dados",
                    value = """
                        {
                          "limit": 5,
                          "topIps": [
                            {
                              "ip": "127.0.0.1",
                              "count": 8,
                              "lastAttack": "2025-08-19T18:31:39.993"
                            }
                          ],
                          "message": "Top 5 IPs mais ativos",
                          "total": 1,
                          "timestamp": "2025-08-22T07:53:21.561580459"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "200",
            description = "Lista vazia",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Sem Ataques",
                    value = """
                        {
                          "message": "Nenhum ataque registrado ainda",
                          "topIps": [],
                          "limit": 5,
                          "timestamp": "2025-08-22T07:53:21.561580459"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Erro",
                    value = """
                        {
                          "error": "Erro ao buscar top IPs: Timeout na consulta",
                          "timestamp": "2025-08-22T07:53:21.561580459"
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/stats/top-ips")
    public ResponseEntity<Map<String, Object>> getTopIps(
            @Parameter(description = "Número máximo de IPs a retornar", example = "10")
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
    
    @Operation(
        summary = "Top Credenciais Tentadas",
        description = "Retorna as combinações de usuário/senha mais tentadas pelos atacantes",
        tags = {"Estatísticas"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Top credenciais tentadas",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Top Credenciais com Dados",
                    value = """
                        {
                          "total": 3,
                          "message": "Top 3 credenciais mais tentadas",
                          "topCredentials": [
                            {
                              "password": "123456",
                              "count": 2,
                              "username": "admin"
                            },
                            {
                              "password": "admin123",
                              "count": 2,
                              "username": "root"
                            },
                            {
                              "password": "password",
                              "count": 1,
                              "username": "test"
                            }
                          ],
                          "limit": 3,
                          "timestamp": "2025-08-22T07:53:25.7"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "200",
            description = "Lista vazia",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Sem Credenciais",
                    value = """
                        {
                          "message": "Nenhuma credencial registrada ainda",
                          "topCredentials": [],
                          "limit": 3,
                          "timestamp": "2025-08-22T07:53:21.561580459"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Erro",
                    value = """
                        {
                          "error": "Erro ao buscar top credenciais: Timeout na consulta",
                          "timestamp": "2025-08-22T07:53:21.561580459"
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/stats/top-credentials")
    public ResponseEntity<Map<String, Object>> getTopCredentials(
            @Parameter(description = "Número máximo de credenciais a retornar", example = "10")
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
    
    @Operation(
        summary = "Estatísticas por Hora",
        description = "Retorna número de ataques agrupados por hora nas últimas 24 horas",
        tags = {"Estatísticas"}
    )
    @GetMapping("/stats/timeline")
    public ResponseEntity<Map<String, Object>> getTimelineStats() {
        try {
            Map<String, Object> timeline = statisticsService.getTimelineStats();
            return ResponseEntity.ok(timeline);
        } catch (Exception e) {
            log.error("Erro ao buscar estatísticas de timeline: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Erro ao buscar timeline: " + e.getMessage(),
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
