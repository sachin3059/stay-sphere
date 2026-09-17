$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

docker info 2>$null | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker is not running. Start Docker Desktop, then retry."
}

Write-Host "Building Spring Boot JARs (one Gradle run on your machine)..."
if (-not $env:JAVA_HOME) {
    Write-Warning "JAVA_HOME is not set. Install JDK 21 and set JAVA_HOME if gradlew fails."
}
.\gradlew.bat bootJar -x test --no-daemon

Write-Host "Starting containers..."
docker compose up -d --build

Write-Host "Done. Gateway: http://localhost:8080"
