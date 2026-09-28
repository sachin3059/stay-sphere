# ADR 010 — Caching, Bloom filter, gateway rate limits

## Context

Explore and property detail are read-heavy. Invalid property IDs can bypass a naive cache-aside layer and hit Postgres (cache penetration). Availability reads were uncached; invalidation used `KEYS`.

## Decision

1. **Shared `RedisJsonCache`** in `common`: JSON values, TTL jitter, short lock on miss.
2. **Property detail + search** cached in `property-service`; catalog mutations bump search version and evict detail.
3. **Guava Bloom filter** of active property IDs on `GET` by id; rebuild when a listing is unlisted.
4. **Availability** keys include a per-property version (`INCR availability:ver:{id}`) instead of scanning Redis.
5. **Spring Cloud Gateway `RequestRateLimiter`** backed by reactive Redis.

## Consequences

- Redis is required for gateway and property-service in production-like runs.
- Bloom false positives still reach the DB; false negatives are avoided by rebuilding on deactivate.
- Search results may be briefly stale (TTL ~90s).
