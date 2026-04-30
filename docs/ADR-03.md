# ADR-003 — JPA Impedance Mismatch and Optimistic Locking

| | |
|---|---|
| **Status** | ACCEPTED |
| **Date** | v3.0.0 |
| **Author** | PabloTzeliks |

## Context

In pure DDD, entity IDs are generated in the domain (UUID for User, Long from sequence for Url). With pre-filled IDs, Spring Data JPA's `save()` may attempt merge instead of persist, triggering extra SELECT. Additionally, concurrent updates to User (plan changes, role changes) need protection without pessimistic locking.

## Decision

- Domain entities (`User`, `Url`) remain persistence-agnostic — zero JPA annotations.
- `@Version` lives exclusively on `UserEntity` (infrastructure layer).
- `UserEntity` has mutable setters only for fields that change: `role`, `plan`, `password`, `updatedAt`.
- `UrlEntity` implements `Persistable<Long>` with `isNew` flag — avoids extra SELECT on INSERT.

## Consequences

- ✅ Domain stays pure and framework-agnostic
- ✅ Optimistic locking prevents lost updates under concurrency
- ✅ `Persistable` pattern eliminates redundant SELECT on Url creation
- ❌ More complexity in update paths: `PostgresUserRepositoryAdapter.update()` must load the entity, apply domain changes via mapper, then save
- ❌ `@Version` field must never be exposed or mapped to domain model

## Patterns in Use

```
Domain → toEntity() → UrlEntity (isNew=true) → repository.save() → INSERT (no SELECT)
Domain → findById() → UserEntity → toDomain() → mutate domain → updateEntityFromDomain() → save() → UPDATE
```