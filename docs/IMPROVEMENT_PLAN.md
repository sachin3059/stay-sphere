# StaySphere — Improvement Plan (incremental on `main`)

We land **one phase per commit** on `main` so each step stays reviewable.

## Progress

| Phase | Focus | Status |
|-------|--------|--------|
| **0** | Security & hygiene | Done |
| **1** | Shared foundations (exceptions, validation, JWT in `common`, Flyway) | Done |
| **2** | Merge 9 → 6 services | Done |
| **3** | Booking & payment correctness | **Done (this step)** |
| 4 | Outbox & Kafka | Pending |
| 5 | Postgres search (no ES) | Pending |
| 6 | Saga / checkout | Pending |
| 7–9 | Observability, RBAC, tests & CI | Pending |

See [adr/](adr/) for decisions per phase.

**Note:** The branch `cursor/platform-improvement-roadmap` contains an all-in-one version of the full plan. We are **not** merging that branch; we rebuild the same ideas here step by step.
