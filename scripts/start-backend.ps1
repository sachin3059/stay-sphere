$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

# Docker prints capability warnings on stderr; do not treat those as fatal in PowerShell.
$prevEap = $ErrorActionPreference
$ErrorActionPreference = "Continue"
docker info *> $null
$dockerOk = ($LASTEXITCODE -eq 0)
$ErrorActionPreference = $prevEap
if (-not $dockerOk) {
    Write-Error "Docker is not running. Start Docker Desktop, then retry."
}

Write-Host "Building Spring Boot JARs (one Gradle run on your machine)..."
if (-not $env:JAVA_HOME) {
    Write-Warning "JAVA_HOME is not set. Install JDK 21 and set JAVA_HOME if gradlew fails."
}
.\gradlew.bat bootJar -x test --no-daemon

Write-Host "Starting containers..."
$ErrorActionPreference = "Continue"
docker compose up -d --build
if ($LASTEXITCODE -ne 0) {
    $ErrorActionPreference = "Stop"
    Write-Error "docker compose failed (exit $LASTEXITCODE)."
}
$ErrorActionPreference = "Stop"

Write-Host "Done. Gateway: http://localhost:8080"
