# Upload demo seed images to Cloudinary (remote Unsplash -> staysphere/seed/{slug}/photoN)
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$envFile = Join-Path $root ".env"
if (-not (Test-Path $envFile)) { throw "Missing .env at repo root" }

Get-Content $envFile | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), "Process")
    }
}

$cloud = $env:CLOUDINARY_CLOUD_NAME
$key = $env:CLOUDINARY_API_KEY
$secret = $env:CLOUDINARY_API_SECRET
if (-not $cloud -or -not $key -or -not $secret) {
    throw "Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET in .env"
}

$slugs = @(
    "goa-villa-01", "goa-studio-02", "mumbai-apt-03", "pune-apt-04", "blr-house-05", "manali-cabin-06",
    "jaipur-house-07", "udaipur-villa-08", "rishikesh-cottage-09", "coorg-estate-10", "delhi-studio-11", "kochi-apt-12"
)

$remotePhotos = @(
    "https://images.unsplash.com/photo-1613490493576-7fde63acd811?w=800&q=80",
    "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800&q=80",
    "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=80",
    "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800&q=80",
    "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800&q=80"
)

function Get-Signature([hashtable]$Params, [string]$ApiSecret) {
    $sorted = $Params.Keys | Sort-Object
    $pairs = foreach ($k in $sorted) { "$k=$($Params[$k])" }
    $toSign = ($pairs -join "&") + $ApiSecret
    $sha1 = [System.Security.Cryptography.SHA1]::Create()
    $hash = $sha1.ComputeHash([Text.Encoding]::UTF8.GetBytes($toSign))
    -join ($hash | ForEach-Object { $_.ToString("x2") })
}

$uploaded = 0
foreach ($slug in $slugs) {
    for ($n = 1; $n -le 5; $n++) {
        $folder = "staysphere/seed/$slug"
        $publicId = "photo$n"
        $remote = $remotePhotos[($n - 1) % $remotePhotos.Length]
        $timestamp = [int][double]::Parse((Get-Date -UFormat %s))
        $params = @{
            timestamp = $timestamp
            folder    = $folder
            public_id = $publicId
            overwrite = "true"
        }
        $sig = Get-Signature $params $secret
        $uri = "https://api.cloudinary.com/v1_1/$cloud/image/upload"
        & curl.exe -sS -X POST $uri `
            -F "file=$remote" `
            -F "api_key=$key" `
            -F "timestamp=$timestamp" `
            -F "signature=$sig" `
            -F "folder=$folder" `
            -F "public_id=$publicId" `
            -F "overwrite=true" | Out-Null
        if ($LASTEXITCODE -ne 0) { throw "Cloudinary upload failed for $folder/$publicId" }
        $uploaded++
        Write-Host "OK $folder/$publicId"
    }
}
Write-Host "Uploaded $uploaded images to Cloudinary."
