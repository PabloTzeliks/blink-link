# ADR-005 — ClickHouse for Analytics Storage

| | |
|---|---|
| **Status** | ACCEPTED (replaces original ADR-006 on DynamoDB) |
| **Date** | 2026-05 |
| **Author** | PabloTzeliks |
| **Scope** | v4.2 |

## Context

Analytics click events require a storage model optimised for write throughput (one write per redirect) and OLAP-style reads (aggregations over time ranges, GROUP BY country, GROUP BY device). BlinkLink has an approximate read/write ratio of 1000:1 on the redirect path.

PostgreSQL is unsuitable for analytics at scale: aggregate queries over large click tables compete with the OLTP workload that serves redirects, creating I/O contention on the same instance. This violates the core architectural principle that the redirect path must never be blocked by secondary workloads.

DynamoDB (original plan) is a key-value store optimised for single-item lookups. Analytics access patterns require aggregations that DynamoDB cannot execute server-side, forcing expensive application-side aggregation. It is the wrong tool for the access pattern.

## Decision

Use **ClickHouse** as the exclusive storage layer for analytics click events. PostgreSQL stores no analytics data. ClickHouse is deployed as a separate instance from PostgreSQL, providing full workload isolation.

## Rationale

ClickHouse is a columnar OLAP database built for exactly this access pattern:

- Stores data by column, not by row — reads only the columns a query needs
- Compresses time-series data dramatically (repeated values per column)
- Executes GROUP BY and COUNT aggregations server-side with vectorised processing
- MergeTree engine is optimised for high-frequency inserts + range queries
- Used in production at this access pattern by Cloudflare, Uber, Spotify, Contentsquare

## Access Patterns

| ID | Pattern | Query |
|---|---|---|
| AP-1 | Total clicks for a URL | `SELECT COUNT(*) WHERE url_code = X` |
| AP-2 | Clicks per day over range | `GROUP BY toDate(clicked_at) WHERE url_code = X AND clicked_at BETWEEN A AND B` |
| AP-3 | Top countries | `GROUP BY country_code WHERE url_code = X` |
| AP-4 | Top devices | `GROUP BY device_type WHERE url_code = X` |

## Table Design

```sql
CREATE TABLE clicks (
    url_code        String,
    user_id         UUID,
    clicked_at      DateTime64(3, 'UTC'),
    country_code    Nullable(String),
    device_type     String,
    user_agent_hash String
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(clicked_at)
ORDER BY (url_code, clicked_at);
```

Partition by month: old partitions can be dropped by TTL without full table scan. Order by `(url_code, clicked_at)`: primary index matches AP-1 through AP-4.

## AWS Deployment

| Option | Description | Recommendation |
|---|---|---|
| Option A — EC2 + ClickHouse | Full control, no managed overhead, higher operational burden | Fallback if cost/control requirements change at v5 |
| Option B — Aiven for ClickHouse | Managed service, free tier available, automated backups and monitoring, minimal ops burden | Recommended for v4.2 |

## Trade-offs

| Gain | Cost |
|---|---|
| OLAP queries execute server-side, sub-300ms for 90 days of data | No native AWS managed service — requires Aiven or self-hosted EC2 |
| Workload isolation — analytics queries never impact redirect OLTP | ClickHouse SQL dialect differs from PostgreSQL — learning curve |
| Columnar compression — click data compresses 10-100x vs row storage | Separate instance to monitor, back up, and secure |
| No application-side aggregation required | Eventual consistency between Redis Streams and ClickHouse — acceptable for analytics |
