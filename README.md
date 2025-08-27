# HoneyPot SSH/Telnet com Spring Boot

> **Sistema de honeypot educacional para estudo e análise de ataques SSH/Telnet**

[![Java](https://img.shields.io/badge/Java-24.0.2-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-5.5.1-blue.svg)](https://www.mongodb.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Educational](https://img.shields.io/badge/Purpose-Educational-blue.svg)](https://github.com/yourusername/honeypot)

## AVISO IMPORTANTE - PROPÓSITO EDUCACIONAL

**Este projeto foi desenvolvido exclusivamente para fins educacionais e de pesquisa.**

### Sobre o Projeto
- **Finalidade:** Aprendizado e estudo de segurança cibernética
- **Não comercial:** Este software não está à venda e nunca será comercializado
- **Educacional:** Criado para fins acadêmicos e de pesquisa em cybersecurity
- **Responsabilidade:** Use apenas em ambiente controlado e para fins legítimos
- **Legal:** O uso deve estar em conformidade com as leis locais

### Tecnologias Utilizadas
O projeto foi construído com as seguintes tecnologias:
- **Backend:** Java 24 + Spring Boot 3.5.4
- **Banco de Dados:** MongoDB 5.5.1 com Spring Data
- **Documentação:** OpenAPI 3 (Swagger)
- **Containerização:** Docker + Docker Compose
- **Logging:** SLF4J + Logback
- **Build:** Maven 3.6+

## Visão Geral

Honeypot SSH/Telnet desenvolvido em **Spring Boot** com arquitetura limpa, seguindo princípios **SOLID** e **Clean Architecture**. O sistema simula serviços SSH e Telnet vulneráveis para capturar, analisar e monitorar tentativas de ataque em tempo real, fornecendo insights valiosos sobre padrões de ataques cibernéticos para fins educacionais.

## Arquitetura do Sistema

### Estrutura de Camadas

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌───────────────────┐  ┌─────────────────┐  ┌─────────────┐│
│  │ HoneyPotController│  │   Web Security  │  │   Swagger   ││
│  └───────────────────┘  └─────────────────┘  └─────────────┘│
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                     Business Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │ HoneyPotService │  │  LogService     │  │StatisticsSvc│  │
│  └─────────────────┘  └─────────────────┘  └─────────────┘  │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │ManagementService│  │NotificationSvc  │  │ ReportSvc   │  │
│  └─────────────────┘  └─────────────────┘  └─────────────┘  │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                     Data Layer                              │
│  ┌───────────────────┐  ┌─────────────────┐                 │
│  │AttackLogRepository│  │  MongoDB        │                 │
│  └───────────────────┘  └─────────────────┘                 │
└─────────────────────────────────────────────────────────────┘
```

### Services Implementados

| Service | Responsabilidade | Status |
|---------|------------------|---------|
| **HoneyPotService** | Core da honeypot (SSH/Telnet) | ✅ Implementado |
| **LogService** | Gerenciamento de logs e consultas | ✅ Implementado |
| **StatisticsService** | Análises e estatísticas | ✅ Implementado |
| **ManagementService** | Controle e monitoramento | ✅ Implementado |
| **NotificationService** | Sistema de notificações e alertas | ✅ Implementado |
| **ReportService** | Geração de relatórios e exportação | ✅ Implementado |

## O que o Projeto Faz

Este honeypot educacional simula serviços SSH e Telnet vulneráveis para capturar e analisar tentativas de ataques cibernéticos. É uma ferramenta de aprendizado que permite:

### **Objetivos Educacionais**
- **Análise de Comportamento:** Entender como atacantes agem em sistemas vulneráveis
- **Estudo de Padrões:** Identificar credenciais, comandos e técnicas mais utilizadas
- **Visualização de Dados:** Dashboard web para análise visual dos ataques
- **Sistema de Alertas:** Notificações em tempo real de atividades suspeitas
- **Documentação:** Logs detalhados de todas as interações para estudo posterior

### **Funcionalidades Implementadas**

#### **Core da Honeypot**
- ✅ **Serviços SSH (porta 2222/22) e Telnet (porta 2323/23)** configuráveis
- ✅ **Banners realistas** simulando OpenSSH e sistemas Ubuntu autênticos
- ✅ **Captura de credenciais** (usuário/senha) em tempo real
- ✅ **Shell interativo fake** com 25+ comandos Linux simulados (`ls`, `pwd`, `ps`, `netstat`, etc.)
- ✅ **Detecção de comandos críticos** (wget, curl, iptables, systemctl)
- ✅ **Logging automático** de todas as interações e comandos
- ✅ **Auto-inicialização** configurável por perfil

#### **Persistência e Dados**
- ✅ **MongoDB** com Spring Data para armazenamento de logs
- ✅ **Collection `attack_logs`** com schema estruturado
- ✅ **Sistema de notificações** com diferentes tipos de alertas
- ✅ **Índices otimizados** para consultas rápidas
- ✅ **Docker Compose** para infraestrutura completa (MongoDB + Mongo Express)

#### **API REST Completa (20+ Endpoints)**
- ✅ **Controle da Honeypot:** start, stop, restart, status, health
- ✅ **Gestão de Logs:** consulta paginada com filtros avançados
- ✅ **Estatísticas em Tempo Real:** top IPs, credenciais, timeline
- ✅ **Sistema de Notificações:** alertas, filtros, marcação como lida
- ✅ **Relatórios:** exportação em PDF e Excel
- ✅ **Documentação OpenAPI:** Swagger UI integrado

#### **Dashboard Web Interativo**
- ✅ **Interface responsiva** com design moderno
- ✅ **Gráficos em tempo real** de ataques por hora
- ✅ **Métricas visuais:** contadores, top atacantes, credenciais
- ✅ **Filtros dinâmicos** por protocolo, IP, período
- ✅ **Notificações em tempo real** com diferentes níveis de prioridade

#### **Arquitetura e Qualidade**
- ✅ **Clean Architecture** com separação clara de responsabilidades
- ✅ **Princípios SOLID** aplicados em toda a estrutura
- ✅ **Services especializados** por domínio (Logs, Stats, Management, Notifications, Reports)
- ✅ **Controller RESTful** limpo com documentação OpenAPI
- ✅ **Logs estruturados** com diferentes níveis (INFO, WARN, ERROR)
- ✅ **Configuração por perfis** (desenvolvimento/produção)
- ✅ **Tratamento de erros** centralizado e consistente

## Como Usar

### Pré-requisitos

```bash
# Sistema
- Java 24+ (JDK 24.0.2)
- Maven 3.6+
- Docker e Docker Compose
- Linux/Unix (para portas privilegiadas)
```

### 1. Iniciar Infraestrutura

```bash
# Iniciar MongoDB + Mongo Express
cd docker
docker compose up -d

# Verificar status
docker ps
docker logs mongo
```

### 2. Executar a Aplicação

#### **Desenvolvimento** (Portas 2222/2323)
```bash
# Compilar e executar
./mvnw spring-boot:run

# Ou compilar primeiro
./mvnw clean package
java -jar target/HoneyPot-0.0.1-SNAPSHOT.jar
```

#### **Produção** (Portas 22/23 - requer root)
```bash
# Compilar
./mvnw clean package

# Executar com perfil de produção
sudo java -jar target/HoneyPot-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

### 3. Testar a Honeypot

```bash
# Teste rápido da API
curl http://localhost:8080/api/honeypot/status

# Teste SSH
ssh -p 2222 localhost
# Usuário: admin, Senha: 123456

# Teste Telnet
telnet localhost 2323

# Script de teste completo
./test-honeypot.sh
```

## Configuração

### Perfis Disponíveis

#### **Desenvolvimento** (Padrão)
```properties
# application.properties
honeypot.ssh.port=2222
honeypot.telnet.port=2323
honeypot.auto-start=true
honeypot.debug=true
```

#### **Produção**
```properties
# application-prod.properties
honeypot.ssh.port=22
honeypot.telnet.port=23
honeypot.auto-start=true
honeypot.production=true
```

### Personalização

```properties
# Banners personalizados
honeypot.ssh.banner=SSH-2.0-OpenSSH_8.2p1 Ubuntu-4ubuntu0.5
honeypot.telnet.banner=Ubuntu 20.04.3 LTS

# Configurações de segurança
honeypot.max-connections=100
honeypot.session-timeout=300
honeypot.log-level=INFO
```

## API REST - Endpoints

### **Controle da Honeypot**
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|---------|
| `POST` | `/api/honeypot/start` | Iniciar honeypot | ✅ |
| `POST` | `/api/honeypot/stop` | Parar honeypot | ✅ |
| `POST` | `/api/honeypot/restart` | Reiniciar honeypot | ✅ |
| `GET` | `/api/honeypot/status` | Status detalhado | ✅ |
| `GET` | `/api/honeypot/health` | Saúde do sistema | ✅ |

### **Consulta de Logs**
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|---------|
| `GET` | `/api/honeypot/logs` | Logs paginados | ✅ |
| `GET` | `/api/honeypot/logs/ip/{ip}` | Logs por IP | ✅ |
| `GET` | `/api/honeypot/logs/protocol/{protocol}` | Logs por protocolo | ✅ |
| `GET` | `/api/honeypot/logs/date-range` | Logs por período | ✅ |
| `GET` | `/api/honeypot/logs/username/{username}` | Logs por usuário | ✅ |
| `DELETE` | `/api/honeypot/logs` | Limpar todos os logs | ✅ |

### **Estatísticas e Análises**
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|---------|
| `GET` | `/api/honeypot/stats` | Estatísticas gerais | ✅ |
| `GET` | `/api/honeypot/stats/top-ips` | Top IPs atacantes | ✅ |
| `GET` | `/api/honeypot/stats/top-credentials` | Top credenciais | ✅ |

## Exemplos de Uso da API

### **Top IPs Atacantes**
```bash
# Top 10 IPs (padrão)
curl http://localhost:8080/api/honeypot/stats/top-ips

# Top 5 IPs
curl "http://localhost:8080/api/honeypot/stats/top-ips?limit=5"
```

**Resposta:**
```json
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
```

### **Top Credenciais Tentadas**
```bash
curl "http://localhost:8080/api/honeypot/stats/top-credentials?limit=3"
```

**Resposta:**
```json
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
    }
  ],
  "limit": 3,
  "timestamp": "2025-08-22T07:53:25.7"
}
```

### **Logs Paginados**
```bash
curl "http://localhost:8080/api/honeypot/logs?page=0&size=3"
```

## Estrutura do Banco

### **Collection: `attack_logs`**
```json
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
```

### **Índices MongoDB**
```javascript
// Índices otimizados para consultas rápidas
db.attack_logs.createIndex({"timestamp": -1})
db.attack_logs.createIndex({"sourceIp": 1})
db.attack_logs.createIndex({"protocol": 1})
db.attack_logs.createIndex({"username": 1})
db.attack_logs.createIndex({"sourceIp": 1, "timestamp": -1})
```

## Docker

### **MongoDB**
```yaml
# docker/docker-compose.yml
services:
  mongo:
    image: mongo:5.5
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_ROOT_USERNAME: root
      MONGO_INITDB_ROOT_PASSWORD: example
      MONGO_INITDB_DATABASE: honeypot_dev
```

### **Mongo Express**
```yaml
  mongo-express:
    image: mongo-express:latest
    ports:
      - "8081:8081"
    environment:
      ME_CONFIG_MONGODB_ADMINUSERNAME: root
      ME_CONFIG_MONGODB_ADMINPASSWORD: example
      ME_CONFIG_MONGODB_URL: mongodb://root:example@mongo:27017/
```

## Roadmap de Desenvolvimento

### **FASE 1: MVP (CONCLUÍDA)**
- [x] Honeypot SSH/Telnet básico
- [x] Captura de credenciais
- [x] Logging em MongoDB
- [x] API REST básica
- [x] Docker para infraestrutura

### **FASE 2: Arquitetura (CONCLUÍDA)**
- [x] Refatoração para Clean Architecture
- [x] Services especializados
- [x] Tratamento de erros centralizado
- [x] Logs estruturados

### **FASE 3: Funcionalidades Avançadas (EM DESENVOLVIMENTO)**
- [X] **Dashboard Web** - Interface gráfica para visualização
- [ ] **Métricas Avançadas** - Análises de segurança
- [x] **Sistema de Alertas** - Notificações em tempo real
- [x] **Relatórios Automáticos** - PDF/Excel

### **FASE 4: Testes e Qualidade (PRÓXIMOS PASSOS)**
- [ ] **Testes Unitários** - JUnit 5 + Mockito
- [ ] **Testes de Integração** - TestContainers
- [ ] **Testes de Performance** - JMeter/Gatling
- [ ] **Cobertura de Código** - JaCoCo
- [ ] **Análise Estática** - SonarQube

### **FASE 5: Produção e Monitoramento**
- [ ] **CI/CD Pipeline** - GitHub Actions
- [ ] **Monitoramento** - Prometheus + Grafana
- [ ] **Logs Centralizados** - ELK Stack
- [ ] **Segurança** - OWASP ZAP
- [ ] **Deploy** - Kubernetes


## Segurança e Responsabilidade

### **AVISOS CRÍTICOS DE SEGURANÇA**

> **ATENÇÃO**: Esta honeypot é exclusivamente para fins educacionais e de pesquisa.

#### **Declaração de Responsabilidade**
- **Uso Educacional Apenas:** Este software foi criado para aprendizado e pesquisa
- **Proibido Uso Malicioso:** Não use para atividades ilegais ou não autorizadas
- **Ambiente Controlado:** Execute apenas em laboratórios e ambientes de teste
- **Conformidade Legal:** O usuário é responsável por cumprir leis locais
- **Isolamento Obrigatório:** Nunca exponha diretamente à internet pública

#### **AVISO SOBRE COMERCIALIZAÇÃO**

**ESTE SOFTWARE NÃO É COMERCIAL E NUNCA SERÁ VENDIDO**

- **Projeto Acadêmico:** Desenvolvido para fins puramente educacionais
- **Sempre Gratuito:** Este software permanecerá sempre gratuito
- **Não à Venda:** Não tem e nunca terá propósito comercial
- **Código Aberto:** Disponível para a comunidade acadêmica
- **Compartilhamento:** Incentivamos o uso em universidades e cursos

### **Recomendações de Segurança para Uso Educacional**

#### **Ambiente Seguro**
1. **Isolamento Obrigatório:** Execute em VM isolada ou container Docker
2. **Firewall:** Configure regras restritivas de acesso
3. **Monitoramento:** Logs e alertas para detectar uso inadequado
4. **Rede Local:** Use apenas em redes internas, nunca em internet pública
5. **Backup Seguro:** Estratégia de backup criptografado para logs
6. **Auditoria Regular:** Revisão periódica de logs e atividades

#### **Responsabilidades do Usuário**
- **Documentação:** Mantenha registro de uso para fins acadêmicos
- **Supervisão:** Uso sob supervisão de instrutor qualificado (recomendado)
- **Incident Response:** Plano de resposta para uso inadequado
- **Acesso Controlado:** Limite acesso apenas a pesquisadores autorizados

### **Configurações de Segurança**
```properties
# application-prod.properties
honeypot.security.enabled=true
honeypot.security.max-connections=50
honeypot.security.rate-limit=100
honeypot.security.blacklist.enabled=true
honeypot.security.whitelist.enabled=false
```

## Logs e Monitoramento

### **Estrutura de Logs**
```json
{
  "timestamp": "2025-08-22T07:53:07.021874654",
  "level": "INFO",
  "service": "HoneyPot",
  "component": "SSHService",
  "message": "Nova conexão SSH",
  "metadata": {
    "sourceIp": "192.168.1.100",
    "port": 2222,
    "sessionId": "uuid"
  }
}
```

### **Métricas Disponíveis**
- **Conexões ativas** por protocolo
- **Taxa de ataques** por minuto/hora
- **IPs mais ativos** em tempo real
- **Credenciais mais tentadas**
- **Performance** da aplicação
- **Uso de recursos** do sistema

## Troubleshooting

### **Problemas Comuns**

#### **Porta já em uso**
```bash
# Verificar portas
sudo netstat -tlnp | grep :22
sudo netstat -tlnp | grep :23

# Parar serviços conflitantes
sudo systemctl stop ssh
sudo systemctl stop telnet
```

#### **Permissões insuficientes**
```bash
# Para portas privilegiadas (22/23)
sudo java -jar target/HoneyPot-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

#### **MongoDB não conecta**
```bash
# Verificar status
docker ps | grep mongo
docker logs mongo

# Reiniciar serviços
docker compose restart
```

#### **Aplicação não inicia**
```bash
# Verificar logs
./mvnw spring-boot:run

# Verificar Java
java -version
echo $JAVA_HOME

# Limpar e recompilar
./mvnw clean compile
```

## Recursos e Referências

### **Links Úteis**
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb)
- [MongoDB Documentation](https://docs.mongodb.com/)
- [Docker Documentation](https://docs.docker.com/)
- [Honeypot Security](https://en.wikipedia.org/wiki/Honeypot_(computing))

### **Artigos e Tutoriais**
- [Building a Honeypot with Spring Boot](https://example.com)
- [MongoDB Security Best Practices](https://example.com)
- [Spring Boot Testing Strategies](https://example.com)

### **Ferramentas Relacionadas**
- [Wireshark](https://www.wireshark.org/) - Análise de tráfego
- [Nmap](https://nmap.org/) - Scanner de rede
- [Metasploit](https://www.metasploit.com/) - Framework de teste

## Contribuição Educacional

### **Como Contribuir para o Projeto Educacional**
Este projeto aceita contribuições que melhorem seu valor educacional:

1. **Fork o Projeto:** Crie sua própria cópia para experimentação
2. **Crie uma Branch Educacional:** `git checkout -b feature/EducationalImprovement`
3. **Documente suas Mudanças:** Inclua explicações pedagógicas
4. **Commit com Propósito:** `git commit -m 'Add educational feature: X'`
5. **Push Educacional:** `git push origin feature/EducationalImprovement`
6. **Pull Request Acadêmico:** Explique o valor educacional da contribuição

### **Tipos de Contribuições Bem-Vindas**
- **Melhorias na Documentação:** Tutoriais, exemplos, explicações
- **Material Didático:** Guias de laboratório, exercícios práticos
- **Features Educacionais:** Funcionalidades que ajudam no aprendizado
- **Correções de Bugs:** Sempre bem-vindas para melhorar a experiência
- **Traduções:** Documentação em outros idiomas
- **Análises:** Estudos de caso, relatórios de pesquisa

### **Reportar Problemas**
- **Issues Educacionais:** Use templates específicos para contexto acadêmico
- **Logs Detalhados:** Inclua logs para fins de aprendizado
- **Reprodução:** Passos claros para reproduzir problemas
- **Ambiente:** Especifique ambiente de teste/laboratório

### **Sugestões de Melhorias Educacionais**
- **Label `educational-enhancement`:** Para features educacionais
- **Caso de Uso Acadêmico:** Descreva aplicação em ensino
- **Protótipos:** Mockups que melhorem a experiência de aprendizado
- **Discussão Colaborativa:** Envolva a comunidade acadêmica

## Licença Educacional

Este projeto está licenciado sob a **MIT License** com foco educacional - veja o arquivo [LICENSE](LICENSE) para detalhes completos.

### **Características da Licença Educacional:**
- **Uso Acadêmico Livre:** Universidades, escolas, cursos podem usar gratuitamente
- **Modificação Permitida:** Adapte para suas necessidades educacionais
- **Distribuição Educacional:** Compartilhe com outros educadores
- **Uso Comercial Proibido:** Não pode ser vendido ou usado comercialmente
- **Atribuição Acadêmica:** Credite o projeto em trabalhos acadêmicos

## Autor e Equipe Educacional

- **Eduardo** - *Desenvolvedor Principal* - [@eduardoh03](https://github.com/eduardoh03)
  - Criador do projeto para fins educacionais
  - Especialista em segurança cibernética e ensino
  - Aberto para colaborações acadêmicas

## Agradecimentos Acadêmicos

### **Comunidades que Inspiraram o Projeto:**
- **Comunidade Spring Boot:** Framework educacional excepcional
- **MongoDB Education:** Recursos e documentação de qualidade
- **Comunidade Acadêmica de Cybersecurity:** Inspiração e feedback
- **Educadores em Segurança:** Que compartilham conhecimento gratuitamente
- **Projetos Open Source Educacionais:** Que servem de exemplo

### **Instituições de Ensino (Convidadas a Usar):**
- Universidades com cursos de Cybersecurity
- Escolas técnicas de TI
- Cursos online de segurança
- Laboratórios de pesquisa em segurança

---

<div align="center">

## **Projeto Educacional Open Source**

**Se este projeto educacional foi útil para seu aprendizado, considere dar uma estrela!**

[![Educational](https://img.shields.io/badge/Purpose-Educational-blue.svg)](https://github.com/eduardoh03/HoneyPot)
[![Free](https://img.shields.io/badge/Price-Always_Free-green.svg)](https://github.com/eduardoh03/HoneyPot)
[![GitHub stars](https://img.shields.io/github/stars/eduardoh03/HoneyPot.svg?style=social&label=Star)](https://github.com/eduardoh03/HoneyPot)
[![GitHub forks](https://img.shields.io/github/forks/eduardoh03/HoneyPot.svg?style=social&label=Fork)](https://github.com/eduardoh03/HoneyPot)

### **Compartilhe o Conhecimento**

Este projeto foi criado para a comunidade educacional. 
Sinta-se livre para:
- Usar em aulas e laboratórios
- Estudar o código-fonte  
- Realizar pesquisas acadêmicas
- Contribuir com melhorias

### **Lembrete Final**

**NÃO É COMERCIAL • SEMPRE EDUCACIONAL • SEMPRE GRATUITO**

---

*Desenvolvidopara a comunidade educacional de cybersecurity*

</div>

