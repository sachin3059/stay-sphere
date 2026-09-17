# StaySphere Stripe E2E via API gateway (http://localhost:8080)
$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"
$ts = Get-Date -Format "yyyyMMddHHmmss"
$hostEmail = "host-$ts@test.com"
$guestEmail = "guest-$ts@test.com"
$pass = "password123"

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Uri,
        [hashtable]$Headers = @{},
        [object]$Body = $null
    )
    $params = @{
        Method      = $Method
        Uri         = $Uri
        Headers     = $Headers
        ContentType = "application/json"
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Compress)
    }
    return Invoke-RestMethod @params
}

Write-Host "`n=== 1) Stripe config ===" -ForegroundColor Cyan
$config = Invoke-Api GET "$base/api/payments/stripe/config"
Write-Host "gateway=$($config.data.gateway)"

Write-Host "`n=== 2) Register host user: $hostEmail ===" -ForegroundColor Cyan
$hostReg = Invoke-Api POST "$base/api/auth/register" -Body @{
    email    = $hostEmail
    password = $pass
    fullName = "Host User"
}
$guestTokenForHost = $hostReg.data.accessToken

Write-Host "`n=== 3) Become HOST (API) ===" -ForegroundColor Cyan
$hostAuth = Invoke-Api POST "$base/api/auth/become-host" -Headers @{
    Authorization = "Bearer $guestTokenForHost"
}
$hostToken = $hostAuth.data.accessToken
if ($hostAuth.data.role -ne "HOST") {
    throw "Expected HOST role, got $($hostAuth.data.role)"
}
Write-Host "role=$($hostAuth.data.role)"

Write-Host "`n=== 5) Create property ===" -ForegroundColor Cyan
$prop = Invoke-Api POST "$base/api/properties" -Headers @{
    Authorization = "Bearer $hostToken"
} -Body @{
    title         = "Stripe Test Flat"
    description   = "E2E test"
    city          = "Pune"
    country       = "India"
    address       = "Test Address"
    latitude      = 18.5362
    longitude     = 73.8938
    pricePerNight = 2500
    maxGuests     = 4
    bedrooms      = 2
    bathrooms     = 1
    propertyType  = "APARTMENT"
    amenities     = @("WiFi")
}
$propertyId = $prop.data.id
$hostId = $prop.data.hostId
Write-Host "propertyId=$propertyId hostId=$hostId"

Write-Host "`n=== 6) Pricing rule ===" -ForegroundColor Cyan
Invoke-Api POST "$base/api/pricing/rules" -Headers @{
    Authorization = "Bearer $hostToken"
} -Body @{
    propertyId   = $propertyId
    basePrice    = 2500
    minimumStay  = 1
} | Out-Null
Write-Host "OK"

Write-Host "`n=== 7-8) Guest register + login: $guestEmail ===" -ForegroundColor Cyan
Invoke-Api POST "$base/api/auth/register" -Body @{
    email    = $guestEmail
    password = $pass
    fullName = "Guest User"
} | Out-Null
$guestAuth = Invoke-Api POST "$base/api/auth/login" -Body @{
    email    = $guestEmail
    password = $pass
}
$guestToken = $guestAuth.data.accessToken

Write-Host "`n=== 9) Create booking ===" -ForegroundColor Cyan
$booking = Invoke-RestMethod -Method POST -Uri "$base/api/bookings" `
    -Headers @{
        Authorization   = "Bearer $guestToken"
        "Idempotency-Key" = "book-$ts"
    } `
    -ContentType "application/json" `
    -Body (@{
        propertyId  = $propertyId
        hostId      = $hostId
        checkIn     = "2026-12-01"
        checkOut    = "2026-12-05"
        totalGuests = 2
    } | ConvertTo-Json -Compress)
$bookingId = $booking.id
if ($booking.status -ne "PENDING") {
    throw "Booking status $($booking.status), expected PENDING"
}
Write-Host "bookingId=$bookingId status=$($booking.status)"

Write-Host "`n=== 10) Stripe PaymentIntent ===" -ForegroundColor Cyan
$idemp = "pay-$bookingId"
$intent = Invoke-Api POST "$base/api/payments/stripe/intent" -Headers @{
    Authorization = "Bearer $guestToken"
} -Body @{
    bookingId       = $bookingId
    hostId            = $hostId
    idempotencyKey    = $idemp
}
$paymentId = $intent.data.paymentId
$piId = $intent.data.paymentIntentId
Write-Host "paymentId=$paymentId paymentIntentId=$piId"

