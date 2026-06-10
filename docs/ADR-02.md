# ADR-002 — Revised Phased Rollout; Kafka and DynamoDB Replaced

| | |
|---|---|
| **Status** | REVISED (original: ACCEPTED 2026-03) |
| **Date** | 2026-05 |
| **Author** | PabloTzeliks |
| **Scope** | v4.1 → v4.4 |

## Original Plan

- v4.1 — Redis + ElastiCache
- v4.2 — Kafka (MSK) + DynamoDB
- v4.3 — Analytics read API

## Problems Identified with Original Plan

Kafka inside a monolith solves the wrong problem. Kafka's value is decoupling between physically separate services. Inside the same JAR, an `ApplicationEventPublisher` with a dedicated thread pool achieves the same async fan-out without the operational cost of a broker. The original plan introduced Kafka's full operational complexity — broker management, topic configuration, consumer group rebalancing, MSK on AWS — to solve a problem that Spring Modulith events and Redis Streams resolve at a fraction of the cost.

DynamoDB is a key-value store optimised for single-item lookups by primary key. Analytics access patterns (aggregations over time ranges, GROUP BY country, GROUP BY device) are OLAP queries, not key-value lookups. DynamoDB forces application-side aggregation for every analytics query, which is expensive and complex. The tool was chosen for write throughput — a problem that does not exist at BlinkLink's current scale.

URL shorteners have a read/write ratio of approximately 1000:1. Every technology decision must protect the redirect path above all else. Neither Kafka nor DynamoDB meaningfully improves the redirect path.

## Revised Phased Plan

| Phase | Technology | Feature Delivered | Done When |
|---|---|---|---|
| **v4.1 — IN PROGRESS** | Redis + ElastiCache | Cache-aside on redirect · Rate limiting · Custom short codes | ElastiCache deployed, redirect p95 < 20ms; rate limiting still in development |
| **v4.2** | Spring Modulith + Redis Streams + ClickHouse | Adopt Spring Modulith · Redis Streams for click events · ClickHouse storage | Click events confirmed in ClickHouse, redirect never blocks |
| **v4.3** | Analytics read API | `GET /api/v3/urls/{code}/stats` · Timeseries endpoint · Plan-gated | Queries return correct aggregations from ClickHouse < 300ms |
| **v4.4** | AWS full deployment + observability | RDS · ElastiCache · Aiven ClickHouse · Micrometer · Tracing | All services deployed, p95 redirect < 20ms in production |

> Status note (2026-05): cache-aside, Redis sequence (ADR-003) and custom short codes (ADR-004) are delivered. **Rate limiting is still IN DEVELOPMENT** (no `RateLimitPort`/adapter wired yet) and the redirect cache is mid-refactor toward a richer `UrlContext` payload.

## Technology Replacements

| Removed | Replaced By | Decision Reference |
|---|---|---|
| Amazon MSK (Kafka) | Redis Streams | ADR-006 |
| Amazon DynamoDB | ClickHouse (Aiven) | ADR-005 |
