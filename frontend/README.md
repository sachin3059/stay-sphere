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
5. **Booking & Stripe** — book, pay, my trips
6. **Host ops & guest polish** — host reservations, resume pending payment
7. **Listing photos** — Cloudinary upload on create + manage photos
8. **Session** — refresh on 401 + proactive token refresh
9. **Availability** — check dates, host calendar blocks
10. **Pricing quote** — server calculate on book
11. **Waitlist** — join when unavailable, my waitlist, host queue
12. **Notifications** — inbox, header bell, poll/refetch
13. **Edit listings** — host edit page, unlist, remove photos
14. **Trip detail** — booking + payment receipt on `/bookings/:id`
15. **Refunds** — cancel then refund (guest trip detail + host reservations)
16. **Explore UX** — gallery, map, sort, pagination
17. **Social auth** — Google + GitHub, profile page
18. **Admin** — users, listing moderation, health links (current)

### Booking flow (manual test)

1. Sign in as a guest, open a listing → **Book this stay**.
2. Pick dates and guests → **Continue to payment**.
3. Pay with test card `4242 4242 4242 4242` (any future expiry, any CVC).
4. After success you land on **My trips** with status `CONFIRMED` (or `PENDING` briefly if Kafka is still catching up — use cancel only while pending).

Routes: `/properties/:id/book`, `/bookings/:id/pay`, `/bookings/my`, `/bookings/complete` (Stripe redirect).

### Step 6 (manual test)

- **Guest:** My trips → **Complete payment** on a `PENDING` booking (reuses Stripe intent idempotency `pay-{bookingId}`).
- **Host:** User menu → **Reservations** (`/host/reservations`) — bookings for all your listings via `GET /api/bookings/property/{id}`.

### Step 7 (photos)

Set `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` in repo-root `.env` and restart **property-service**. Then:

- **List a property** — choose photos before **Publish listing**, or
- **My listings** → **Add photos** / **Manage photos** (`/host/listings/:id/photos`).

### Step 8 (session)

Access tokens expire; the app calls `POST /api/auth/refresh` on **401**, retries once, and refreshes proactively within 2 minutes of JWT `exp`. Failed refresh redirects to `/login?session=expired`.

### Step 9 (availability)

- **Guest:** Property detail → **Check dates**; book page shows live availability and blocks checkout when unavailable.
- **Host:** My listings → **Calendar** (`/host/listings/:id/availability`) — block dates; list shows booked/blocked periods.

### Step 10 (pricing)

Pick check-in/out on **Book** or property **Check dates** — total comes from `POST /api/pricing/calculate` (same logic as booking). Payment uses the booking `totalPrice` from the server.

### Step 11 (waitlist)

- **Guest:** Unavailable dates → **Join waitlist** on book or property checker; **My waitlist** in user menu (`/waitlist/my`).
- **Host:** Listing **Calendar** → waitlist queue for the From/To range.

### Step 12 (notifications)

- Sign in → header **bell** (`/notifications`) lists `GET /api/notifications/my`.
- Unread count uses local last-seen time; inbox refreshes on focus and every 60s.
- After book + pay (Kafka events), guest/host should see booking/payment rows (email may still fail in dev without SendGrid).

### Step 13 (edit listings)

- **Host:** My listings → **Edit** (`/host/listings/:id/edit`) — save title, price, amenities; **Unlist** hides from Explore.
- **Photos:** **Remove** on manage photos page; `PUT` syncs pricing `basePrice` + `minimumStay`.

Restart **property-service** after pulling backend changes (`PUT /api/properties/{id}`, `DELETE .../images`).

### Step 14 (trip detail)

- **Guest:** My trips → **Trip details** (`/bookings/:id`) — stay summary + payment status from `GET /api/payments/booking/{bookingId}`.
- Pending trips: **Complete payment** / **Cancel** from the detail page.

### Step 15 (refunds)

- Cancel a **CONFIRMED** paid trip on **Trip details**, then **Issue refund** (guest) or use **Reservations** as host.
- Policy: `docs/PAYMENT_REFUNDS.md`. Restart **payment-service** after backend auth changes.

### Step 16 (explore UX)

- **Property detail:** multi-photo gallery; **Location** map (OSM static image + Google Maps link) when lat/lng set.
- **Explore:** sort (newest / price) and pagination (9 per page) on current search results.

### Step 17 (social auth)

Set `VITE_GOOGLE_CLIENT_ID` / `VITE_GITHUB_CLIENT_ID` and matching secrets in repo `.env` — see `docs/OAUTH.md`. Login/register show social buttons when configured.

### Step 18 (admin)

Set `STAYSPHERE_BOOTSTRAP_ADMIN_EMAIL` in repo `.env`, restart auth-service, sign in again — see `docs/ADMIN.md`. Menu → **Admin**.

**Later steps:** production deploy (Phase 19), forgot password, date-range search on Explore (optional).
