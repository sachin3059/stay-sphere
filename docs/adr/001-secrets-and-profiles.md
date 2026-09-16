# ADR 001: Secrets and profiles

## Decision

- Non-local runs require `JWT_SECRET` and third-party API keys via environment variables.
- `application-local.yml` may supply dev-only placeholders when `spring.profiles.active=local`.
- `.env` is gitignored; `.env.example` documents required variables.

## Alternatives considered

- Hardcoded defaults in `application.yml` — rejected (leak risk).
- Vault-only — deferred; env + compose is enough for this project.

## Industry

Fail-fast configuration and no secrets in VCS are standard in production deployments.
