# ADR 007: Checkout choreography

## Context

Checkout spans booking creation, payment, and confirmation. Phase 4 delivers reliable `payment_success` events via the transactional outbox.

## Decision

`booking-service` consumes `payment-events` and idempotently calls `confirmBookingInternal` when `event` is `payment_success`. Deduplication uses `booking_processed_events` keyed by `eventId` from the payment outbox.

`POST /api/bookings/{id}/confirm` remains for manual recovery or admin flows.

## Compensation

If confirmation fails after payment (e.g. booking already `EXPIRED`), the consumer transaction rolls back and Kafka retries. Persistent failures need manual reconciliation (confirm endpoint or refund). Already-`CONFIRMED` bookings are a no-op inside `confirmBookingInternal`.

## Industry

Choreography fits a short marketplace checkout before introducing a central saga orchestrator.
