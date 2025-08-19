#!/bin/bash

echo "🔐 Testando HoneyPot SSH"
echo "=========================="

# Verificar se a aplicação está rodando
echo "1. Verificando status da aplicação..."
curl -s http://localhost:8080/api/honeypot/status | jq '.'

echo -e "\n2. Verificando estatísticas antes do teste..."
curl -s http://localhost:8080/api/honeypot/stats | jq '.'

echo -e "\n3. Testando conexão SSH básica..."
echo "Testando banner SSH..."
echo "quit" | nc localhost 2222 | head -3

echo -e "\n4. Testando tentativa de login SSH..."
echo "Simulando usuário e senha..."
echo -e "admin\n123456" | nc localhost 2222 | head -3

echo -e "\n5. Testando com dados SSH reais..."
echo "Simulando handshake SSH..."
echo -e "SSH-2.0-OpenSSH_8.2p1 Ubuntu-4ubuntu0.5\nroot\nadmin123" | nc localhost 2222 | head -3

echo -e "\n6. Aguardando processamento..."
sleep 3

echo -e "\n7. Verificando estatísticas após teste..."
curl -s http://localhost:8080/api/honeypot/stats | jq '.'

echo -e "\n8. Verificando logs SSH..."
curl -s "http://localhost:8080/api/honeypot/logs/protocol/SSH" | jq '.'

echo -e "\n✅ Teste SSH concluído!"
