# CLAUDE.md — BlinkLink Operational Guide

> Read this file first. Do not read docs/ADR-*.md unless a task explicitly touches that decision area.

## Project Identity

BlinkLink v4.0.0 — Production URL Shortener. Java 21 · Spring Boot 4 · PostgreSQL 17 · Redis (+ Redis Streams) · ClickHouse.
Single developer (Pablo Tzeliks). Clean + Hexagonal Architecture. Pure DDD domain.

**Current phase: v4.1** — Redis cache-aside (delivered), Redis sequence (delivered), custom short codes (delivered), **rate limiting (IN DEVELOPMENT)**. Spring Modulith + Redis Streams + ClickHouse are v4.2+. Kafka and DynamoDB were dropped (see ADR-002).

---

## Architecture in 30 Seconds

```
Infrastructure → Application → Domain
     ↓               ↓            ↓
Controllers      Use Cases     Entities
Adapters         Ports         Value Objects
Schedulers       DTOs          Strategies
```

- Domain is **framework-agnostic**. Zero Spring imports inside `domain/`.
- All external concerns (JPA, Redis, Redis Streams, JWT, ClickHouse) live exclusively in `infrastructure/`.
- Use cases orchestrate via **ports** (interfaces). Adapters implement ports.
- No class in `domain/` or `application/` may import from `org.springframework.data.redis` (incl. Redis Streams) or any JPA annotation. Event publish/consume is exclusively an infrastructure concern behind `EventStreamPort` (ADR-006).

**Package structure:**
```
pablo.tzeliks.blink_link/
  domain/
    url/model, ports, strategy, exception
    user/model, ports, exception
    common/exception
  application/
    url/usecase, dto, mapper
    user/usecase, dto, mapper
  infrastructure/
    url/persistence, encoding, schedule
    user/persistence
    security/adapter, config, filter, jwt, oauth2
    configuration/
    web/url, user, common, dto
```

---

## Git Rules (Non-negotiable)

- **Never commit directly to `main`**. Always work on a feature branch.
- **No `Co-Authored-By` in commit messages**.
- Branch naming: `feat/`, `fix/`, `refactor/`, `chore/`, `docs/`
- Commits must be atomic and describe *what changed*, not *what the task was*.

---

## Domain Model (v4.1 state)

### Url
Fields: `id (Long)`, `originalUrl`, `shortCode`, `userId (UUID)`, `createdAt`, `expirationDate`
Factory methods:
- `Url.create(id, userId, originalUrl, shortCode, strategy)` — used for both generated and custom codes (custom code is passed as `shortCode`)
- `Url.restore(...)` — rehydrate from persistence

`userId` is **mandatory** as of v4.1 (FR-1.8). Do not create Url without it.

### User
Fields: `id (UUID)`, `email (Email VO)`, `password (Password VO)`, `role (Role)`, `plan (Plan)`, `authProvider`, `createdAt`, `updatedAt`
Plans: `FREE | VIP | ENTERPRISE`. Roles: `USER | ADMIN`.

### Value Objects
- `Email` — validated on construction, immutable record
- `Password` — non-blank, immutable record

---

## Active Ports (v4.1)

| Port | Location | Implemented By |
|---|---|---|
| `UrlRepositoryPort` | domain/url/ports | `PostgresUrlRepositoryAdapter` |
| `UserRepositoryPort` | domain/user/ports | `PostgresUserRepositoryAdapter` |
| `ShortenerPort` | domain/url/ports | `Base62Encoder` |
| `SequencePort` | application/url/port/out | `RedisSequenceAdapter` *(v4.1 delivered)* |
| `CachePort` | application/url/port/out | `RedisCacheAdapter` *(v4.1, mid-refactor to `UrlContext` payload)* |
| `CurrentUserProviderPort` | application/user/ports | `SpringSecurityCurrentUserProvider` |
| `TokenGenerationPort` | application/user/ports | `TokenService` |
| `UserPasswordEncoderPort` | domain/user/ports | `BCryptPasswordEncoderAdapter` |

**v4.1 IN DEVELOPMENT (not yet implemented):** `RateLimitPort` (no `RedisRateLimitAdapter`, no filter/interceptor yet). Only the `RateLimitResult` and `UrlContext` records exist under `application/url/port/out`.

**v4.2 ports (DO NOT implement yet):** `EventStreamPort` (Redis Streams — ADR-006)

---

## Redis Key Schema

| Key | Type | TTL | Purpose |
|---|---|---|---|
| `url:{shortCode}` | Hash (target: `UrlContext` = destination/ownerId/rateLimit) | min(remainingUrlTTL, 7d) | Cache-aside redirect lookup |
| `ratelimit:user:{ownerId}` | ZSet | sliding window | Per-user rate limit (IN DEVELOPMENT) |
| `sequence:url:id` | String | none | ID sequence counter |
| `blinklink:events:clicks` | Stream | n/a | Click event delivery to analytics (v4.2 — ADR-006) |

