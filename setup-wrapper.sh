#!/bin/bash
set -e
echo "Baixando Gradle Wrapper..."
if ! command -v gradle &> /dev/null; then
    echo "Gradle não encontrado. Por favor instale o Gradle ou ajuste o script."
    exit 1
fi
gradle wrapper --gradle-version 7.5
chmod +x gradlew
echo "Wrapper gerado com sucesso."
