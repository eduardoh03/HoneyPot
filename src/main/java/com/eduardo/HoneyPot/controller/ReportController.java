package com.eduardo.HoneyPot.controller;

import com.eduardo.HoneyPot.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
@Slf4j
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    /**
     * Gera relatório de ataques em PDF
     */
    @GetMapping("/attacks/pdf")
    public ResponseEntity<byte[]> generateAttackReportPDF() {
        try {
            log.info("Gerando relatório de ataques em PDF...");
            byte[] pdfContent = reportService.generateAttackReportPDF();
            
            String filename = "relatorio_ataques_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            
            log.info("Relatório PDF gerado com sucesso: {} bytes", pdfContent.length);
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
                
        } catch (Exception e) {
            log.error("Erro ao gerar relatório PDF: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gera relatório de ataques em Excel
     */
    @GetMapping("/attacks/excel")
    public ResponseEntity<byte[]> generateAttackReportExcel() {
        try {
            log.info("Gerando relatório de ataques em Excel...");
            byte[] excelContent = reportService.generateAttackReportExcel();
            
            String filename = "relatorio_ataques_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            
            log.info("Relatório Excel gerado com sucesso: {} bytes", excelContent.length);
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
                
        } catch (Exception e) {
            log.error("Erro ao gerar relatório Excel: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gera relatório de estatísticas em Excel
     */
    @GetMapping("/statistics/excel")
    public ResponseEntity<byte[]> generateStatisticsReportExcel() {
        try {
            log.info("Gerando relatório de estatísticas em Excel...");
            byte[] excelContent = reportService.generateStatisticsReportExcel();
            
            String filename = "relatorio_estatisticas_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            
            log.info("Relatório de estatísticas Excel gerado com sucesso: {} bytes", excelContent.length);
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
                
        } catch (Exception e) {
            log.error("Erro ao gerar relatório de estatísticas: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gera relatório de notificações em Excel
     */
    @GetMapping("/notifications/excel")
    public ResponseEntity<byte[]> generateNotificationsReportExcel() {
        try {
            log.info("Gerando relatório de notificações em Excel...");
            byte[] excelContent = reportService.generateNotificationsReportExcel();
            
            String filename = "relatorio_notificacoes_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            
            log.info("Relatório de notificações Excel gerado com sucesso: {} bytes", excelContent.length);
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
                
        } catch (Exception e) {
            log.error("Erro ao gerar relatório de notificações: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gera relatório consolidado em Excel (todas as informações)
     */
    @GetMapping("/consolidated/excel")
    public ResponseEntity<byte[]> generateConsolidatedReportExcel() {
        try {
            log.info("Gerando relatório consolidado em Excel...");
            byte[] excelContent = reportService.generateConsolidatedReportExcel();
            
            String filename = "relatorio_consolidado_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            
            log.info("Relatório consolidado Excel gerado com sucesso: {} bytes", excelContent.length);
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
                
        } catch (Exception e) {
            log.error("Erro ao gerar relatório consolidado: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gera todos os relatórios disponíveis
     */
    @GetMapping("/all")
    public ResponseEntity<String> generateAllReports() {
        try {
            log.info("Gerando todos os relatórios disponíveis...");
            
            // Gerar todos os relatórios
            reportService.generateAttackReportPDF();
            reportService.generateAttackReportExcel();
            reportService.generateStatisticsReportExcel();
            reportService.generateNotificationsReportExcel();
            reportService.generateConsolidatedReportExcel();
            
            log.info("Todos os relatórios foram gerados com sucesso");
            return ResponseEntity.ok("Todos os relatórios foram gerados com sucesso");
            
        } catch (Exception e) {
            log.error("Erro ao gerar todos os relatórios: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Erro ao gerar relatórios: " + e.getMessage());
        }
    }
}
