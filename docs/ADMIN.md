# Admin & operations (Phase 18)

Platform operators use the **ADMIN** role to manage users and listing visibility.

## Bootstrap the first admin

1. Register (or sign in with GitHub/Google) using the email you want as admin.
2. In repo-root `.env` set:

   ```env
   STAYSPHERE_BOOTSTRAP_ADMIN_EMAIL=you@example.com
   ```

3. Restart **auth-service** (or the full stack). On startup, that user is promoted to `ADMIN`.
4. **Sign out and sign in again** so your JWT includes `role: ADMIN`.

You can also promote users from **Admin → Users** once any admin exists.

Public registration never creates `ADMIN` accounts.

## Admin UI (web)

| URL | Purpose |
|-----|---------|
| `/admin` | Dashboard + health links |
| `/admin/users` | List users, change role (GUEST / HOST / ADMIN) |
| `/admin/listings` | Set listing status (ACTIVE / INACTIVE / UNDER_REVIEW) |

Only `ACTIVE` listings appear in guest search and Explore.

## Admin API (via gateway `:8080`)

Requires `Authorization: Bearer <access_token>` for an **ADMIN** user.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/admin/users` | List users |
| PATCH | `/api/admin/users/{userId}/role` | Body: `{ "role": "HOST" }` |
| GET | `/api/admin/properties` | All listings (any status) |
| PATCH | `/api/admin/properties/{id}/status` | Body: `{ "status": "UNDER_REVIEW" }` |

## Service health (no auth)

Through the gateway:

- `GET /api/auth/health`
- `GET /api/properties/health`
- `GET /api/bookings/health`
- `GET /api/payments/health`
- `GET /api/notifications/health`

The admin dashboard links to these for quick ops checks.
