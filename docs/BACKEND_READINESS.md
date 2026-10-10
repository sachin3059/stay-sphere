# Backend readiness (pre-frontend)

The API gateway at **`:8080`** is the single entry point for a browser or mobile client.

## Done for frontend handoff

| Area | Notes |
|------|--------|
| **Auth** | Register/login/refresh/logout; `POST /api/auth/become-host` (Bearer JWT) upgrades GUEST → HOST |
| **CORS** | Configured on **api-gateway** via `CORS_ALLOWED_ORIGINS` (default `localhost:3000`, `5173`) |
| **Listings** | Properties, pricing, availability, Postgres search |
| **Booking** | Create/confirm/cancel, idempotency, overlap constraints |
| **Payments** | Stripe PaymentIntents + checkout page + confirm sync |
| **Events** | Kafka outbox, payment_success → booking confirmed |
| **Ops** | Actuator health, correlation id, Flyway per service |
| **E2E** | Manual flows via **frontend** at `http://localhost:5173` (gateway `:8080`) |

## Client contract

- **Base URL:** `http://localhost:8080` (dev)
- **Auth header:** `Authorization: Bearer <accessToken>` from `data.accessToken`
- **Host flows:** register → `POST /api/auth/become-host` → use new tokens → `POST /api/properties`
- **Guest booking:** register/login → `POST /api/bookings` with optional `Idempotency-Key`
- **Pay:** `POST /api/payments/stripe/intent` then Stripe.js or `/api/payments/checkout`

## Optional before production (not blocking UI dev)

- Stripe **webhooks** in deployed environments (`STRIPE_WEBHOOK_SECRET`)
- Admin-only role assignment (ADMIN), email verification
- SendGrid / Cloudinary keys for real emails and image CDN
- Rate limiting and API versioning

## Verify locally

```powershell
.\scripts\start-backend.ps1   # or docker compose up after bootJar
cd frontend && npm run dev    # register, host, book, pay in the UI
```
