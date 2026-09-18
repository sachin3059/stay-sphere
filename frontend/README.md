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
2. **Auth** — register, login, logout, become-host (current)
3. **Listings** — search, detail, host create property (next)
4. **Booking & Stripe** — book flow + payment UI
