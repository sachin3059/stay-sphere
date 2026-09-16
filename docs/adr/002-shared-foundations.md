# ADR 002: Shared foundations (`common`)

## Decision

- Shared API errors: typed exceptions (`BadRequestException`, `ConflictException`, `ResourceNotFoundException`) and one `GlobalExceptionHandler` in `common`.
- Shared JWT parsing: `JwtUtil` + `JwtAuthFilter` in `common`; each service scans `com.staysphere.common` (gateway keeps its own reactive JWT helper).
- Schema policy: `ddl-auto: validate` by default; `local` profile uses `update` for developer ergonomics.
- Flyway: per-service history table + `V1__baseline` placeholder; real DDL migrations added in later phases.

## Alternatives considered

- Copy-paste JWT and handlers per service — rejected (drift risk).
- Hibernate-only schema with no Flyway — rejected (no versioned migrations path).
