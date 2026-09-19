# Production deployment (Phase 19)

Goal: run StaySphere outside pure localhost dev (single URL for SPA + API, secrets in env, CI green).

## Quick start (Docker all-in-one)

1. Copy `env.template` → `.env` and set **production** values (see matrix below).
2. Build and run:

   ```bash
   docker compose up -d --build
   ```

3. Open **http://localhost** — the `web` service serves the React build and proxies `/api/*` to the gateway.

Dev loop unchanged: `npm run dev` on `:5173` with `VITE_API_URL=http://localhost:8080`.

## Environment matrix

| Variable | Where | Purpose |
|----------|--------|---------|
| `JWT_SECRET` | All services + gateway | Same 32+ char secret everywhere |
| `POSTGRES_*` / `SPRING_DATASOURCE_*` | Backend | Database |
| `CORS_ALLOWED_ORIGINS` | api-gateway | Comma-separated browser origins if SPA and API are on **different** hosts (e.g. `https://app.example.com`). Same-origin Docker `web` on port 80 often needs `http://localhost` only when testing. |
| `STRIPE_SECRET_KEY`, `STRIPE_PUBLISHABLE_KEY` | payment-service | Live or test keys |
| `STRIPE_WEBHOOK_SECRET` | payment-service | From Stripe Dashboard webhook |
| `PAYMENT_GATEWAY` | payment-service | `stripe` in production |
| `CLOUDINARY_*` | property-service | Listing photos |
| `GOOGLE_CLIENT_ID`, `GITHUB_*` | auth-service | OAuth |
| `VITE_*` / build args | frontend image build | OAuth client IDs baked at build time |
| `STAYSPHERE_BOOTSTRAP_ADMIN_EMAIL` | auth-service | First admin |
| `SENDGRID_API_KEY`, `SENDGRID_FROM_EMAIL` | notification-service | Transactional email (optional until set) |

Never commit `.env`. Use your host’s secret store in real production.

## Frontend build

```bash
cd frontend
npm ci
# API on another host:
VITE_API_URL=https://api.example.com npm run build
# Same host (nginx proxy /api):
VITE_API_URL= npm run build
```

Artifacts in `frontend/dist/`. The repo `frontend/Dockerfile` builds this and serves via nginx.

## Stripe webhooks (public URL)

1. Stripe Dashboard → Developers → Webhooks → Add endpoint.
2. URL: `https://<your-public-host>/api/payments/webhooks/stripe`
3. Events: at least `payment_intent.succeeded`, `payment_intent.payment_failed`, `charge.refunded` (as you use refunds).
4. Copy signing secret → `STRIPE_WEBHOOK_SECRET` in `.env`, restart payment-service.

Local testing: `stripe listen --forward-to localhost:8080/api/payments/webhooks/stripe` — see `docs/STRIPE.md`.

## OAuth in production

- Google: authorized JS origin = your SPA URL; client ID in `GOOGLE_CLIENT_ID` + `VITE_GOOGLE_CLIENT_ID` at build.
- GitHub: callback `https://<your-host>/auth/github/callback`; `VITE_GITHUB_CLIENT_ID` at build.

## SendGrid email

Set `SENDGRID_API_KEY` and a verified `SENDGRID_FROM_EMAIL`. Without a key, notifications are still stored in-app; email send is skipped (logged).

## CI

GitHub Actions runs `./gradlew build` and `frontend` `npm run build` on push/PR to `main`.

## Optional (not required for MVP)

- **Rate limiting** on gateway (Redis) — backlog.
- **TLS** — terminate at reverse proxy (Caddy, nginx, cloud load balancer) in front of `web` or gateway.

## Acceptance checklist

- [ ] Register / login (email or GitHub)
- [ ] Explore → book → pay (Stripe test or live)
- [ ] Webhook confirms booking (check booking status CONFIRMED)
- [ ] Host listing + photo upload (Cloudinary)
- [ ] Admin bootstrap works (`docs/ADMIN.md`)
