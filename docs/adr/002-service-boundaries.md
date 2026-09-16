# ADR 002: Service boundaries

## Decision

Six deployables: auth, property (listings + calendar + pricing), booking (reservations + waitlist), payment, notification, api-gateway, plus `common`.

Payment and notification stay separate for isolation and fan-out. Calendar and pricing merge into property; waitlist merges into booking.

## Alternatives

- Nine microservices — too much split-brain for calendar vs booking.
- Monolith — weaker interview story for event-driven design.

## Industry

Teams often consolidate wrongly split services ("macroservices" per domain).
