package com.eduardo.HoneyPot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("🍯 HoneyPot SSH/Telnet API")
                .description("""
                    **Sistema de honeypot profissional para captura e análise de ataques SSH/Telnet**
                    
                    ## 🎯 Funcionalidades
                    - **Captura de ataques** SSH e Telnet em tempo real
                    - **Análise de credenciais** tentadas pelos atacantes
                    - **Estatísticas avançadas** de segurança
                    - **Monitoramento** completo do sistema
                    - **API REST** para integração e automação
                    
                    ## 🔒 Segurança
                    > ⚠️ **ATENÇÃO**: Esta honeypot é para fins educacionais e de pesquisa.
                    > Em produção, execute em ambiente isolado e com monitoramento adequado.
                    
                    
                    ## 📊 Endpoints Principais
                    - **Controle**: Start/Stop/Restart da honeypot
                    - **Logs**: Consulta e análise de ataques capturados
                    - **Estatísticas**: Métricas e análises de segurança
                    - **Monitoramento**: Health checks e status do sistema
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Eduardo")
                    .email("eduardo@example.com")
                    .url("https://github.com/eduardoh03/HoneyPot"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Servidor Local - Desenvolvimento"),
                new Server()
                    .url("https://honeypot.example.com")
                    .description("Servidor de Produção")
            ))
            .tags(List.of(
                new Tag()
                    .name("Controle")
                    .description("Endpoints para gerenciar a honeypot (start/stop/restart/status)"),
                new Tag()
                    .name("Logs")
                    .description("Endpoints para consultar e analisar logs de ataques"),
                new Tag()
                    .name("Estatísticas")
                    .description("Endpoints para métricas e análises de segurança"),
                new Tag()
                    .name("Monitoramento")
                    .description("Endpoints para health checks e monitoramento do sistema")
            ));
    }
}
