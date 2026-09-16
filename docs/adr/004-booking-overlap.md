# ADR 004: Booking overlap and checkout correctness

## Decision

- Stay modeled as `daterange` with GiST **exclusion constraint** per `property_id` for `PENDING` and `CONFIRMED`.
- Redis lock: per-property token with Lua release; reduces races under contention.
- Checkout day exclusive: `[check_in, check_out)`.
- Price computed server-side via property-service pricing API (client cannot set `pricePerNight`).
- `PENDING` bookings expire on a schedule; payment must match booking total and guest ownership.

## Alternatives

- App-only SELECT + INSERT — races under READ COMMITTED.
- Redis as sole truth — does not survive application bugs.

## Industry

Postgres exclusion constraints are common for resource calendars; Redis locks often supplement throughput.
