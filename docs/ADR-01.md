# ADR-001 — Retain Layered Monolith; Defer Modular Monolith

| | |
|---|---|
| **Status** | ACCEPTED |
| **Date** | 2026-03 |
| **Author** | PabloTzeliks |
| **Version** | v4.0.0 |

## Context

v3.0.0 is a layered monolith with hexagonal boundaries enforced by convention (package structure), not compilation boundaries. v4.0.0 introduces Redis, Kafka, DynamoDB, and AWS deployment. The question: refactor to Modular Monolith (separate Maven modules) first?

## Decision

**No Modular Monolith refactor for v4.0.0.** Retain layered monolith; extend with new ports and adapters.

## Rationale

- Modular Monolith solves accidental cross-domain coupling in multi-team environments. One developer + two domain entities = problem doesn't meaningfully exist.
- Refactoring cost: Maven multi-module restructuring, all import paths, Spring context config, all integration tests → zero user-facing value.
- v4.0.0 introduces four new technologies. Learning five things simultaneously produces shallow understanding of all. Goal: deep understanding, not breadth.
- Existing hexagonal discipline already provides the key benefit: new infrastructure added through new adapters without touching domain.
- Pain-driven refactoring > anticipatory refactoring.

## Consequences

- ✅ v4 feature work begins immediately
- ✅ All 80%+ v3 test coverage remains valid
- ✅ Technologies learned sequentially
- ⚠️ Cross-domain coupling possible by convention (acceptable at this scale)
- ❌ If project grows to multi-developer or 3+ domain entities, refactor will be more expensive later

**Deferred:** Modular Monolith is recommended first consideration for v5.0.0 architecture review.

## Alternatives Rejected

| Option | Reason |
|---|---|
| Modular Monolith now | High cost, no immediate payoff, blocks v4 feature work |
| Microservices | Massively premature. Operational complexity far exceeds problem size. |