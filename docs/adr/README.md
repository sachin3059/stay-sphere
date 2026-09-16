# Architecture decision records

| ADR | Title |
|-----|--------|
| [001](001-secrets-and-profiles.md) | Secrets and Spring profiles |
| [002](002-shared-foundations.md) | Shared `common` module, Flyway baseline |
| [003](003-service-boundaries.md) | Merge to six deployable services |
| [004](004-booking-overlap.md) | Overlap constraint, locks, server-side price |
| [005](005-transactional-outbox.md) | Transactional outbox and idempotent consumers |
| [006](006-search-in-postgres.md) | Postgres FTS; no Elasticsearch |
| [007](007-checkout-saga.md) | Payment success → booking confirmation |
| [008](008-observability.md) | Correlation id and Actuator health |
| [009](009-rbac-and-denylist.md) | HOST RBAC and Redis token denylist |
| [010](010-tests-and-ci.md) | GitHub Actions and unit tests |
