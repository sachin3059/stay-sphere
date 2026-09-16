# ADR 010: Tests and CI

## Decision

- GitHub Actions workflow runs `./gradlew build` on push/PR to `main`.
- Start with a focused unit test in `booking-service` for date overlap logic; expand with Testcontainers later.

## Rationale

Keeps CI fast while the monorepo grows; `build` compiles all six services and runs existing tests.
