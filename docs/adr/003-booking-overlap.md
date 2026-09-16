# ADR 003: Booking overlap

## Decision

- Stay modeled as `daterange` with GiST **exclusion constraint** per `property_id` for `PENDING` and `CONFIRMED`.
- Redis lock: per-property token with Lua release; optimization under contention.
- Checkout day exclusive: `[check_in, check_out)`.

## Alternatives

- App-only SELECT + INSERT — races under READ COMMITTED.
- Redis as sole truth — does not survive app bugs.

## Industry

Postgres exclusion constraints are used for resource calendars; Redis locks often supplement throughput.
