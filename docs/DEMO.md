# StaySphere demo script

1. Set keys in repo-root `.env`, then `docker compose up -d` (or `.\scripts\start-backend.ps1`).
2. **Host:** register → `POST /api/auth/become-host` with Bearer token → create property + pricing rule.
3. **Guest:** register/login → create a booking with an `Idempotency-Key` header.
3. Pay with an amount matching the booking total; confirm auto-confirm via `payment_success` (booking-service consumer).
4. Check notification logs and availability calendar block from `booking_confirmed`.
5. Optional: `curl -H "X-Correlation-Id: demo-1" …` through the gateway and grep logs for the same id.
6. **Stripe test checkout:** set keys in `.env` (`PAYMENT_GATEWAY=stripe`), open `/api/payments/checkout` — see [STRIPE.md](STRIPE.md).
7. Run `./gradlew :booking-service:test` or full `./gradlew build`.
