# StaySphere Web

React + TypeScript frontend for the StaySphere API gateway.

## Stack

- **Vite 6** · **React 19** · **TypeScript**
- **Tailwind CSS 4** · **React Router 7** · **TanStack Query** · **Zustand**

## Setup

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173). Ensure the backend gateway runs on `http://localhost:8080` and `CORS_ALLOWED_ORIGINS` includes `http://localhost:5173`.

## Project layout

```
src/
  components/   # UI primitives + layout shell
  config/       # Env helpers
  lib/api/      # Typed fetch client → gateway
  pages/        # Route-level screens
  routes/       # React Router config
  store/        # Auth session (persisted)
```

## Build order (incremental)

1. **Foundation** — layout, design tokens, API client, home
2. **Auth** — register, login, logout, become-host
3. **Listings** — explore, search, property detail
4. **Host** — create property + pricing
5. **Booking & Stripe** — book, pay, my trips (current)

### Booking flow (manual test)

1. Sign in as a guest, open a listing → **Book this stay**.
2. Pick dates and guests → **Continue to payment**.
3. Pay with test card `4242 4242 4242 4242` (any future expiry, any CVC).
4. After success you land on **My trips** with status `CONFIRMED` (or `PENDING` briefly if Kafka is still catching up — use cancel only while pending).

Routes: `/properties/:id/book`, `/bookings/:id/pay`, `/bookings/my`, `/bookings/complete` (Stripe redirect).
