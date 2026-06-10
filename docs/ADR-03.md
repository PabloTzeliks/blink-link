# ADR-003 — Redis as Distributed Sequence Server for ID Generation

| | |
|---|---|
| **Status** | ACCEPTED (unchanged) |
| **Date** | 2026-03 |
| **Author** | PabloTzeliks |
| **Scope** | v4.1 (DELIVERED) |

## Decision

Replace PostgreSQL `nextval()` with Redis `INCR` as the primary ID generation mechanism. Redis operates as a dedicated sequence server, not a cache.

## Initialization Protocol

1. On startup: query PostgreSQL `MAX(id)` from `urls` table
2. Call Redis `SET sequence:url:id {max_id} NX`
3. If key already exists, skip — another instance initialized first
4. All subsequent IDs via Redis `INCR sequence:url:id`

PostgreSQL = source of truth for init. Redis = live counter during operation.

## Failure Contract

Redis restarts → re-initializes from PostgreSQL `MAX(id)` → may re-issue an ID.

Resolution: PostgreSQL UNIQUE constraint on `short_code` is the final arbiter. `ShortenUrlUseCase` catches `DataIntegrityViolationException` and retries (max 3x).

## Startup Degradation (Implemented in v4.1)

If Redis is unavailable at startup, `SequenceInitializer` logs a WARN and continues. The application starts without the sequence initialized. URL creation returns 503 until Redis recovers. Redirects are unaffected.

## Port Contract

| Component | Responsibility |
|---|---|
| `SequencePort` (application) | `nextId() : Long` |
| `RedisSequenceAdapter` (infrastructure) | `INCR` on Redis |
| `SequenceInitializer` (infrastructure) | Startup sync from PostgreSQL `MAX(id)` |

## Trade-offs

| Gain | Cost |
|---|---|
| Sub-millisecond ID generation, no PostgreSQL serialisation point | Redis is a runtime dependency for URL creation (not for redirects) |
| Horizontal scaling without sequence contention | Rare collision on Redis restart requires retry logic in use case |
| Decouples sequence generation from relational writes | Startup initialization logic adds complexity and must be tested |

## Key

`sequence:url:id` — no TTL, no PII, integer only.
