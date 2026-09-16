# ADR 005: Search in Postgres

## Decision

Property search uses Postgres FTS (`tsvector`), `pg_trgm`, and PostGIS (or earthdistance) for geo. Elasticsearch removed from compose and property-service.

## Alternatives

- Elasticsearch — better at huge scale and heavy faceting; unnecessary for current catalog size.

## Industry

Many products use Postgres FTS until search becomes a dedicated scaling concern.
