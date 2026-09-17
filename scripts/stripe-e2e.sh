#!/usr/bin/env bash
# StaySphere Stripe E2E — Git Bash / WSL / macOS / Linux (no jq required)
set -euo pipefail
BASE="${BASE_URL:-http://localhost:8080}"
TS="$(date +%Y%m%d%H%M%S)"
HOST_EMAIL="host-${TS}@test.com"
GUEST_EMAIL="guest-${TS}@test.com"
PASS="password123"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT/.env"
STATE_FILE="$ROOT/.e2e-stripe-state.json"

if command -v python3 >/dev/null 2>&1; then
  PYTHON=python3
elif command -v python >/dev/null 2>&1; then
  PYTHON=python
else
  echo "Need python or python3 on PATH (for JSON). Or run: powershell -File scripts/stripe-e2e.ps1"
  exit 1
fi

# Usage: echo "$json" | json_get data.accessToken
json_get() {
  local path="$1"
  $PYTHON -c "
import json, sys
path = sys.argv[1].split('.')
obj = json.load(sys.stdin)
for key in path:
    obj = obj[key]
print(obj)
" "$path"
}

json_pretty() {
  $PYTHON -m json.tool
}

echo "=== 1) Stripe config ==="
curl -sf "$BASE/api/payments/stripe/config" | json_pretty

echo "=== 2) Register host ==="
curl -sf -X POST "$BASE/api/auth/register" -H "Content-Type: application/json" \
  -d "{\"email\":\"$HOST_EMAIL\",\"password\":\"$PASS\",\"fullName\":\"Host User\"}" | json_pretty

echo "=== 3) Promote HOST ==="
docker exec staysphere-postgres psql -U staysphere -d staysphere \
  -c "UPDATE users SET role = 'HOST' WHERE email = '$HOST_EMAIL';"

