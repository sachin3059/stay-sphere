# StaySphere demo script

1. `docker compose up -d` (Postgres, Redis, Kafka, services, gateway).
2. Register or log in as a guest; create a booking with an `Idempotency-Key` header.
3. Pay with an amount matching the booking total; confirm auto-confirm via `payment_success` (booking-service consumer).
4. Check notification logs and availability calendar block from `booking_confirmed`.
5. Optional: `curl -H "X-Correlation-Id: demo-1" …` through the gateway and grep logs for the same id.
6. Run `./gradlew :booking-service:test` or full `./gradlew build`.
