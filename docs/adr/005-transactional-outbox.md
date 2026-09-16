# ADR 005: Transactional outbox

## Decision

Write domain events to an outbox table in the **same database transaction** as aggregate changes. A scheduled poller publishes to Kafka using `FOR UPDATE SKIP LOCKED`. Consumers deduplicate with a `processed_events` table keyed by `eventId`.

## Alternatives

- `kafkaTemplate.send` inside `@Transactional` — dual-write risk if the DB commits and Kafka fails (or vice versa).
- Redis Streams only — valid at small scale; Kafka kept for fan-out to notification and waitlist.

## Industry

Transactional outbox is the standard pattern for reliable event publication from microservices.
