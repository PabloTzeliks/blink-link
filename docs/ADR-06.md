# ADR-006 — Redis Streams for Click Event Delivery (Replaces Kafka)

| | |
|---|---|
| **Status** | ACCEPTED |
| **Date** | 2026-05 |
| **Author** | PabloTzeliks |
| **Scope** | v4.2 |

## Context

Click events must be delivered from the redirect path to the analytics storage (ClickHouse) asynchronously. The redirect must return 302 before the analytics write completes. The original plan used Kafka (MSK on AWS) for this delivery.

## Kafka Rejection Rationale

Kafka inside a monolith delivers the operational complexity of a distributed system without its core benefit: decoupling between physically separate services. Within a single JAR, an `ApplicationEventPublisher` with a dedicated thread pool achieves the same async fan-out.

Kafka's genuine advantages — long retention, compacted topics, exactly-once semantics, replay from arbitrary offset — are not required at BlinkLink's current scale and architecture. Paying MSK costs and operational overhead for capabilities that are not needed is architecturally premature.

## Decision

Use **Redis Streams with Consumer Groups** as the event delivery mechanism for click events. Redis is already in the stack and already deployed on ElastiCache.

## Stream Design

| Component | Value |
|---|---|
| Stream key | `blinklink:events:clicks` |
| Consumer group | `analytics-consumer-group` |
| Message fields | `url_code`, `user_id`, `clicked_at`, `country_code`, `device_type`, `user_agent_hash` |

## Delivery Contract

### Producer (`RedirectUrlUseCase` via `EventStreamPort`)

- `XADD blinklink:events:clicks * url_code X user_id Y clicked_at Z ...`
- Fire-and-forget: redirect returns 302 before XADD completes
- If Redis unavailable: event dropped, WARN log, redirect succeeds (FR-1.4 policy)

### Consumer (`AnalyticsStreamConsumer` — dedicated thread, analytics module)

- `XREADGROUP GROUP analytics-consumer-group consumer-1 COUNT 100 STREAMS ...`
- Writes batch to ClickHouse
- `XACK` on successful write (at-least-once delivery)
- On ClickHouse failure: WARN log, message remains pending for retry

## Port Contract

| Component | Responsibility |
|---|---|
| `EventStreamPort` (application layer, url module) | `publish(ClickEvent)` |
| `RedisEventStreamAdapter` (infrastructure) | `XADD` to Redis Streams |
| `AnalyticsStreamConsumer` (infrastructure, analytics module) | `XREADGROUP` + ClickHouse write |

**Architecture rule:** No class in the domain or application layer may reference Redis Streams directly. Event publishing and consuming are exclusively infrastructure concerns behind ports.

## Migration Path to Kafka

If BlinkLink is extracted to microservices in v5 and Kafka becomes justified:

- `EventStreamPort` contract does not change
- `RedisEventStreamAdapter` is replaced by `KafkaEventStreamAdapter`
- `AnalyticsStreamConsumer` is replaced by `KafkaAnalyticsConsumer`
- The analytics module and ClickHouse are unaffected
- The url module and redirect path are unaffected

## Trade-offs vs Kafka

| Gain | Cost |
|---|---|
| No additional AWS service (MSK) — Redis already deployed on ElastiCache | No long-term event retention — Redis Streams hold events until acknowledged |
| No broker management, topic configuration, or consumer group rebalancing at broker level | No replay from arbitrary historical offset (Kafka's strongest advantage) |
| Simpler local development — one less Docker container | Less battle-tested at very high throughput vs Kafka — not relevant at current scale |
| Consistent with existing Redis investment and operational knowledge | If replay or long retention become requirements, Kafka is the correct migration |
