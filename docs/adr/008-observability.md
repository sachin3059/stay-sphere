# ADR 008: Request observability

## Decision

- Propagate `X-Correlation-Id` at the API gateway and in each servlet service via `CorrelationIdFilter` in `common`.
- Put correlation id in SLF4J MDC (`correlationId`) and echo it on the response.
- Expose Spring Boot Actuator `health` and `info` from shared `common` config; permit `/actuator/**` on service security chains.

## Rationale

Supports log correlation across microservices without a full tracing backend in local dev.
