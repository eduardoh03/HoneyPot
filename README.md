# HoneyPot SSH/Telnet com Spring Boot

> **Sistema de honeypot profissional para captura e análise de ataques SSH/Telnet**

[![Java](https://img.shields.io/badge/Java-24.0.2-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-5.5.1-blue.svg)](https://www.mongodb.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Visão Geral

Honeypot SSH/Telnet desenvolvido em **Spring Boot** com arquitetura limpa, seguindo princípios **SOLID** e **Clean Architecture**. O sistema captura, analisa e monitora tentativas de ataque em tempo real, fornecendo insights valiosos sobre padrões de segurança.

## Arquitetura do Sistema

### Estrutura de Camadas

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │ HoneyPotController │  │   Web Security  │  │   Swagger   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                     Business Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │ HoneyPotService │  │  LogService     │  │StatisticsSvc│ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
│  ┌─────────────────┐                                       │
│  │ManagementService│                                       │
│  └─────────────────┘                                       │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                     Data Layer                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │AttackLogRepository│  │  MongoDB       │  │   Cache     │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Services Implementados

| Service | Responsabilidade | Status |
|---------|------------------|---------|
| **HoneyPotService** | Core da honeypot (SSH/Telnet) | ✅ Implementado |
| **LogService** | Gerenciamento de logs e consultas | ✅ Implementado |
| **StatisticsService** | Análises e estatísticas | ✅ Implementado |
| **ManagementService** | Controle e monitoramento | ✅ Implementado |

## Funcionalidades Implementadas

### **Core da Honeypot**
- ✅ **Portas SSH (2222/22)** e **Telnet (2323/23)** configuráveis
- ✅ **Banners falsos** simulando OpenSSH/Telnet real
- ✅ **Captura de credenciais** (usuário/senha) em tempo real
- ✅ **Shell fake** com comandos básicos (`ls`, `pwd`, `uname`, `echo`)
- ✅ **Logging automático** de todas as interações
- ✅ **Auto-inicialização** configurável

### **Persistência e Dados**
- ✅ **MongoDB** com Spring Data
- ✅ **Collection `attack_logs`** estruturada
- ✅ **Índices otimizados** para consultas rápidas
- ✅ **Docker Compose** para MongoDB + Mongo Express

### **API REST Completa**
- ✅ **16 endpoints** implementados e testados
- ✅ **Paginação inteligente** nos logs
- ✅ **Filtros avançados** (IP, protocolo, período, usuário)
- ✅ **Estatísticas em tempo real**
- ✅ **Health checks** e monitoramento
- ✅ **Tratamento de erros** centralizado

### **Arquitetura e Qualidade**
- ✅ **Clean Architecture** implementada
- ✅ **Separação de responsabilidades** (SOLID)
- ✅ **Services especializados** por domínio
- ✅ **Controller limpo** apenas com mapeamento HTTP
- ✅ **Logs estruturados** e centralizados
- ✅ **Configuração por perfis** (dev/prod)

## Como Usar

### Pré-requisitos

```bash
# Sistema
- Java 24+ (JDK 24.0.2)
- Maven 3.6+
- Docker e Docker Compose
- Linux/Unix (para portas privilegiadas)

# Variáveis de ambiente
export JAVA_HOME=/usr/lib/jvm/jdk-24.0.2-oracle-x64
export PATH=$JAVA_HOME/bin:$PATH
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
- [ ] **Dashboard Web** - Interface gráfica para visualização
- [ ] **Cache Redis** - Otimização de performance
- [ ] **Métricas Avançadas** - Análises de segurança
- [ ] **Sistema de Alertas** - Notificações em tempo real
- [ ] **Relatórios Automáticos** - PDF/Excel

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

## Testes

### **Testes Unitários**
```bash
# Executar todos os testes
./mvnw test

# Executar com cobertura
./mvnw test jacoco:report

# Executar testes específicos
./mvnw test -Dtest=LogServiceTest
```

### **Testes de Integração**
```bash
# Testes com containers
./mvnw verify

# Testes específicos
./mvnw test -Dtest=HoneyPotIntegrationTest
```

### **Testes de Performance**
```bash
# Teste de carga com JMeter
jmeter -n -t tests/performance/honeypot-load-test.jmx

# Teste de stress
./mvnw gatling:test
```

## Segurança

### **Avisos Importantes**
> **ATENÇÃO**: Esta honeypot é para fins educacionais e de pesquisa.

### **Recomendações de Produção**
1. **Isolamento**: Execute em container Docker isolado
2. **Firewall**: Configure regras de acesso restritas
3. **Monitoramento**: Logs e alertas 24/7
4. **Rede**: Não exponha em rede pública sem proteção
5. **Backup**: Estratégia de backup para logs
6. **Auditoria**: Revisão regular de logs

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

## Contribuição

### **Como Contribuir**
1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

### **Reportar Bugs**
- Use o sistema de Issues do GitHub
- Inclua logs detalhados
- Descreva os passos para reproduzir
- Especifique ambiente e versões

### **Sugestões de Features**
- Abra uma Issue com label `enhancement`
- Descreva o caso de uso
- Inclua mockups se aplicável
- Discuta implementação com a comunidade

## Licença

Este projeto está licenciado sob a **MIT License** - veja o arquivo [LICENSE](LICENSE) para detalhes.

## Autores

- **Eduardo** - *Desenvolvimento inicial* - [@eduardo](https://github.com/eduardo)

## Agradecimentos

- Comunidade Spring Boot
- Contribuidores do MongoDB

---

<div align="center">

**Se este projeto te ajudou, considere dar uma estrela!**

[![GitHub stars](https://img.shields.io/github/stars/eduardoh03/HoneyPot.svg?style=social&label=Star)](https://github.com/eduardoh03/HoneyPot)
[![GitHub forks](https://img.shields.io/github/forks/eduardoh03/HoneyPot.svg?style=social&label=Fork)](https://github.com/eduardoh03/HoneyPot)
[![GitHub issues](https://img.shields.io/github/issues/eduardoh03/HoneyPot.svg)](https://github.com/eduardoh03/HoneyPot/issues)

</div>

