# ADR 006: Search in PostgreSQL

## Decision

- Full-text search uses a generated `tsvector` column on `properties` plus `pg_trgm` for fuzzy title match.
- Public API: `GET /api/properties/search/advanced` with optional `query`, filters, and ranking via `ts_rank`.
- Remove Elasticsearch from the stack (dependency, compose service, reindex endpoints).

## Alternatives

- Keep Elasticsearch for geo/radius — deferred; basic filters remain in SQL.
- External OpenSearch — same operational cost as ES for this project size.

## Industry

Postgres FTS + GIN is sufficient for catalog search at moderate scale; avoids a second search cluster.
