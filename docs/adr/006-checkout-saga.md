# ADR 006: Checkout choreography

## Decision

`payment_success` events trigger idempotent booking confirmation in `booking-service`. Manual confirm endpoint remains for admin/recovery.

## Compensation

If confirmation fails after payment success, retry via consumer offset replay and `booking_processed_events` idempotency. Manual reconciliation documented for poison messages.

## Industry

Choreography is common for 2–3 step marketplace flows before introducing a central orchestrator.
