# Payment refunds

## API

`POST /api/payments/{paymentId}/refund` — **JWT required**

Refunds the Stripe (or simulated) charge and sets payment status to `REFUNDED`. Publishes `payment_refunded` on Kafka for notifications.

## Who may call

| Caller | Allowed when |
|--------|----------------|
| **Guest** (`payment.guestId`) | Booking is **CANCELLED** and payment is **SUCCESS** |
| **Host** (`payment.hostId`) | Same |
| **Other users** | Never (403 / bad request) |

There is no public admin refund API in the demo app.

## Business rules

1. Payment must be `SUCCESS` (not `PENDING`, `FAILED`, or already `REFUNDED`).
2. Booking linked to the payment must be **`CANCELLED`** (cancel via `POST /api/bookings/{id}/cancel` as guest or host first).
3. Refund runs through the configured gateway (`payment.gateway=stripe` uses Stripe Refunds API).

## Typical flows

**Guest:** Cancel trip on trip detail → **Issue refund** (if payment succeeded).

**Host:** Guest or host cancels → host opens **Reservations** → **Refund payment** on a cancelled, paid booking.

## Stripe test mode

Use a booking that was paid with test card `4242…`, cancel it, then call refund from the UI or:

```http
POST /api/payments/{paymentId}/refund
Authorization: Bearer <guest-or-host-jwt>
```

## Webhooks (production)

Set `STRIPE_WEBHOOK_SECRET` in repo-root `.env` (see `docs/STRIPE.md`). Webhook handler verifies signatures and ignores events for already-**REFUNDED** payments so retries stay idempotent.