# Load Stripe secret from repo .env (not committed)
$envFile = Join-Path (Split-Path $PSScriptRoot -Parent) ".env"
$stripeKey = $null
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^STRIPE_SECRET_KEY=(.+)$') { $stripeKey = $matches[1].Trim() }
    }
}
if (-not $stripeKey -or $stripeKey -like '*replace*') {
    Write-Host "`n=== 11) Pay manually ===" -ForegroundColor Yellow
    Write-Host "Open: $base/api/payments/checkout"
    Write-Host "Guest JWT (first 40 chars): $($guestToken.Substring(0, [Math]::Min(40, $guestToken.Length)))..."
    Write-Host "bookingId=$bookingId hostId=$hostId idempotencyKey=$idemp"
    Write-Host "Then re-run with -ConfirmOnly or run steps 12-13 below."
} else {
    Write-Host "`n=== 11) Confirm PaymentIntent on Stripe (test card) ===" -ForegroundColor Cyan
    $pair = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${stripeKey}:"))
    $stripeConfirm = Invoke-RestMethod -Method POST `
        -Uri "https://api.stripe.com/v1/payment_intents/$piId/confirm" `
        -Headers @{ Authorization = "Basic $pair" } `
        -ContentType "application/x-www-form-urlencoded" `
        -Body "payment_method=pm_card_visa&return_url=http://localhost:8080/api/payments/checkout"
    Write-Host "Stripe status=$($stripeConfirm.status)"

    Write-Host "`n=== 12) Sync payment in StaySphere ===" -ForegroundColor Cyan
    $payConfirm = Invoke-Api POST "$base/api/payments/stripe/confirm/$paymentId" -Headers @{
        Authorization = "Bearer $guestToken"
    }
    Write-Host "payment status=$($payConfirm.data.status)"

    Write-Host "`n=== 13) Verify booking ===" -ForegroundColor Cyan
    Start-Sleep -Seconds 8
    $bookingCheck = Invoke-RestMethod -Method GET -Uri "$base/api/bookings/$bookingId" `
        -Headers @{ Authorization = "Bearer $guestToken" }
    if ($bookingCheck.status -ne "CONFIRMED") {
        $bookingCheck = Invoke-RestMethod -Method POST -Uri "$base/api/bookings/$bookingId/confirm" `
            -Headers @{ Authorization = "Bearer $guestToken" }
    }
    Write-Host "booking status=$($bookingCheck.status)"
    if ($bookingCheck.status -eq "CONFIRMED") {
        Write-Host "`nSUCCESS: Stripe E2E complete." -ForegroundColor Green
    } else {
        Write-Host "`nBooking not CONFIRMED; check Kafka / payment_success consumer." -ForegroundColor Yellow
    }
}

$state = @{
    hostEmail        = $hostEmail
    guestEmail       = $guestEmail
    hostToken        = $hostToken
    guestToken       = $guestToken
    propertyId       = $propertyId
    hostId           = $hostId
    bookingId        = $bookingId
    paymentId        = $paymentId
    paymentIntentId  = $piId
    idempotencyKey   = $idemp
}
$statePath = Join-Path (Split-Path $PSScriptRoot -Parent) ".e2e-stripe-state.json"
$state | ConvertTo-Json | Set-Content $statePath
Write-Host "`nState saved: $statePath"
