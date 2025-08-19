#!/bin/bash

echo "🔐 Teste Simples SSH HoneyPot"
echo "=============================="

# Verificar estatísticas iniciais
echo "📊 Estatísticas iniciais:"
curl -s http://localhost:8080/api/honeypot/stats | jq '.'

echo -e "\n🔍 Testando conexão SSH..."
echo "Enviando dados de teste..."

# Teste 1: Conexão simples
echo "Teste 1: Conexão simples"
echo "quit" | timeout 3 nc localhost 2222 || echo "Conexão fechada"

echo -e "\nTeste 2: Tentativa de login"
echo -e "admin\n123456" | timeout 3 nc localhost 2222 || echo "Conexão fechada"

echo -e "\nTeste 3: Dados SSH"
echo -e "SSH-2.0-OpenSSH_8.2p1 Ubuntu-4ubuntu0.5\nroot\nadmin123" | timeout 3 nc localhost 2222 || echo "Conexão fechada"

echo -e "\n⏳ Aguardando processamento..."
sleep 5

echo -e "\n📊 Estatísticas finais:"
curl -s http://localhost:8080/api/honeypot/stats | jq '.'

echo -e "\n📝 Logs SSH:"
curl -s "http://localhost:8080/api/honeypot/logs/protocol/SSH" | jq '.'

echo -e "\n✅ Teste concluído!"
