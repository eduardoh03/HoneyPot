#!/bin/bash

echo "🧪 Testando HoneyPot SSH/Telnet"
echo "=================================="

# Verificar se a aplicação está rodando
echo "1. Verificando status da aplicação..."
curl -s http://localhost:8080/api/honeypot/status | jq '.'

echo -e "\n2. Verificando estatísticas..."
curl -s http://localhost:8080/api/honeypot/stats | jq '.'

echo -e "\n3. Testando conexão SSH (porta 2222)..."
if nc -z localhost 2222; then
    echo "✅ Porta SSH (2222) está aberta"
else
    echo "❌ Porta SSH (2222) não está acessível"
fi

echo -e "\n4. Testando conexão Telnet (porta 2323)..."
if nc -z localhost 2323; then
    echo "✅ Porta Telnet (2323) está aberta"
else
    echo "❌ Porta Telnet (2323) não está acessível"
fi

echo -e "\n5. Testando conexão SSH real..."
echo -e "SSH-2.0-OpenSSH_8.2p1 Ubuntu-4ubuntu0.5\ntest\npassword" | nc localhost 2222 | head -5

echo -e "\n6. Testando conexão Telnet real..."
echo -e "test\npassword\nexit" | nc localhost 2323 | head -10

echo -e "\n7. Verificando logs após teste..."
sleep 2
curl -s "http://localhost:8080/api/honeypot/logs?size=5" | jq '.'

echo -e "\n✅ Teste concluído!"
