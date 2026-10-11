# Extended features — build order and where code lives

We add capabilities **one phase at a time** (backend + web + docs). Use this as a map of **what exists today** vs **what we are adding**.

## Platform map (already shipped)

| Concern | Service | Port | Key paths |
|--------|---------|------|-----------|
| Auth (JWT, OAuth, refresh) | `auth-service` | 8081 | `/api/auth/*` |
| Listings, search, pricing, availability | `property-service` | 8082 | `/api/properties/*`, `/api/pricing/*`, `/api/availability/*` |
| Bookings + waitlist | `booking-service` | 8083 | `/api/bookings/*`, `/api/waitlist/*` |
| Stripe + webhooks | `payment-service` | 8084 | `/api/payments/*` |
| In-app + email notifications | `notification-service` | 8087 | `/api/notifications/*` (Kafka-driven email) |
| Entry + rate limit | `api-gateway` | 8080 | Routes in `application.yml`, public list in `GatewayPublicRouteMatcher` |
| Shared errors, JWT, Redis cache helpers | `common` | — | `GlobalExceptionHandler`, `JwtUtil`, `RedisJsonCache` |

Frontend: `frontend/src` — pages under `pages/`, API clients under `features/*/api.ts`, routes in `routes/router.tsx`.

---

## Phase F1 — Forgot password / reset ✅ (this doc’s first slice)

**Goal:** Email/password users can recover access without SQL.

| Layer | Location |
|-------|----------|
| DB | `auth-service/.../db/migration/V3__password_reset_tokens.sql` |
| Entity / repo | `PasswordResetToken`, `PasswordResetTokenRepository` |
| Logic | `PasswordResetService` — token hash (SHA-256), 1h TTL, single use, revokes refresh tokens on reset |
| Email | `AuthEmailService` — SMTP via Amazon SES (same env as notification); dev logs link when SMTP unset |
| API | `POST /api/auth/forgot-password`, `POST /api/auth/reset-password` |
| Gateway | Public routes in `GatewayPublicRouteMatcher` |
| Web | `ForgotPasswordPage`, `ResetPasswordPage`, `features/auth/api.ts` |

**Flow:** User submits email → opaque token stored hashed → email with link `{FRONTEND_BASE_URL}/reset-password?token=...` → user sets new password → all refresh tokens revoked.

OAuth-only accounts (no password) are ignored silently; API still returns success (no email enumeration).

---

## Phase F2 — Email verification ✅

| Layer | Location |
|-------|----------|
| DB | `V4__email_verification.sql` — `users.email_verified_at`, `email_verification_tokens` |
| API | `POST /api/auth/verify-email`, `POST /api/auth/resend-verification` (JWT) |
| JWT | `emailVerified` claim in `JwtUtil`; booking create checks it |
| Email | `AuthEmailService.sendVerificationEmail` |
| Web | `/check-email`, `/verify-email?token=…`, banner, profile status |

---

## Phase F3 — Explore search by dates ✅

| Layer | Location |
|-------|----------|
| API | Optional `checkIn` / `checkOut` on `GET /api/properties`, `/search`, `/search/advanced` |
| SQL | `NOT EXISTS` on `blocked_dates` (advanced search + list-all-with-dates); JPQL search post-filter |
| Cache | `PropertyCacheSupport.searchKey` includes dates; `AvailabilityService` bumps search gen on block |
| Web | `StaySearchBar` dates, Explore URL params, property links carry dates |

---

## Phase F4 — API versioning (planned)

| Layer | Planned location |
|-------|------------------|
| Gateway | Route `/api/v1/**` → same services; deprecate unversioned paths later |
| Services | Optional `@RequestMapping("/api/v1/auth")` or gateway strip-prefix |

**Today:** All routes are `/api/...` without version prefix.

---

## Phase F5 — Server-side sort + pagination ✅

| Layer | Location |
|-------|----------|
| API | `page` (0-based), `size` (max 50), `sort` on search + list endpoints |
| Response | `PropertyPageResponse` (`items`, `totalElements`, `totalPages`, …) |
| DB | `PropertySearchRepositoryImpl` — `LIMIT`/`OFFSET` + whitelisted `ORDER BY` |
| Cache | Search keys include page, size, sort |
| Web | Explore uses server totals; client `sortProperties` removed |

---

## Phase F6 — Stripe Connect / host payouts (planned)

| Layer | Planned location |
|-------|------------------|
| payment-service | Connect account onboarding, application fees, transfer on `payment_intent.succeeded` |
| booking/property | Store `stripeConnectAccountId` on host user or listing |
| Web | Host “Payouts” settings |

**Today:** Single platform Stripe account; hosts are not paid out automatically.

---

## Phase F7 — Stronger geo search (planned)

| Layer | Planned location |
|-------|------------------|
| DB | PostGIS or `earth_distance` / bounding box in `PropertySearchRepository` |
| API | `lat`, `lng`, `radiusKm` query params |
| ADR | Update `006-search-in-postgres.md` if we add PostGIS |

**Today:** Lat/lon on listings + map on detail; no radius filter on search.

---

Update `docs/APPLICATION_ROADMAP.md` checkboxes when each phase ships.
