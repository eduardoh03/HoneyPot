# HoneyPot SSH/Telnet com Spring Boot

## 🚀 Roadmap de Desenvolvimento - Honeypot SSH/Telnet em Java com Spring Boot

### ✅ IMPLEMENTADO (MVP)
- ✅ Abrir portas **22 (SSH)** e **23 (Telnet)**
- ✅ Exibir **banner falso** simulando OpenSSH/Telnet
- ✅ Capturar **usuário e senha** digitados pelo atacante
- ✅ Criar **shell fake** com comandos básicos:
  - `ls`, `pwd`, `uname`, `echo`
- ✅ Logar todas as interações em **MongoDB**
- ✅ **API REST** para consultar os logs
- ✅ **Documentação** dos endpoints
- ✅ **Persistência** com MongoDB
- ✅ **Docker** para MongoDB

### 🔄 EM DESENVOLVIMENTO
- 🔄 **Segurança** com Docker isolado
- 🔄 **Visualização** com dashboards

### 📋 PRÓXIMOS PASSOS
- 📋 Integrar com **Grafana/Kibana**
- 📋 **Relatórios automáticos**
- 📋 **Autenticação** na API

## 🚀 Como Usar

### 1. Pré-requisitos
- Java 24+
- Maven 3.6+
- Docker e Docker Compose
- **IMPORTANTE**: 
  - **Desenvolvimento**: Portas 2222 (SSH) e 2323 (Telnet) - configuradas em `application.properties`
  - **Produção**: Portas 22 (SSH) e 23 (Telnet) - use o perfil `prod`

### 2. Iniciar MongoDB
```bash
cd docker
docker-compose up -d
```

### 3. Executar a Aplicação

#### Desenvolvimento (portas 2222/2323)
```bash
# Compilar e executar
./mvnw spring-boot:run

# Ou compilar primeiro
./mvnw clean package
java -jar target/HoneyPot-0.0.1-SNAPSHOT.jar
```

#### Produção (portas 22/23 - requer root)
```bash
# Compilar
./mvnw clean package

# Executar com perfil de produção (requer privilégios de root)
sudo java -jar target/HoneyPot-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 4. Testar a Honeypot
```bash
# Executar script de teste
./test-honeypot.sh

# Ou testar manualmente
curl http://localhost:8080/api/honeypot/status
```

## ⚙️ Configuração

### Perfis Disponíveis

#### Desenvolvimento (padrão)
- **Arquivo**: `application.properties`
- **Portas**: SSH (2222), Telnet (2323)
- **Execução**: `./mvnw spring-boot:run`

#### Produção
- **Arquivo**: `application-prod.properties`
- **Portas**: SSH (22), Telnet (23)
- **Execução**: `sudo java -jar app.jar --spring.profiles.active=prod`

### Personalizar Configurações
Edite o arquivo `application.properties` para alterar:
```properties
# Portas da honeypot
honeypot.ssh.port=2222
honeypot.telnet.port=2323

# Banners personalizados
honeypot.ssh.banner=SSH-2.0-OpenSSH_8.2p1 Ubuntu-4ubuntu0.5
honeypot.telnet.banner=Ubuntu 20.04.3 LTS

# Auto-inicialização
honeypot.auto-start=true
```

## 🔌 Endpoints da API

### Controle da Honeypot
- `POST /api/honeypot/start` - Iniciar honeypot
- `POST /api/honeypot/stop` - Parar honeypot
- `GET /api/honeypot/status` - Status atual

### Consulta de Logs
- `GET /api/honeypot/logs` - Todos os logs (com paginação)
- `GET /api/honeypot/logs/ip/{ip}` - Logs por IP
- `GET /api/honeypot/logs/protocol/{protocol}` - Logs por protocolo
- `GET /api/honeypot/logs/date-range` - Logs por período
- `GET /api/honeypot/logs/username/{username}` - Logs por usuário

### Estatísticas
- `GET /api/honeypot/stats` - Estatísticas gerais
- `GET /api/honeypot/stats/top-ips` - Top IPs atacantes ✅ IMPLEMENTED
- `GET /api/honeypot/stats/top-credentials` - Top credenciais tentadas

## 🧪 Testando a Honeypot

### Testando Endpoints da API

#### Top IPs Atacantes
```bash
# Top 10 IPs (padrão)
curl http://localhost:8080/api/honeypot/stats/top-ips

# Top 5 IPs
curl "http://localhost:8080/api/honeypot/stats/top-ips?limit=5"

# Resposta exemplo:
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
  "timestamp": "2025-08-19T19:14:08.323080645"
}
```

### Teste SSH
```bash
ssh -p 2222 localhost
# Ou
nc localhost 2222
```

### Teste Telnet
```bash
telnet localhost 2323
# Ou
nc localhost 2323
```

## 📊 Estrutura do Banco

### Collection: `attack_logs`
```json
{
  "id": "uuid",
  "timestamp": "2024-01-01T10:00:00",
  "sourceIp": "192.168.1.100",
  "port": 22,
  "protocol": "SSH",
  "username": "root",
  "password": "tentativa",
  "command": "ls",
  "sessionId": "uuid",
  "banner": "SSH-2.0-OpenSSH_8.2p1",
  "successful": false
}
```

## 🐳 Docker

### MongoDB
- Porta: 27017
- Usuário: root
- Senha: example
- Database: honeypot_dev

### Mongo Express
- Porta: 8081
- Usuário: admin
- Senha: pass
- URL: http://localhost:8081

## 🔒 Segurança

⚠️ **ATENÇÃO**: Esta honeypot é para fins educacionais e de pesquisa. Em produção:

1. **Isole** em container Docker
2. **Configure** firewall adequadamente
3. **Monitore** logs constantemente
4. **Não exponha** em rede pública sem proteção

## 📝 Logs

A aplicação gera logs detalhados de todas as interações:
- Conexões SSH/Telnet
- Tentativas de login
- Comandos executados
- IPs de origem
- Timestamps precisos

## 🚨 Troubleshooting

### Porta já em uso
```bash
# Verificar portas em uso
sudo netstat -tlnp | grep :22
sudo netstat -tlnp | grep :23

# Parar serviços que usam essas portas
sudo systemctl stop ssh
sudo systemctl stop telnet
```

### Permissões insuficientes
```bash
# Rodar como root (não recomendado para produção)
sudo java -jar target/HoneyPot-0.0.1-SNAPSHOT.jar
```

### MongoDB não conecta
```bash
# Verificar se MongoDB está rodando
docker ps | grep mongo

# Ver logs do MongoDB
docker logs mongo
```

## 📚 Recursos Adicionais

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MongoDB Spring Data](https://spring.io/projects/spring-data-mongodb)
- [Honeypot Security](https://en.wikipedia.org/wiki/Honeypot_(computing))
