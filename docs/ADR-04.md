# ADR-004 — Custom Code Uniqueness: PostgreSQL UNIQUE as Final Arbiter

| | |
|---|---|
| **Status** | ACCEPTED (replaces original ADR-004 on DynamoDB) |
| **Date** | 2026-05 |
| **Author** | PabloTzeliks |
| **Scope** | v4.1 (DELIVERED) |

## Context

Custom short codes (FR-1.5, FR-1.6) require uniqueness enforcement. The original ADR-002 referenced Redis `SETNX` as the uniqueness gate. During implementation, `SETNX` was rejected in favour of the PostgreSQL UNIQUE constraint as the sole arbiter.

## Decision

The PostgreSQL UNIQUE constraint on the `short_code` column is the authoritative uniqueness gate for custom codes. Redis is used only for cache warming after successful persistence, not for uniqueness checking.

## Rationale

`SETNX` returns boolean false for two distinct conditions: code already taken, or Redis unavailable. The use case cannot distinguish between them without a try/catch that leaks infrastructure concerns into application logic. This violates the port/adapter contract.

The PostgreSQL UNIQUE constraint is unambiguous: `DataIntegrityViolationException` means duplicate, period. The use case handles exactly one failure mode with exactly one exception type.

`SETNX` before persistence creates phantom Redis keys: if the INSERT fails for any reason after `SETNX` succeeds, the key remains in Redis until TTL expiry. During that window, the code appears unavailable even though it does not exist in the database.

## Implementation Flow

| Step | Action | Failure Response |
|---|---|---|
| 1 | Plan gate: VIP or ENTERPRISE check | 403 Forbidden if FREE |
| 2 | `CustomCodeValidator.validate()` | 422 Unprocessable Entity if invalid format, reserved, or blocklisted |
| 3 | `SequencePort.nextId()` | Proceeds to next step |
| 4 | `Url.create()` with customCode as `short_code` | Domain entity created |
| 5 | `repository.save()` | 409 `DuplicateCodeException` on UNIQUE violation |
| 6 | `cache.put()` | Silent degradation on Redis failure (FR-1.4 policy) |