echo "=== 4) Host login ==="
HOST_JSON=$(curl -sf -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" \
  -d "{\"email\":\"$HOST_EMAIL\",\"password\":\"$PASS\"}")
HOST_TOKEN=$(echo "$HOST_JSON" | json_get data.accessToken)
echo "role=$(echo "$HOST_JSON" | json_get data.role)"

echo "=== 5) Create property ==="
PROP_JSON=$(curl -sf -X POST "$BASE/api/properties" \
  -H "Authorization: Bearer $HOST_TOKEN" -H "Content-Type: application/json" \
  -d '{"title":"Stripe Test Flat","description":"E2E","city":"Pune","country":"India","address":"Test","latitude":18.5362,"longitude":73.8938,"pricePerNight":2500,"maxGuests":4,"bedrooms":2,"bathrooms":1,"propertyType":"APARTMENT","amenities":["WiFi"]}')
PROPERTY_ID=$(echo "$PROP_JSON" | json_get data.id)
HOST_ID=$(echo "$PROP_JSON" | json_get data.hostId)
echo "propertyId=$PROPERTY_ID hostId=$HOST_ID"

echo "=== 6) Pricing rule ==="
curl -sf -X POST "$BASE/api/pricing/rules" \
  -H "Authorization: Bearer $HOST_TOKEN" -H "Content-Type: application/json" \
  -d "{\"propertyId\":\"$PROPERTY_ID\",\"basePrice\":2500,\"minimumStay\":1}" | json_pretty

echo "=== 7-8) Guest register + login ==="
curl -sf -X POST "$BASE/api/auth/register" -H "Content-Type: application/json" \
  -d "{\"email\":\"$GUEST_EMAIL\",\"password\":\"$PASS\",\"fullName\":\"Guest User\"}" >/dev/null
GUEST_JSON=$(curl -sf -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" \
  -d "{\"email\":\"$GUEST_EMAIL\",\"password\":\"$PASS\"}")
GUEST_TOKEN=$(echo "$GUEST_JSON" | json_get data.accessToken)

echo "=== 9) Create booking ==="
BOOK_JSON=$(curl -sf -X POST "$BASE/api/bookings" \
  -H "Authorization: Bearer $GUEST_TOKEN" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: book-$TS" \
  -d "{\"propertyId\":\"$PROPERTY_ID\",\"hostId\":\"$HOST_ID\",\"checkIn\":\"2026-12-01\",\"checkOut\":\"2026-12-05\",\"totalGuests\":2}")
BOOKING_ID=$(echo "$BOOK_JSON" | json_get id)
echo "bookingId=$BOOKING_ID status=$(echo "$BOOK_JSON" | json_get status)"

echo "=== 10) Stripe intent ==="
IDEMP="pay-$BOOKING_ID"
INTENT_JSON=$(curl -sf -X POST "$BASE/api/payments/stripe/intent" \
  -H "Authorization: Bearer $GUEST_TOKEN" -H "Content-Type: application/json" \
  -d "{\"bookingId\":\"$BOOKING_ID\",\"hostId\":\"$HOST_ID\",\"idempotencyKey\":\"$IDEMP\"}")
PAYMENT_ID=$(echo "$INTENT_JSON" | json_get data.paymentId)
PI_ID=$(echo "$INTENT_JSON" | json_get data.paymentIntentId)
echo "paymentId=$PAYMENT_ID pi=$PI_ID"

STRIPE_KEY=""
if [[ -f "$ENV_FILE" ]]; then
  STRIPE_KEY=$(grep -E '^STRIPE_SECRET_KEY=' "$ENV_FILE" | cut -d= -f2- | tr -d '\r')
fi
if [[ -z "$STRIPE_KEY" || "$STRIPE_KEY" == *replace* ]]; then
  echo "Set STRIPE_SECRET_KEY in .env, then confirm PI manually or use checkout page."
  exit 1
fi

echo "=== 11) Stripe confirm (test card) ==="
STRIPE_STATUS=$(curl -sf -X POST "https://api.stripe.com/v1/payment_intents/$PI_ID/confirm" \
  -u "$STRIPE_KEY:" \
  -d "payment_method=pm_card_visa" \
  -d "return_url=$BASE/api/payments/checkout" | json_get status)
echo "Stripe status=$STRIPE_STATUS"

echo "=== 12) StaySphere payment confirm ==="
curl -sf -X POST "$BASE/api/payments/stripe/confirm/$PAYMENT_ID" \
  -H "Authorization: Bearer $GUEST_TOKEN" | json_pretty

echo "=== 13) Verify booking ==="
sleep 8
BOOK_CHECK=$(curl -sf "$BASE/api/bookings/$BOOKING_ID" -H "Authorization: Bearer $GUEST_TOKEN")
STATUS=$(echo "$BOOK_CHECK" | json_get status)
if [[ "$STATUS" != "CONFIRMED" ]]; then
  STATUS=$(curl -sf -X POST "$BASE/api/bookings/$BOOKING_ID/confirm" \
    -H "Authorization: Bearer $GUEST_TOKEN" | json_get status)
fi
echo "booking status=$STATUS"

if [[ "$STATUS" == "CONFIRMED" ]]; then
  echo "SUCCESS: Stripe E2E complete."
else
  echo "FAILED: expected CONFIRMED, got $STATUS"
  exit 1
fi

export HOST_EMAIL GUEST_EMAIL HOST_TOKEN GUEST_TOKEN PROPERTY_ID HOST_ID \
  BOOKING_ID PAYMENT_ID PI_ID IDEMP STATE_FILE
$PYTHON -c "
import json, os
state = {
  'hostEmail': os.environ['HOST_EMAIL'],
  'guestEmail': os.environ['GUEST_EMAIL'],
  'hostToken': os.environ['HOST_TOKEN'],
  'guestToken': os.environ['GUEST_TOKEN'],
  'propertyId': os.environ['PROPERTY_ID'],
  'hostId': os.environ['HOST_ID'],
  'bookingId': os.environ['BOOKING_ID'],
  'paymentId': os.environ['PAYMENT_ID'],
  'paymentIntentId': os.environ['PI_ID'],
  'idempotencyKey': os.environ['IDEMP'],
}
with open(os.environ['STATE_FILE'], 'w') as f:
    json.dump(state, f, indent=2)
print('State saved:', os.environ['STATE_FILE'])
"
