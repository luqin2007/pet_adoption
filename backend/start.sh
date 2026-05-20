#!/bin/bash
set -e

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_FILE="$BIN_DIR/target/backend-0.0.1-SNAPSHOT.jar"
ENV_FILE="$BIN_DIR/.env"
LOG_DIR="$BIN_DIR/logs"
JAVA_CMD="/usr/bin/java"

if [ ! -f "$JAR_FILE" ]; then
    echo "JAR not found: $JAR_FILE"
    echo "Run 'mvn clean package -DskipTests' first."
    exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
    echo ".env not found: $ENV_FILE"
    exit 1
fi

mkdir -p "$LOG_DIR"

set -a
. "$ENV_FILE"
set +a

exec "$JAVA_CMD" \
    -Xmx512m -Xms256m \
    -jar "$JAR_FILE" \
    --server.port=8080 \
    >> "$LOG_DIR/startup.log" 2>&1
