# ADR 009: RBAC and token denylist

## Decision

- Enforce `@PreAuthorize("hasRole('HOST')")` on property create and image upload endpoints.
- On logout, revoke the refresh token and add the access token to a Redis denylist (`deny:access:<token>`) for the remaining JWT TTL.
- `JwtAuthFilter` rejects denied tokens on every service that shares `common` and connects to Redis.

## Rationale

Registration remains GUEST-only (phase 0); HOST is required for listing management. Denylist gives immediate access-token invalidation after logout without waiting for expiry.