> Current code uses a plain `url:` String value (`originalUrl` only); the migration to the `UrlContext` hash payload is mid-refactor. The `ratelimit:user:{ownerId}` ZSet and the events stream are not implemented yet.

**Security rule:** No PII and no tokens in Redis keys or values (NFR-4.1). Cached `UrlContext` carries only `destination`, `ownerId` (UUID) and `rateLimit`.

---

## Rate Limits (FR-2)

| Plan | Limit |
|---|---|
| FREE | 100 req/min |
| VIP | 500 req/min |
| ENTERPRISE | 2000 req/min |

429 response must include `Retry-After` header.
Rate limit key = `userId` (authenticated) or IP (anonymous). Never URL code.

---

## Custom Short Code Flow (v4.1 — delivered, see ADR-004)

PostgreSQL UNIQUE on `short_code` is the **sole** uniqueness arbiter. `SETNX` was rejected (it cannot distinguish "taken" from "Redis down" and leaves phantom keys on INSERT failure).

1. Request includes optional `customCode` field
2. Plan gate: user plan must be VIP or ENTERPRISE → else 403
3. `CustomCodeValidator.validate()`: format `[a-zA-Z0-9_-]` 4–20 chars, reserved words, blocklist → else 422
4. Best-effort pre-checks: `cache.exists(code)` and `repository.existsByShortCode(code)` → fast-fail 409 if taken
5. PostgreSQL INSERT — UNIQUE violation → 409 `DuplicateCodeException` (no retry for custom codes)
6. For *generated* codes (ShortenUrlUseCase): retry with new ID on UNIQUE violation (up to 3x)

---

## Redis Sequence Initialization (ADR-003)

On startup, `SequenceInitializer` must:
1. Query PostgreSQL `MAX(id)` from `urls`
2. `SET sequence:url:id {max_id} NX` — only if key does not exist
3. Serve all subsequent IDs via `INCR sequence:url:id`

On collision (rare): PostgreSQL UNIQUE constraint fires → use case catches → retry.

---

## Testing Rules

- Unit tests: pure Mockito, no Spring context
- Integration tests: `@DataJpaTest` or `@SpringBootTest` + Testcontainers
- E2E: `@SpringBootTest(RANDOM_PORT)` + MockMvc
- JaCoCo floor: **80% instruction coverage** — build fails if not met
- New ports must have unit tests with mock adapters (NFR-5.1)
- Cache miss → PostgreSQL → cache populate flow must have Testcontainers test (NFR-5.2)
- Redis sequence init (SET NX + INCR) must have Redis Testcontainers test (NFR-5.2)

---

## Error Handling

All errors follow RFC 7807 Problem Details. See `GlobalExceptionHandler`.
Key mappings:
- `ResourceNotFoundException` → 404
- `InvalidResourceException` → 400
- `BusinessRuleException` → 409
- `AuthenticationException` → 401
- `AuthorizationException` → 403
- `UrlExpiredException` → 410
- Rate limit breach → 429 + `Retry-After`

---

## ADR Index

Read the relevant ADR file when a task touches that decision area.

| File | Covers |
|---|---|
| `docs/ADR-01.md` | v4: Retain layered monolith for v4.1; **adopt Spring Modulith from v4.2** |
| `docs/ADR-02.md` | v4: Revised phased rollout (v4.1→v4.4); Kafka & DynamoDB dropped |
| `docs/ADR-03.md` | v4: Redis `INCR` as distributed sequence server (replaces PostgreSQL `nextval()`) |
| `docs/ADR-04.md` | v4: Custom code uniqueness — PostgreSQL UNIQUE as final arbiter (SETNX rejected) |
| `docs/ADR-05.md` | v4.2: **ClickHouse** for analytics storage (replaces DynamoDB) |
| `docs/ADR-06.md` | v4.2: **Redis Streams** for click event delivery (replaces Kafka) |
| `docs/ADR-07.md` | v3: JPA impedance mismatch, `@Version` on UserEntity, `Persistable` |
| `docs/ADR-08.md` | v3: URL lifecycle, expiration strategy, `FOR UPDATE SKIP LOCKED` async purge |

---

## What NOT to Do

- Do not add Spring annotations to `domain/` classes
- Do not call repositories directly from controllers
- Do not implement the Redis Streams event pipeline or ClickHouse analytics adapters — that is v4.2
- Do not reintroduce Kafka or DynamoDB — both were dropped (ADR-002, ADR-005, ADR-006)
- Do not bypass the port interface to call adapters directly
- Do not commit sensitive values (secrets, tokens, passwords) to any file
- Do not delete or modify Flyway migration files already applied