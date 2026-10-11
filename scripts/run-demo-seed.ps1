# Full demo data: Postgres seed + property-service restart (seed SQL may use external image URLs)
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

Write-Host "1/2 Applying demo-seed.sql..."
python "$PSScriptRoot\generate_demo_seed.py" --apply

Write-Host "2/2 Restarting property-service (Bloom filter)..."
docker compose restart property-service

Write-Host "Done. Check: curl.exe http://localhost:8080/api/properties/search/advanced"
Write-Host "Or open Explore in the browser."
