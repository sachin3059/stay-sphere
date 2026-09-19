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

---

## Part B — Remaining work (build in this order)

### Phase 16 — Explore & discovery upgrades ← **current**

**Goal:** Richer search and listing presentation.

| # | Change | Layer | Notes |
|---|--------|--------|--------|
| 16.1 | Property image gallery on detail (all `imageUrls`) | Frontend | |
| 16.2 | Map / coordinates on detail (lat/lng already on property) | Frontend | Optional map provider |
| 16.3 | Date-range search on Explore | Backend + Frontend | If product requires; may extend search API |
| 16.4 | Sort (price, newest) and pagination | Backend + Frontend | If result sets grow |

**Acceptance:** Better browse experience without changing core book flow.

---

### Phase 17 — Account & social auth (later enhancement)

**Goal:** Faster signup and account linking (not required for MVP).

| # | Change | Layer | Notes |
|---|--------|--------|--------|
| 17.1 | OAuth2/OIDC — Google | Backend + Frontend | Spring OAuth2 client, callback URLs |
| 17.2 | OAuth2 — GitHub | Backend + Frontend | Same pattern |
| 17.3 | Link OAuth identity to existing email account | Backend | Conflict rules |
| 17.4 | Sign in with ChatGPT | Partner only | Only if OpenAI approves app |
| 17.5 | Profile page (name, email read-only) | Frontend | Extend when profile APIs exist |
| 17.6 | Forgot password / email verification | Backend + Frontend | SendGrid |

**Acceptance:** “Continue with Google” issues same JWT contract as email login.

---

### Phase 18 — Admin & operations

**Goal:** Platform operator tools (optional for demo MVP).

| # | Change | Layer | Notes |
|---|--------|--------|--------|
| 18.1 | ADMIN role assignment (not public register) | Backend | Already restricted; needs admin API |
| 18.2 | Admin UI: users, listings moderation | Frontend | |
| 18.3 | Actuator/health dashboard link for ops | Docs/ops | |

---

### Phase 19 — Production deployment

**Goal:** Runnable outside localhost.

| # | Change | Layer | Notes |
|---|--------|--------|--------|
| 19.1 | Frontend production build + hosting | Ops | `npm run build`, CDN or static behind gateway |
| 19.2 | Env matrix: gateway CORS, JWT, Stripe, Cloudinary | Ops | Secrets not in git |
| 19.3 | Stripe webhooks on public URL | Ops | |
| 19.4 | CI: backend tests + frontend build | CI | Extend existing pipeline |
| 19.5 | Rate limiting / API versioning | Backend | Per IMPROVEMENT_PLAN optional items |
| 19.6 | Real email (SendGrid) for transactional mail | Backend | |

**Acceptance:** Staging URL completes register → book → pay → confirm.

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
- [ ] Social login (later)

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
- [ ] Webhooks in deployed env
- [ ] Admin / email / OAuth (later)

---

## How to use this doc

1. Pick the **next open phase** in Part B (currently **Phase 16**).
2. Implement all rows in that phase (or agree to split a phase across two PRs).
3. Update checkboxes in Part C when a capability ships.
4. Keep `frontend/README.md` “Build order” in sync with the phase number for day-to-day dev.

**Current focus:** **Phase 16 — Explore upgrades**, then **Phase 17 — OAuth**.
