# ADR-004 — URL Lifecycle and Async Purge Engine

| | |
|---|---|
| **Status** | ACCEPTED |
| **Date** | v3.0.0 |
| **Author** | PabloTzeliks |

## Context

URL expiration is plan-driven (7 days FREE, 1 year VIP, 10 years ENTERPRISE). Expired links must be deleted without blocking traffic or causing table-level lock contention. Multiple app instances may run simultaneously.

## Decision

- **Strategy Pattern** (`ExpirationCalculationStrategy`) computes TTL at creation time and stores as `expiration_date`.
- `PurgeUrlsUseCase` runs in batches with configurable size and sleep interval.
- PostgreSQL native DELETE with `FOR UPDATE SKIP LOCKED` for safe concurrent purge across instances.
- `ExpiredUrlCleanUpScheduler` triggers via cron (default: `0 0 3 * * *`).

## Implementation Detail

```sql
DELETE FROM urls
WHERE id IN (
    SELECT id FROM urls
    WHERE expiration_date < :refTime
    ORDER BY id
    FOR UPDATE SKIP LOCKED
    LIMIT :batchSize
)
```

`SKIP LOCKED` means: if another instance is deleting a row, skip it — don't wait. Prevents deadlocks and serialisation under concurrent cleanup.

## v4.1 Addition

`PurgeUrlsUseCase` must also evict the Redis key `blinklink:url:{code}` on hard-delete (FR-1.3). Add `CachePort.evict(code)` call after batch delete.

## Configuration (application.yml)

```yaml
app.job.purge-urls:
  batch-size: 5000
  sleep-millis: 100
  cron: "0 0 3 * * *"
```

## Consequences

- ✅ TTL rules isolated and independently testable per plan
- ✅ Concurrent purge across instances with no deadlock risk
- ✅ DB load controlled via batch size + sleep
- ❌ Scheduler tuning required (batch size, sleep, cron) — operational burden
- ❌ v4.1: purge must also evict Redis or stale cached URLs will redirect after deletion