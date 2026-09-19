# StaySphere — Application build roadmap (sequenced)

Single ordered list of what we are building: **guest booking marketplace** + **host listings** + **payments**, on the API gateway (`:8080`) and React web app (`:5173`).

Use this as the **default order of work** (one slice at a time, commit when stable).

---

## Part A — Completed (do not re-do unless fixing bugs)

| # | Slice | Delivered |
|---|--------|-----------|
| A1 | **Platform backend** | Microservices, Flyway, booking overlap, Redis lock (release after create), Stripe intents, Kafka confirm saga, Postgres search, CORS, become-host API, E2E script |
| A2 | **Web foundation** | Vite/React/Tailwind, layout, API client, routing, env |
| A3 | **Auth (email)** | Register, login, logout, JWT in Zustand, become host |
| A4 | **Listings (guest)** | Explore, advanced search, property detail |
| A5 | **Listings (host)** | Create property, pricing rule, my listings |
| A6 | **Booking + Stripe** | Book flow, PaymentElement, my trips, cancel pending, 3DS return route |
| A7 | **Host ops** | Reservations dashboard, resume payment on pending trips |
| A8 | **Photos** | Cloudinary upload on create + manage photos per listing |
| A9 | **Session reliability** | 401 → refresh + retry, proactive refresh, session-expired login message |
| A10 | **Availability** | Check before book, detail date checker, host block calendar |
| A11 | **Pricing transparency** | Server quote on book + property date checker |
| A12 | **Waitlist** | Join when unavailable, my waitlist, host queue view |
| A13 | **Notifications** | Inbox, header bell, deep links to trips / waitlist / host reservations |
| A14 | **Listing edits** | PUT property, edit page, unlist, remove photos, pricing rule update |
| A15 | **Trip detail** | `/bookings/:id` with booking + payment receipt block |
| A16 | **Refunds** | Policy docs, host/guest refund UI, webhook idempotency |
| A17 | **Explore UX** | Photo gallery, map on detail, sort + client pagination |
| A18 | **Social auth** | Google + GitHub OAuth, profile page, email linking |
| A19 | **Admin & operations** | Admin APIs, bootstrap email, `/admin` UI, health links (`docs/ADMIN.md`) |

---

## Part B — Remaining work (build in this order)

### Phase 19 — Production deployment ← **current**

**Goal:** Runnable outside localhost. Guide: **`docs/DEPLOYMENT.md`**.

| # | Change | Layer | Status |
|---|--------|--------|--------|
| 19.1 | Frontend production build + hosting | Ops | Docker `web` (nginx + SPA), `frontend/Dockerfile` |
| 19.2 | Env matrix: gateway CORS, JWT, Stripe, Cloudinary | Ops | `env.template`, `DEPLOYMENT.md` |
| 19.3 | Stripe webhooks on public URL | Ops | Documented; configure on your host |
| 19.4 | CI: backend tests + frontend build | CI | `.github/workflows/ci.yml` |
| 19.5 | Rate limiting / API versioning | Backend | Optional backlog |
| 19.6 | Real email (SendGrid) | Backend | Wired; skips send when key unset |

**Acceptance:** Staging URL completes register → book → pay → confirm (you run deploy + Stripe webhook on your domain).

---

## Part C — Application capabilities map (everything we are building)

Use this as the **feature checklist** (✓ = shipped in Part A).

### Guest

- [x] Discover stays (Explore, search)
- [x] View listing detail
- [x] Register / login (email)
- [x] Book dates + guests
- [x] Pay with Stripe (test)
- [x] My trips (view, cancel pending, resume pay)
- [x] Waitlist when unavailable
- [x] Notifications
- [x] Trip/payment receipt detail
- [x] Social login (Google & GitHub)

### Host

- [x] Become host
- [x] Create listing + pricing
- [x] Upload photos (Cloudinary)
- [x] View reservations across listings
- [x] Block calendar dates (host); availability check on book
- [x] Edit / archive listing
- [x] Remove photos
- [x] Waitlist view per property (host calendar)
- [x] Refunds (if policy allows)

### Platform

- [x] API gateway + JWT
- [x] Booking idempotency + overlap DB constraint
- [x] Payment intents + confirm sync
- [x] Event-driven booking confirm
- [x] Token refresh in web client
- [ ] Webhooks in deployed env (configure per `DEPLOYMENT.md`)
- [x] Admin (users, listing moderation)
- [ ] Email / deploy webhooks (Phase 19)

---

## How to use this doc

1. Pick the **next open phase** in Part B (currently **Phase 19**).
2. Implement all rows in that phase (or agree to split a phase across two PRs).
3. Update checkboxes in Part C when a capability ships.
4. Keep `frontend/README.md` “Build order” in sync with the phase number for day-to-day dev.

**Current focus:** **Phase 19 — Production deploy**.

**Deferred from Phase 17:** forgot password / email verification (SendGrid); ChatGPT sign-in.

**Deferred (optional):** Explore date-range search (needs search API + availability); server-side sort/pagination when listings exceed client page size.

---

## Todo / backlog (not scheduled — pick when needed)

| Item | Layer | Notes |
|------|--------|--------|
| **Host map pin + address autocomplete** | Frontend (+ env) | Save real `latitude` / `longitude` on create/edit listing. Options: **Google Places + Maps** (`GOOGLE_MAPS_API_KEY`), **Ola Maps** (developer.olamaps.io), or **OSS** (Leaflet + Photon/Nominatim, no key). Today create flow still uses default Pune coords; detail page only *displays* map if lat/lng exist. |
| **Marketplace payouts** | Backend + Stripe Connect | Platform fee + pay host (deferred; single Stripe account for MVP). |
