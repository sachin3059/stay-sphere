# Caching, Bloom filter, and rate limiting (Phase 20)

## Property reads (`property-service`)

| Layer | Key pattern | TTL | Invalidation |
|-------|-------------|-----|--------------|
| **Bloom filter** | In-memory Guava filter of active property IDs | Rebuild on startup; `put` on create/activate; rebuild on unlist | Stops junk `GET /api/properties/{id}` from hitting Postgres |
| **Detail cache** | `prop:detail:{id}` | ~10 min + jitter | Host edit, photos, status change |
| **Search cache** | `prop:search:{sha256(...)}` includes `prop:search:ver` | ~90s + jitter | `INCR prop:search:ver` on catalog changes |

`GET /api/properties/{id}` flow: Bloom → Redis detail → Postgres.

## Availability (`property-service`)

| Key | TTL | Invalidation |
|-----|-----|--------------|
| `availability:{propertyId}:v{ver}:{from}:{to}` | ~3 min + jitter | `INCR availability:ver:{propertyId}` on booking/block events |

No `KEYS` scans in Redis.

## Stampede control (`common`)

`RedisJsonCache#getOrLoad` uses a short Redis lock (`cache:lock:{key}`) and TTL jitter so hot keys do not all expire at once.

## API gateway rate limiting

Spring Cloud Gateway `RequestRateLimiter` (token bucket in Redis).

- Keys: `auth:{ip}` for `/api/auth/**`, else `user:{ip}` or `ip:{ip}`.
- Defaults: `GATEWAY_RATE_LIMIT_REPLENISH=80`, `GATEWAY_RATE_LIMIT_BURST=160` (per second replenish is per route key semantics in SCG — tune in staging).

Returns **429** when exceeded.

## Env (optional)

```env
GATEWAY_RATE_LIMIT_REPLENISH=80
GATEWAY_RATE_LIMIT_BURST=160
SPRING_DATA_REDIS_HOST=redis
```

Gateway and property-service require Redis when these features are enabled.
