# ADR 001: Secrets and profiles

## Decision

- Non-local runs require `JWT_SECRET` and third-party API keys via environment variables.
- One repo-root **`.env`** feeds every service (Docker Compose `env_file` and `application-local.yml` import).
- No per-service `.env` files or separate template files.

## Alternatives considered

- Hardcoded defaults in `application.yml` — rejected (leak risk).
- Vault-only — deferred; env + compose is enough for this project.

## Industry

Fail-fast configuration and no secrets in VCS are standard in production deployments.
