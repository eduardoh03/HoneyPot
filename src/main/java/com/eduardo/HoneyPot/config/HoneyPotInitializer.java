package com.eduardo.HoneyPot.config;

import com.eduardo.HoneyPot.service.HoneyPotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HoneyPotInitializer implements CommandLineRunner {
    
    @Autowired
    private HoneyPotService honeyPotService;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando HoneyPot SSH/Telnet...");
        
        // Aguardar um pouco para o Spring Boot inicializar completamente
        Thread.sleep(3000);
        
        try {
            honeyPotService.startHoneyPot();
            log.info("✅ HoneyPot iniciado com sucesso!");
        } catch (Exception e) {
            log.error("❌ Erro ao iniciar HoneyPot: {}", e.getMessage());
            log.error("Verifique se as portas 22 e 23 estão disponíveis e se você tem permissões de root");
        }
    }
}
