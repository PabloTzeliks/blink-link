# ADR-002 — Redis as First v4 Technology; Phased Rollout

| | |
|---|---|
| **Status** | ACCEPTED |
| **Date** | 2026-03 |
| **Author** | PabloTzeliks |
| **Scope** | v4.1 first |

## Context

v4.0.0 scope: Redis caching, custom short URLs, Kafka event streaming, DynamoDB analytics, AWS deployment. Simultaneous delivery = long cycle with no working milestones and shallow learning.

## Decision

Deliver v4.0.0 in three sequential sub-versions. Each must be fully working and deployed before the next begins.

| Phase | Technology | Feature | Done When |
|---|---|---|---|
| **v4.1** | Redis + ElastiCache | Cache-aside · Rate limiting · Custom short URLs | ElastiCache deployed, redirect p95 < 20ms on cache hit |
| **v4.2** | Kafka (MSK) + DynamoDB | Click event streaming · Analytics writes | Events confirmed in DynamoDB, redirect never blocks on analytics |
| **v4.3** | Analytics read API | GET /urls/{code}/stats · Plan-gated | VIP users can query click history by day and country |

## Domain Pre-condition (before any v4.1 work)

`Url` domain entity must carry `userId (UUID)`. Required for rate limiting, custom URL ownership validation, and future analytics. This is a targeted domain change, not an architectural refactor.

## Kafka Placement Decision

Kafka lives inside the monolith as an infrastructure adapter (`KafkaAdapter` implementing `EventPublisherPort`). The `@KafkaListener` consumer that writes to DynamoDB is a Spring bean within the same JAR.

**Architecture rule:** No class in `domain/` or `application/` may import from `org.springframework.kafka`. Kafka is exclusively an infrastructure concern in `infrastructure.adapters.messaging`. This boundary ensures the consumer can be physically extracted in v5 without domain changes.

**Thread isolation:** `@KafkaListener` consumer must run on a dedicated `ThreadPoolTaskExecutor`, isolated from Tomcat HTTP thread pool. Consumer backpressure must not impact the redirect path.