# ADR 003: Service boundaries (six deployables)

## Decision

Reduce nine Spring Boot apps to **six** deployables:

| Service | Responsibilities | Port |
|---------|------------------|------|
| api-gateway | Routing, JWT gate | 8080 |
| auth-service | Users, tokens | 8081 |
| property-service | Listings, **availability**, **pricing** | 8082 |
| booking-service | Reservations, **waitlist** | 8083 |
| payment-service | Payments | 8084 |
| notification-service | Email / events | 8087 |

Public API paths `/api/pricing/**` and `/api/availability/**` route to **property-service**. `/api/waitlist/**` routes to **booking-service**.

## Rationale

Availability and pricing are always loaded with property context; waitlist is tightly coupled to booking lifecycle. Fewer JVMs simplify local Docker and deployment while keeping clear package boundaries inside each JAR.

## Alternatives considered

- Keep nine services — rejected (operational overhead for little isolation gain at this scale).
- Single monolith — rejected (loses independent scaling of payments/notifications).
