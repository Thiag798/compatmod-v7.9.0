#!/bin/bash
# CompatMod Gradle wrapper setup
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ ! -f "gradlew" ]; then
    echo "Generating Gradle wrapper..."
    if command -v gradle &> /dev/null; then
        gradle wrapper --gradle-version 8.9
    else
        echo "ERROR: Gradle not found. Please install Gradle 8.9 or download gradlew manually."
        echo "Visit: https://gradle.org/install/"
        exit 1
    fi
fi

echo "Gradle wrapper ready. Run: ./gradlew build"
