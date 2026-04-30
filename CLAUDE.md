# CLAUDE.md — BlinkLink Operational Guide

> Read this file first. Do not read docs/ADR-*.md unless a task explicitly touches that decision area.

## Project Identity

BlinkLink v4.0.0 — Production URL Shortener. Java 21 · Spring Boot 4 · PostgreSQL 17 · Redis · Kafka · DynamoDB.
Single developer (Pablo Tzeliks). Clean + Hexagonal Architecture. Pure DDD domain.

**Current phase: v4.1** — Redis cache-aside, rate limiting, custom short codes. Kafka/DynamoDB are v4.2+.

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
- All external concerns (JPA, Redis, JWT, Kafka) live exclusively in `infrastructure/`.
- Use cases orchestrate via **ports** (interfaces). Adapters implement ports.
- No class in `domain/` or `application/` may import from `org.springframework.kafka`, `org.springframework.data.redis`, or any JPA annotation.

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
- `Url.create(id, originalUrl, shortCode, userId, strategy)` — generated code
- `Url.createWithCustomCode(id, originalUrl, customCode, userId, strategy)` — VIP/Enterprise only

`userId` is **mandatory** as of v4.1 (FR-1.7). Do not create Url without it.

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
| `SequencePort` | application/url/ports | `RedisSequenceAdapter` *(v4.1 new)* |
| `CachePort` | application/url/ports | `RedisCacheAdapter` *(v4.1 new)* |
| `RateLimitPort` | application/url/ports | `RedisRateLimitAdapter` *(v4.1 new)* |
| `CurrentUserProviderPort` | domain/user/ports | `SpringSecurityCurrentUserProvider` |
| `TokenGenerationPort` | domain/user/ports | `TokenService` |
| `UserPasswordEncoderPort` | domain/user/ports | `BCryptPasswordEncoderAdapter` |

**v4.2 ports (DO NOT implement yet):** `EventPublisherPort`

---

## Redis Key Schema (v4.1)

| Key | Type | TTL | Purpose |
|---|---|---|---|
| `blinklink:url:{code}` | String | min(remainingUrlTTL, 7d) | Cache-aside redirect lookup |
| `blinklink:rate:{userId}` | String/ZSet | sliding window | Rate limit counter |
| `sequence:url:id` | String | none | ID sequence counter |

**Security rule:** No PII in Redis keys or values. Cached values = `original_url` only.

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

## Custom Short Code Flow (v4.1)

1. Request includes optional `custom_code` field
2. Use case checks: user plan must be VIP or ENTERPRISE
3. Validate format: `[a-zA-Z0-9_-]`, length 4–20 chars
4. Redis `SETNX blinklink:url:{code}` — if 0: reject with `ShortCodeAlreadyTakenException`
5. PostgreSQL INSERT — if UNIQUE violation: reject (no retry for custom codes)
6. For generated codes: retry with new ID on UNIQUE violation (up to 3x)

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
| `docs/ADR-003-jpa-optimistic-lock.md` | v3: JPA impedance mismatch, `@Version` on UserEntity |
| `docs/ADR-004-url-lifecycle-purge.md` | v3: Expiration strategy pattern, `FOR UPDATE SKIP LOCKED` purge |
| `docs/ADR-001-monolith-structure.md` | v4: Why no Modular Monolith refactor |
| `docs/ADR-002-phased-rollout.md` | v4: v4.1/v4.2/v4.3 phased delivery, Kafka placement |
| `docs/ADR-005-redis-sequence.md` | v4: Redis INCR replaces PostgreSQL nextval() |
| `docs/ADR-006-dynamodb-analytics.md` | v4: DynamoDB key design for analytics (v4.2) |

---

## What NOT to Do

- Do not add Spring annotations to `domain/` classes
- Do not call repositories directly from controllers
- Do not implement Kafka or DynamoDB adapters — that is v4.2
- Do not bypass the port interface to call adapters directly
- Do not commit sensitive values (secrets, tokens, passwords) to any file
- Do not delete or modify Flyway migration files already applied