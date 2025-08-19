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
- **IMPORTANTE**: Para desenvolvimento, usamos portas 2222 (SSH) e 2323 (Telnet). Para produção, configure as portas 22 e 23 no arquivo de configuração.

### 2. Iniciar MongoDB
```bash
cd docker
docker-compose up -d
```

### 3. Executar a Aplicação
```bash
# Compilar e executar
./mvnw spring-boot:run

# Ou compilar primeiro
./mvnw clean package
java -jar target/HoneyPot-0.0.1-SNAPSHOT.jar
```

### 4. Testar a Honeypot
```bash
# Executar script de teste
./test-honeypot.sh

# Ou testar manualmente
curl http://localhost:8080/api/honeypot/status
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
- `GET /api/honeypot/stats/top-ips` - Top IPs atacantes
- `GET /api/honeypot/stats/top-credentials` - Top credenciais tentadas

## 🧪 Testando a Honeypot

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
