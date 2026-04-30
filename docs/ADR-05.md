# ADR-005 — Redis as Distributed Sequence Server for ID Generation

| | |
|---|---|
| **Status** | ACCEPTED |
| **Date** | 2026-03 |
| **Author** | PabloTzeliks |
| **Scope** | v4.1 |

## Context

v3.0.0 generates IDs via PostgreSQL `nextval('urls_id_seq')`. Under horizontal scaling, every ID generation call serialises through PostgreSQL, adding latency to the URL creation path.

## Decision

Replace `PostgreSQL nextval()` with `Redis INCR` as primary ID generation. Redis acts as a dedicated sequence server, not a cache.

## Port Contract

| Component | Responsibility |
|---|---|
| `SequencePort` (application layer) | `nextId() → Long` — only interface use case calls |
| `RedisSequenceAdapter` (infrastructure) | Executes `INCR sequence:url:id` |
| `SequenceInitializer` (infrastructure) | Startup sync: PostgreSQL MAX(id) → Redis SET NX |
| `ShortenUrlUseCase` | Calls `SequencePort.nextId()`; handles UNIQUE violation with retry (max 3x) |

## Initialization Protocol (on every startup)

1. Query PostgreSQL: `SELECT MAX(id) FROM urls`
2. `SET sequence:url:id {max_id} NX` — only if key does not exist
3. If key already exists: leave it — another instance already initialized
4. Begin serving traffic via `INCR sequence:url:id`

PostgreSQL = source of truth for init. Redis = live counter during operation.

## Failure Contract (Redis restart scenario)

1. Redis generates ID N → returns to application
2. Before INSERT to PostgreSQL, Redis restarts
3. Redis re-initializes from PostgreSQL MAX(id) = N-1
4. Redis re-issues ID N to next request
5. **Resolution:** PostgreSQL UNIQUE on `short_code` fires → `ShortenUrlUseCase` catches → retry with new ID

This transforms silent data corruption into a handled exception.

## Trade-offs

| Gain | Cost |
|---|---|
| ID generation scales horizontally | Redis becomes runtime dependency for URL creation (not for redirects) |
| Sub-millisecond ID generation | Startup init logic adds complexity; must be tested |
| Decouples sequence from relational writes | Rare ID collision on Redis restart requires retry in use case |

## Key

`sequence:url:id` — no TTL, no PII, integer only.