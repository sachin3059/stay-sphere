# StaySphere — Improvement Plan (incremental on `main`)

We land **one phase per commit** on `main` so each step stays reviewable.

## Progress

| Phase | Focus | Status |
|-------|--------|--------|
| **0** | Security & hygiene | Done |
| **1** | Shared foundations (exceptions, validation, JWT in `common`, Flyway) | Done |
| **2** | Merge 9 → 6 services | Done |
| **3** | Booking & payment correctness | Done |
| **4** | Outbox & Kafka | Done |
| **5** | Postgres search (no ES) | Done |
| **6** | Saga / checkout | Done |
| **7** | Observability (correlation id, actuator) | Done |
| **8** | RBAC & token denylist | Done |
| **9** | Tests, CI, demo docs | **Done (this step)** |

See [adr/](adr/) for decisions per phase and [DEMO.md](DEMO.md) for a walkthrough.

**Note:** The branch `cursor/platform-improvement-roadmap` contains an all-in-one version of the full plan. We are **not** merging that branch; we rebuild the same ideas here step by step.
