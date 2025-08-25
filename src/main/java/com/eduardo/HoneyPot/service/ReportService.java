package com.eduardo.HoneyPot.service;

import com.eduardo.HoneyPot.model.AttackLog;
import com.eduardo.HoneyPot.model.CommandExecution;
import com.eduardo.HoneyPot.model.Notification;
import com.eduardo.HoneyPot.repository.AttackLogRepository;
import com.eduardo.HoneyPot.repository.NotificationRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportService {
    
    @Autowired
    private AttackLogRepository attackLogRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private StatisticsService statisticsService;
    
    /**
     * Gera relatório PDF dos ataques
     */
    public byte[] generateAttackReportPDF() {
        try {
            List<AttackLog> attacks = attackLogRepository.findAll();
            
            // Implementação do PDF será feita com iText7
            // Por enquanto retornamos um PDF básico
            return generateBasicPDFReport(attacks);
            
        } catch (Exception e) {
            log.error("Erro ao gerar relatório PDF: {}", e.getMessage());
            return new byte[0];
        }
    }
    
    /**
     * Gera relatório Excel dos ataques
     */
    public byte[] generateAttackReportExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Relatório de Ataques");
            
            // Cabeçalho
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Data/Hora", "IP Atacante", "Porta", "Protocolo", "Usuário", "Senha", "Banner", "Comandos", "Sucesso"};
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }
            
            // Dados dos ataques
            List<AttackLog> attacks = attackLogRepository.findAll();
            int rowNum = 1;
            
            for (AttackLog attack : attacks) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(formatDateTime(attack.getTimestamp()));
                row.createCell(1).setCellValue(attack.getSourceIp());
                row.createCell(2).setCellValue(attack.getPort());
                row.createCell(3).setCellValue(attack.getProtocol());
                row.createCell(4).setCellValue(attack.getUsername() != null ? attack.getUsername() : "");
                row.createCell(5).setCellValue(attack.getPassword() != null ? attack.getPassword() : "");
                row.createCell(6).setCellValue(attack.getBanner() != null ? attack.getBanner() : "");
                row.createCell(7).setCellValue(formatCommands(attack.getCommands()));
                row.createCell(8).setCellValue(attack.isSuccessful() ? "Sim" : "Não");
            }
            
            // Auto-dimensionar colunas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Converter para byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("Erro ao gerar relatório Excel: {}", e.getMessage());
            return new byte[0];
        }
    }
    
    /**
     * Gera relatório de estatísticas em Excel
     */
    public byte[] generateStatisticsReportExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Aba 1: Resumo Geral
            Sheet summarySheet = workbook.createSheet("Resumo Geral");
            createSummarySheet(summarySheet);
            
            // Aba 2: Ataques por Protocolo
            Sheet protocolSheet = workbook.createSheet("Ataques por Protocolo");
            createProtocolAnalysisSheet(protocolSheet);
            
            // Aba 3: Top IPs Atacantes
            Sheet topIpsSheet = workbook.createSheet("Top IPs Atacantes");
            createTopIpsSheet(topIpsSheet);
            
            // Aba 4: Comandos Mais Executados
            Sheet commandsSheet = workbook.createSheet("Comandos Executados");
            createCommandsAnalysisSheet(commandsSheet);
            
            // Aba 5: Timeline de Ataques
            Sheet timelineSheet = workbook.createSheet("Timeline de Ataques");
            createTimelineSheet(timelineSheet);
            
            // Converter para byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("Erro ao gerar relatório de estatísticas: {}", e.getMessage());
            return new byte[0];
        }
    }
    
    /**
     * Gera relatório de notificações em Excel
     */
    public byte[] generateNotificationsReportExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Relatório de Notificações");
            
            // Cabeçalho
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Data/Hora", "Tipo", "Título", "Mensagem", "IP Atacante", "Protocolo", "Usuário"};
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }
            
            // Dados das notificações
            List<Notification> notifications = notificationRepository.findAll();
            int rowNum = 1;
            
            for (Notification notification : notifications) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(formatDateTime(notification.getTimestamp()));
                row.createCell(1).setCellValue(notification.getType());
                row.createCell(2).setCellValue(notification.getTitle());
                row.createCell(3).setCellValue(notification.getMessage());
                row.createCell(4).setCellValue(notification.getSourceIp() != null ? notification.getSourceIp() : "");
                row.createCell(5).setCellValue(notification.getProtocol() != null ? notification.getProtocol() : "");
                row.createCell(6).setCellValue(notification.getUsername() != null ? notification.getUsername() : "");
            }
            
            // Auto-dimensionar colunas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Converter para byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("Erro ao gerar relatório de notificações: {}", e.getMessage());
            return new byte[0];
        }
    }
    
    /**
     * Gera relatório consolidado (todas as informações)
     */
    public byte[] generateConsolidatedReportExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Aba 1: Dashboard Executivo
            Sheet dashboardSheet = workbook.createSheet("Dashboard Executivo");
            createDashboardSheet(dashboardSheet);
            
            // Aba 2: Detalhamento de Ataques
            Sheet attacksSheet = workbook.createSheet("Detalhamento de Ataques");
            createDetailedAttacksSheet(attacksSheet);
            
            // Aba 3: Análise de Comportamento
            Sheet behaviorSheet = workbook.createSheet("Análise de Comportamento");
            createBehaviorAnalysisSheet(behaviorSheet);
            
            // Aba 4: Notificações e Alertas
            Sheet alertsSheet = workbook.createSheet("Notificações e Alertas");
            createAlertsSheet(alertsSheet);
            
            // Aba 5: Recomendações de Segurança
            Sheet recommendationsSheet = workbook.createSheet("Recomendações");
            createRecommendationsSheet(recommendationsSheet);
            
            // Converter para byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("Erro ao gerar relatório consolidado: {}", e.getMessage());
            return new byte[0];
        }
    }
    
    // Métodos auxiliares para criação das abas
    private void createSummarySheet(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Métrica");
        headerRow.createCell(1).setCellValue("Valor");
        
        List<AttackLog> attacks = attackLogRepository.findAll();
        long totalAttacks = attacks.size();
        long sshAttacks = attacks.stream().filter(a -> "SSH".equals(a.getProtocol())).count();
        long telnetAttacks = attacks.stream().filter(a -> "TELNET".equals(a.getProtocol())).count();
        
        int rowNum = 1;
        sheet.createRow(rowNum++).createCell(0).setCellValue("Total de Ataques");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(totalAttacks);
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Ataques SSH");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(sshAttacks);
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Ataques Telnet");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(telnetAttacks);
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Data de Geração");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }
    
    private void createProtocolAnalysisSheet(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Protocolo");
        headerRow.createCell(1).setCellValue("Total de Ataques");
        headerRow.createCell(2).setCellValue("Porcentagem");
        
        List<AttackLog> attacks = attackLogRepository.findAll();
        long total = attacks.size();
        
        Map<String, Long> protocolCounts = attacks.stream()
            .collect(Collectors.groupingBy(AttackLog::getProtocol, Collectors.counting()));
        
        int rowNum = 1;
        for (Map.Entry<String, Long> entry : protocolCounts.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
            row.createCell(2).setCellValue(String.format("%.2f%%", (entry.getValue() * 100.0) / total));
        }
    }
    
    private void createTopIpsSheet(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("IP Atacante");
        headerRow.createCell(1).setCellValue("Total de Tentativas");
        headerRow.createCell(2).setCellValue("Última Tentativa");
        
        List<AttackLog> attacks = attackLogRepository.findAll();
        Map<String, Long> ipCounts = attacks.stream()
            .collect(Collectors.groupingBy(AttackLog::getSourceIp, Collectors.counting()));
        
        int rowNum = 1;
        for (Map.Entry<String, Long> entry : ipCounts.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
            
            // Encontrar última tentativa deste IP
            LocalDateTime lastAttempt = attacks.stream()
                .filter(a -> entry.getKey().equals(a.getSourceIp()))
                .map(AttackLog::getTimestamp)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            
            row.createCell(2).setCellValue(lastAttempt != null ? formatDateTime(lastAttempt) : "");
        }
    }
    
    private void createCommandsAnalysisSheet(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Comando");
        headerRow.createCell(1).setCellValue("Total de Execuções");
        headerRow.createCell(2).setCellValue("IPs Únicos");
        
        List<AttackLog> attacks = attackLogRepository.findAll();
        Map<String, Long> commandCounts = new java.util.HashMap<>();
        Map<String, java.util.Set<String>> commandIps = new java.util.HashMap<>();
        
                    for (AttackLog attack : attacks) {
                if (attack.getCommands() != null) {
                    for (CommandExecution command : attack.getCommands()) {
                        commandCounts.merge(command.getCommand(), 1L, Long::sum);
                        commandIps.computeIfAbsent(command.getCommand(), k -> new java.util.HashSet<>()).add(attack.getSourceIp());
                    }
                }
            }
        
        int rowNum = 1;
        for (Map.Entry<String, Long> entry : commandCounts.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
            row.createCell(2).setCellValue(commandIps.get(entry.getKey()).size());
        }
    }
    
    private void createTimelineSheet(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Data/Hora");
        headerRow.createCell(1).setCellValue("IP Atacante");
        headerRow.createCell(2).setCellValue("Protocolo");
        headerRow.createCell(3).setCellValue("Ação");
        
        List<AttackLog> attacks = attackLogRepository.findAll();
        attacks.sort((a1, a2) -> a1.getTimestamp().compareTo(a2.getTimestamp()));
        
        int rowNum = 1;
        for (AttackLog attack : attacks) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(formatDateTime(attack.getTimestamp()));
            row.createCell(1).setCellValue(attack.getSourceIp());
            row.createCell(2).setCellValue(attack.getProtocol());
            row.createCell(3).setCellValue("Tentativa de Conexão");
        }
    }
    
    private void createDashboardSheet(Sheet sheet) {
        // Dashboard executivo com gráficos e métricas principais
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("DASHBOARD EXECUTIVO - HONEYPOT");
        
        Row subtitleRow = sheet.createRow(1);
        subtitleRow.createCell(0).setCellValue("Relatório de Segurança e Monitoramento");
        
        // Métricas principais
        List<AttackLog> attacks = attackLogRepository.findAll();
        long totalAttacks = attacks.size();
        long uniqueIps = attacks.stream().map(AttackLog::getSourceIp).distinct().count();
        
        Row metric1Row = sheet.createRow(3);
        metric1Row.createCell(0).setCellValue("Total de Ataques Detectados:");
        metric1Row.createCell(1).setCellValue(totalAttacks);
        
        Row metric2Row = sheet.createRow(4);
        metric2Row.createCell(0).setCellValue("IPs Únicos Atacantes:");
        metric2Row.createCell(1).setCellValue(uniqueIps);
        
        Row metric3Row = sheet.createRow(5);
        metric3Row.createCell(0).setCellValue("Período de Monitoramento:");
        metric3Row.createCell(1).setCellValue("Ativo");
        
        Row metric4Row = sheet.createRow(6);
        metric4Row.createCell(0).setCellValue("Data de Geração:");
        metric4Row.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }
    
    private void createDetailedAttacksSheet(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Data/Hora", "IP Atacante", "Porta", "Protocolo", "Usuário", "Senha", "Comandos", "Sucesso"};
        
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        
        List<AttackLog> attacks = attackLogRepository.findAll();
        int rowNum = 1;
        
        for (AttackLog attack : attacks) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(formatDateTime(attack.getTimestamp()));
            row.createCell(1).setCellValue(attack.getSourceIp());
            row.createCell(2).setCellValue(attack.getPort());
            row.createCell(3).setCellValue(attack.getProtocol());
            row.createCell(4).setCellValue(attack.getUsername() != null ? attack.getUsername() : "");
            row.createCell(5).setCellValue(attack.getPassword() != null ? attack.getPassword() : "");
            row.createCell(6).setCellValue(formatCommands(attack.getCommands()));
            row.createCell(7).setCellValue(attack.isSuccessful() ? "Sim" : "Não");
        }
    }
    
    private void createBehaviorAnalysisSheet(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Padrão de Comportamento");
        headerRow.createCell(1).setCellValue("Descrição");
        headerRow.createCell(2).setCellValue("Frequência");
        
        List<AttackLog> attacks = attackLogRepository.findAll();
        
        // Análise de padrões
        int rowNum = 1;
        
        // Padrão 1: Múltiplas tentativas do mesmo IP
        Map<String, Long> ipAttempts = attacks.stream()
            .collect(Collectors.groupingBy(AttackLog::getSourceIp, Collectors.counting()));
        
        long multipleAttempts = ipAttempts.values().stream().filter(count -> count > 1).count();
        Row pattern1Row = sheet.createRow(rowNum++);
        pattern1Row.createCell(0).setCellValue("Múltiplas Tentativas");
        pattern1Row.createCell(1).setCellValue("IPs com mais de uma tentativa de conexão");
        pattern1Row.createCell(2).setCellValue(multipleAttempts);
        
        // Padrão 2: Comandos de reconhecimento
        long reconnaissanceCommands = attacks.stream()
            .filter(a -> a.getCommands() != null)
            .flatMap(a -> a.getCommands().stream())
            .filter(cmd -> cmd.getCommand().startsWith("netstat") || cmd.getCommand().startsWith("ss") || cmd.getCommand().startsWith("ps"))
            .count();
        
        Row pattern2Row = sheet.createRow(rowNum++);
        pattern2Row.createCell(0).setCellValue("Comandos de Reconhecimento");
        pattern2Row.createCell(1).setCellValue("Comandos para análise do sistema");
        pattern2Row.createCell(2).setCellValue(reconnaissanceCommands);
    }
    
    private void createAlertsSheet(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Tipo de Alerta");
        headerRow.createCell(1).setCellValue("Descrição");
        headerRow.createCell(2).setCellValue("Recomendação");
        
        int rowNum = 1;
        
        Row alert1Row = sheet.createRow(rowNum++);
        alert1Row.createCell(0).setCellValue("Comandos Críticos");
        alert1Row.createCell(1).setCellValue("Execução de comandos perigosos");
        alert1Row.createCell(2).setCellValue("Monitorar e bloquear IPs suspeitos");
        
        Row alert2Row = sheet.createRow(rowNum++);
        alert2Row.createCell(0).setCellValue("Tentativas de Download");
        alert2Row.createCell(1).setCellValue("Tentativas de baixar arquivos");
        alert2Row.createCell(2).setCellValue("Implementar filtros de rede");
        
        Row alert3Row = sheet.createRow(rowNum++);
        alert3Row.createCell(0).setCellValue("Acesso a Arquivos Sensíveis");
        alert3Row.createCell(1).setCellValue("Tentativas de leitura de arquivos do sistema");
        alert3Row.createCell(2).setCellValue("Auditar permissões de arquivos");
    }
    
    private void createRecommendationsSheet(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Recomendação");
        headerRow.createCell(1).setCellValue("Prioridade");
        headerRow.createCell(2).setCellValue("Descrição");
        
        int rowNum = 1;
        
        Row rec1Row = sheet.createRow(rowNum++);
        rec1Row.createCell(0).setCellValue("Implementar Rate Limiting");
        rec1Row.createCell(1).setCellValue("ALTA");
        rec1Row.createCell(2).setCellValue("Limitar tentativas de conexão por IP");
        
        Row rec2Row = sheet.createRow(rowNum++);
        rec2Row.createCell(0).setCellValue("Configurar Firewall");
        rec2Row.createCell(1).setCellValue("ALTA");
        rec2Row.createCell(2).setCellValue("Bloquear IPs maliciosos automaticamente");
        
        Row rec3Row = sheet.createRow(rowNum++);
        rec3Row.createCell(0).setCellValue("Monitoramento 24/7");
        rec3Row.createCell(1).setCellValue("MÉDIA");
        rec3Row.createCell(2).setCellValue("Implementar alertas em tempo real");
        
        Row rec4Row = sheet.createRow(rowNum++);
        rec4Row.createCell(0).setCellValue("Backup de Logs");
        rec4Row.createCell(1).setCellValue("MÉDIA");
        rec4Row.createCell(2).setCellValue("Backup automático dos logs de ataque");
    }
    
    // Métodos auxiliares
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
    
    private String formatCommands(List<CommandExecution> commands) {
        if (commands == null || commands.isEmpty()) return "";
        return commands.stream()
            .map(CommandExecution::getCommand)
            .collect(Collectors.joining("; "));
    }
    
    private byte[] generateBasicPDFReport(List<AttackLog> attacks) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
    
            // Título
            document.add(new Paragraph("Relatório de Ataques Honeypot")
                    .setBold()
                    .setFontSize(16));
    
            document.add(new Paragraph("Data de Geração: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
    
            document.add(new Paragraph("Total de Ataques: " + attacks.size()));
            document.add(new Paragraph("\n"));
    
            // Lista de ataques
            for (AttackLog attack : attacks) {
                document.add(new Paragraph(
                    "IP: " + attack.getSourceIp() +
                    " | Protocolo: " + attack.getProtocol() +
                    " | Data: " + formatDateTime(attack.getTimestamp())
                ));
            }
    
            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Erro ao gerar PDF: {}", e.getMessage());
            return new byte[0];
        }
    }
    
}
