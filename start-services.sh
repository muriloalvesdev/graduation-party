#!/bin/bash

echo "Criando a rede graduation-network..."
docker network create graduation-network 2>/dev/null || echo "Rede 'graduation-network' já existe"

echo ""
echo "Iniciando o Keycloak..."
cd parent/keycloak
docker compose up -d


cd ../..

echo ""
echo "Compilando o auth-service..."
cd auth-service
mvn clean package -DskipTests

cd ..

echo ""
echo "Compilando o parent..."
cd parent
mvn clean install -DskipTests

cd ..

cd auth-service
echo ""
echo "Iniciando o auth-service..."
docker compose up -d

echo "Todos os serviços estão rodando!"