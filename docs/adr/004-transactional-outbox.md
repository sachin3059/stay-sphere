# ADR 004: Transactional outbox

## Decision

Write domain events to an `outbox` table in the same transaction as aggregate changes. A poller publishes to Kafka with `FOR UPDATE SKIP LOCKED`. Consumers are idempotent.

## Alternatives

- `kafkaTemplate.send` inside `@Transactional` — dual-write risk.
- Redis Streams only — valid at small scale; Kafka kept for interview/demo fan-out.

## Industry

Transactional outbox is the standard fix for reliable event publication from microservices.
