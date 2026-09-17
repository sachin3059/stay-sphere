#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

if ! docker info >/dev/null 2>&1; then
  echo "Docker is not running. Start Docker Desktop, then retry."
  exit 1
fi

echo "Building Spring Boot JARs (one Gradle run on your machine)..."
chmod +x gradlew
./gradlew bootJar -x test --no-daemon

echo "Starting containers..."
docker compose up -d --build

echo "Done. Gateway: http://localhost:8080"
