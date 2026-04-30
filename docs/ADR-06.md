# ADR-006 — DynamoDB for Analytics Storage

| | |
|---|---|
| **Status** | ACCEPTED |
| **Date** | 2026-03 |
| **Author** | PabloTzeliks |
| **Scope** | v4.2 — DO NOT implement in v4.1 |

## Context

Analytics click events require high write throughput (one write per redirect) and time-range reads (clicks per URL over N days). PostgreSQL cannot sustain 500 rows/second append with aggregate queries at scale.

## Decision

DynamoDB is the exclusive storage layer for analytics click events. PostgreSQL stores zero analytics data.

## Access Patterns (drive all key decisions)

| ID | Pattern |
|---|---|
| AP-1 | Total clicks for a URL — COUNT where urlCode = X |
| AP-2 | Clicks per day over date range — GROUP BY date WHERE urlCode = X AND date BETWEEN A AND B |
| AP-3 | Top countries for a URL — COUNT GROUP BY countryCode WHERE urlCode = X |
| AP-4 | Top devices for a URL — COUNT GROUP BY deviceType WHERE urlCode = X |

## Key Design

| Key | Value | Rationale |
|---|---|---|
| Partition Key (PK) | `urlCode` | All queries for a URL land in the same partition. Enables AP-1 through AP-4 without cross-partition scans. |
| Sort Key (SK) | `timestamp#eventId` (ISO-8601 UTC + UUID) | Enables range queries for AP-2. UUID suffix ensures uniqueness on same-millisecond clicks. |
| Attributes | `userId`, `countryCode` (nullable), `deviceType`, `userAgentHash` | All analytics dimensions stored inline. |

## ClickEvent Schema (FR-3.2)

```
urlCode       String  (PK)
timestamp     String  (ISO-8601 UTC, part of SK)
eventId       UUID    (part of SK)
userId        UUID
countryCode   String? (nullable in v4.2 — GeoIP strategy deferred)
deviceType    String
userAgentHash String
```

## Cross-User Isolation (NFR-4.4)

PK is `urlCode`, not `userId`. Ownership enforced in application layer before query. Cross-user access is architecturally impossible: `urlCode` uniqueness enforced by PostgreSQL UNIQUE constraint → `urlCode` is inherently user-scoped.

## Capacity Mode

On-demand. No provisioned throughput. DynamoDB auto-scales on burst. Review at v5 if sustained load warrants reserved capacity for cost.

## Trade-offs

| Gain | Cost |
|---|---|
| Write throughput scales to 500+/s without schema changes | Key design irreversible once data exists — must be correct before v4.2 ships |
| Time-range queries on SK are natively efficient | Aggregate queries (GROUP BY country) require application-side aggregation |
| On-demand mode eliminates capacity planning | countryCode is nullable in v4.2 — analytics queries must handle missing values |