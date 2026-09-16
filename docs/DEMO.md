# StaySphere demo script

1. `docker compose up -d` (Postgres, Redis, Kafka, 6 services + gateway).
2. Register/login as guest; create booking with `Idempotency-Key`.
3. Pay with matching amount; observe auto-confirm via Kafka `payment_success`.
4. Show notification logs and calendar block event.
5. Run `./gradlew :booking-service:test`.
