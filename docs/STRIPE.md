# Stripe test checkout (end-to-end)

## 1. Configure repo-root `.env` only

Set (in `.env`):

```env
PAYMENT_GATEWAY=stripe
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...   # from Stripe CLI or Dashboard webhook
```

Restart **payment-service** (or full `docker compose up`).

## 2. Webhooks (recommended)

```bash
stripe listen --forward-to localhost:8084/api/payments/webhooks/stripe
```

Copy the printed `whsec_...` into `STRIPE_WEBHOOK_SECRET`.

Through the gateway:

```bash
stripe listen --forward-to localhost:8080/api/payments/webhooks/stripe
```

## 3. Pay for a booking

1. Register / login → copy `accessToken`. Hosts: `POST /api/auth/become-host` then use new token.
2. Create a **PENDING** booking (`POST /api/bookings` with `Idempotency-Key`).
3. Open checkout UI: **http://localhost:8080/api/payments/checkout** (or `:8084` direct).
4. Paste JWT, booking id, host id, idempotency key (e.g. `pay-<bookingId>`).
5. **Create PaymentIntent** → enter test card `4242 4242 4242 4242` → **Pay**.
6. Booking should move to **CONFIRMED** via `payment_success` → booking-service consumer.

## API (without HTML)

| Method | Path | Auth |
|--------|------|------|
| GET | `/api/payments/stripe/config` | Public |
| POST | `/api/payments/stripe/intent` | Bearer JWT |
| POST | `/api/payments/stripe/confirm/{paymentId}` | Bearer JWT |
| POST | `/api/payments/webhooks/stripe` | Stripe signature |

Legacy `POST /api/payments` only works when `PAYMENT_GATEWAY=simulate`.

## Refunds

See **`docs/PAYMENT_REFUNDS.md`** for who may call `POST /api/payments/{id}/refund` and the cancel-then-refund flow.
