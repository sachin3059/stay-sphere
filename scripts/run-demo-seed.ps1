# Full demo data: Cloudinary images + Postgres seed + property-service restart
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

Write-Host "1/3 Uploading seed images to Cloudinary..."
& "$PSScriptRoot\upload-seed-cloudinary.ps1"

Write-Host "2/3 Applying demo-seed.sql..."
python "$PSScriptRoot\generate_demo_seed.py" --apply

Write-Host "3/3 Restarting property-service (Bloom filter)..."
docker compose restart property-service

Write-Host "Done. Check: curl.exe http://localhost:8080/api/properties/search/advanced"
Write-Host "Or open Explore in the browser."
