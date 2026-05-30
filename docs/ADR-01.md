# ADR-001 — Retain Layered Monolith for v4.1; Adopt Spring Modulith from v4.2

| | |
|---|---|
| **Status** | REVISED (original: ACCEPTED 2026-03) |
| **Date** | 2026-05 |
| **Author** | PabloTzeliks |
| **Version** | v4.0.0 → v4.2 |

## Original Decision (2026-03)

We would NOT adopt Spring Modulith for v4.0.0. The layered monolith structure would be retained and extended with new ports and adapters.

## Revision Rationale

The original decision was correct for v4.1: it allowed Redis to be introduced without a concurrent architectural refactor. v4.1 is now complete with a mature Redis integration layer.

The original rationale no longer holds for v4.2 onward. The introduction of asynchronous event flows (click events → analytics), a new domain module (analytics), and future fan-out consumers (fraud detection, spam detection) makes the absence of enforced module boundaries an active risk. Without boundaries, coupling between `url`, `user`, and `analytics` domains becomes accidental rather than deliberate.

## Revised Decision

Starting at v4.2, BlinkLink adopts **Spring Modulith** as the structural framework for enforcing module boundaries within the existing monolith. The physical deployment unit remains a single JAR. No microservices extraction occurs in v4.

## Module Boundaries

- `module: url` → URL creation, redirect, custom codes, cache
- `module: user` → Registration, authentication, plans, rate limiting
- `module: analytics` → Click events, aggregations, read API

## Rules Enforced by Spring Modulith

- A module may only access public API types of another module
- Cross-module communication occurs exclusively via `ApplicationEventPublisher`
- No module may directly query another module's database schema
- `@ApplicationModuleTest` validates each module in isolation

## Database Schema Isolation

- `schema: url` → table `urls` (PostgreSQL)
- `schema: user` → table `users` (PostgreSQL)
- `schema: analytics` → table `clicks` (ClickHouse — see ADR-005)

## Migration Path to Microservices

Spring Modulith enforces the same boundaries that microservices would require. If extraction becomes necessary in v5:

- Each module already has its own schema (database split is the hard part — it is already done)
- Cross-module events via `ApplicationEventPublisher` become cross-service events via Redis Streams or HTTP — the event contract does not change
- The module's public API becomes the service's API contract

## Alternatives Considered

| Option | Decision |
|---|---|
| Option A — Retain layered monolith permanently | Rejected: fan-out event flows and analytics isolation require explicit boundaries. Convention is insufficient as the domain grows. |
| Option B — Microservices now | Rejected: massively premature. Operational complexity (service discovery, distributed tracing, independent deployments) far exceeds the benefit at this stage. |
