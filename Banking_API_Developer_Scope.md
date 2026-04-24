# Banking REST API — Developer Scope

**Source:** Swedbank homework assignment
**Stack:** Java + Spring Boot
**Deliverable:** A self-contained microservice demonstrated at a tech interview

---

## 1. Context & Goal

Build a REST API that manages customer bank accounts with multi-currency support. The assignment is deliberately small, so the assessor is not looking at whether the endpoints "work" — they are looking at **how the developer thinks about money, concurrency, failure, and production-readiness**. Every design choice must be defensible in the follow-up interview.

Treat the assignment as a bounded microservice for a banking domain. Correctness over cleverness.

---

## 2. Functional Scope

Four capabilities are listed explicitly:

1. **Add money** to an account in a specific currency.
2. **Debit money** from an account in a specific currency (no auto-conversion).
3. **Get account balance** — must return all currency balances on the account.
4. **Currency exchange** — convert between currencies inside the account using fixed rates.

Implicit capability (needed to exercise the rest): **create an account**.

### 2.1 Proposed REST Contract

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/accounts` | Create an account |
| `GET`  | `/api/v1/accounts/{id}` | Get account + all currency balances |
| `POST` | `/api/v1/accounts/{id}/deposits` | Add money (currency + amount) |
| `POST` | `/api/v1/accounts/{id}/debits` | Debit money (currency + amount) |
| `POST` | `/api/v1/accounts/{id}/exchanges` | Exchange between two currencies on the account |
| `GET`  | `/api/v1/exchange-rates` | List current fixed rates |
| `GET`  | `/api/v1/accounts/{id}/transactions` | Transaction history (nice-to-have) |

Use **plural resource nouns**, **versioned path**, and **HTTP verbs semantically** (POST for state-changing operations; GET for reads).

### 2.2 Request / Response Shape

Use JSON. Money is represented as `{"amount": "100.00", "currency": "EUR"}` — **amount as a string** to avoid any client-side float parsing ambiguity. Server parses into `BigDecimal`.

Apply **RFC 7807 Problem Details** for errors:

```json
{
  "type": "https://example.com/errors/insufficient-funds",
  "title": "Insufficient funds",
  "status": 422,
  "detail": "Account balance in EUR is 45.00, requested 100.00",
  "instance": "/api/v1/accounts/abc-123/debits"
}
```

---

## 3. Non-Functional Requirements (the "high-quality" part)

These are not in the assignment text — they are what separates a passing submission from a strong one.

- **Monetary correctness.** Never use `double` or `float`. Use `BigDecimal` with `RoundingMode.HALF_EVEN` (banker's rounding). Store amounts with a fixed scale (e.g. 2 for EUR/USD/GBP/SEK) or store in minor units (long cents) — pick one, apply consistently, justify it.
- **Concurrency safety.** Two simultaneous debits on the same account must not overdraw. Use optimistic locking (`@Version` on the balance row) or pessimistic (`SELECT … FOR UPDATE`). Be ready to explain the tradeoff.
- **Transactional integrity.** Every state change is inside a single `@Transactional` boundary. Debit = balance update + transaction record + (successful) external call — all or nothing.
- **Idempotency.** Accept an `Idempotency-Key` header on deposit/debit/exchange. Repeated identical requests return the original result, never double-charge. This is table stakes for payment APIs.
- **Validation.** Use `jakarta.validation` (`@NotNull`, `@Positive`, `@DecimalMin("0.01")`). Reject zero and negative amounts, unknown currencies, unknown account IDs.
- **Observability.** Structured logging (SLF4J + JSON layout), Spring Boot Actuator (`/health`, `/metrics`, `/prometheus`), Micrometer timers on each endpoint.
- **API documentation.** Springdoc OpenAPI — Swagger UI served at `/swagger-ui.html`. Presentable in the interview.
- **Security.** Even if out of scope, call it out explicitly in the README: "no auth layer — would add OAuth2 / JWT and mTLS for inter-service calls in production."
- **Configuration.** Externalise via `application.yml` + profiles (`dev`, `test`). No hardcoded URLs or rates in Java code.

---

## 4. Architecture & Tech Choices

Recommended defaults — each one is defensible:

| Concern | Choice | Why |
|---|---|---|
| Language | Java 21 (LTS) | Records, pattern matching, virtual threads available |
| Framework | Spring Boot 3.x | Assignment requires it; current LTS line |
| Build | Maven or Gradle | Either is fine; Gradle is faster, Maven is more universal in banks |
| Persistence | Spring Data JPA + Hibernate | Standard for this kind of service |
| DB | H2 in-memory for dev/test; profile for PostgreSQL | Assignment allows H2; showing a Postgres profile is a plus |
| Migrations | Flyway | Versioned schema from day one, even for H2 |
| HTTP client | `RestClient` (Spring 6.1+) or `WebClient` | Modern, avoid deprecated `RestTemplate` |
| Resilience | Resilience4j | Circuit breaker + retry + timeout on the external call |
| Mapping | MapStruct *or* hand-written | Either works; avoid Lombok if the interviewing team is strict about it |
| Testing | JUnit 5, Mockito, WireMock, Testcontainers | WireMock for the httpstat.us call, Testcontainers if using Postgres profile |
| Docs | Springdoc OpenAPI | Auto-generated Swagger UI |
| Packaging | Dockerfile + `docker-compose.yml` | "Self-contained microservice" — runnable in one command |

### 4.1 Layering

Keep it simple and boring:

```
controller  →  service  →  repository
                 ↓
          external-client (httpstat.us)
```

- **Controller**: HTTP concerns only. Validation, DTO ↔ command, response mapping.
- **Service**: Business rules, transaction boundaries, orchestration of repo + external call.
- **Repository**: Spring Data interfaces.
- **Domain model**: JPA entities + value objects (`Money`, `Currency`).
- **DTOs are separate from entities.** Do not expose JPA entities over HTTP.

---

## 5. Domain Model

Minimum viable entities:

- **Account** — `id (UUID)`, `ownerName`, `createdAt`.
- **AccountBalance** (or **Wallet**) — one row per `(account, currency)`. Fields: `accountId`, `currency`, `amount`, `version` (for optimistic locking).
- **Transaction** — immutable ledger record. Fields: `id`, `accountId`, `type` (DEPOSIT / DEBIT / EXCHANGE_OUT / EXCHANGE_IN), `currency`, `amount`, `status` (PENDING / COMPLETED / FAILED), `externalCallRef`, `idempotencyKey`, `createdAt`.
- **ExchangeRate** — `fromCurrency`, `toCurrency`, `rate`, `validFrom`. Seeded via Flyway with a fixed matrix.

`Currency` is an enum: `EUR, USD, SEK, GBP`.

**Design note worth raising in the interview:** a real system would use event sourcing or a pure append-only ledger — balances would be projections, never directly mutable. For this assignment, a mutable balance + append-only transaction log is a pragmatic middle ground. Be ready to discuss both.

---

## 6. Business Rules & Edge Cases to Handle

1. **Deposit** — create the balance row for that currency if it doesn't exist; add amount.
2. **Debit**
   - Balance row must exist and `balance.amount >= debit.amount` in the requested currency.
   - **Call the external system first** (assignment requirement). If it fails → the debit fails, nothing is persisted.
   - Record a `Transaction` row regardless of success/failure (with status).
3. **Exchange**
   - Source currency balance must be sufficient.
   - Apply `rate` from the `ExchangeRate` table. Round result with `HALF_EVEN` to the target currency's scale.
   - Write two transaction rows (EXCHANGE_OUT and EXCHANGE_IN) inside one DB transaction.
4. **Amount validation** — must be strictly positive, scale ≤ 2.
5. **Currency validation** — must be in the enum.
6. **Account not found** — `404`.
7. **Insufficient funds** — `422 Unprocessable Entity` with Problem Details body.
8. **Unknown exchange rate pair** — `422` (or seed the full 4×4 matrix so this can't happen; prefer seeding).
9. **Concurrent debits** — optimistic lock → retry once on `OptimisticLockException`, then fail.

---

## 7. External System Call (httpstat.us)

The assignment requires simulating an external call before debiting. Treat it like a real integration:

- Injected client (interface + impl), not inline `RestClient` usage.
- Configurable base URL in `application.yml` — default `https://httpstat.us/200`.
- **Timeout** (e.g. 2s connect, 5s read).
- **Retry** with exponential backoff (e.g. 3 attempts) via Resilience4j.
- **Circuit breaker** so repeated failure doesn't pile up threads.
- **Log** request/response with a correlation ID.
- Easy to switch to `/500` or `/408?sleep=10000` in config to demo failure paths during the interview.

**Design point for the interview:** calling the external service *before* the DB write means the DB transaction only commits if the external call succeeded — which is the correct ordering for "log before debit". Calling it *inside* the same DB transaction holds the row lock for the duration of an HTTP call, which is a latency/contention hazard. Be ready to discuss this tradeoff, and optionally implement the external call outside the `@Transactional` method but still before the state change.

---

## 8. Persistence & Migrations

- Flyway script `V1__init.sql` creates `account`, `account_balance`, `transaction`, `exchange_rate` tables + indexes on `(account_id, currency)`.
- Flyway script `V2__seed_exchange_rates.sql` seeds the fixed 4×4 rate matrix (including 1.0 for self-pairs, or exclude them).
- Use UUIDs for IDs (generated server-side, not auto-increment) — stable, non-guessable, useful in distributed contexts.

---

## 9. Error Handling

One `@RestControllerAdvice` class maps:

- `MethodArgumentNotValidException` → 400
- `AccountNotFoundException` → 404
- `InsufficientFundsException` → 422
- `UnknownCurrencyException` / `UnknownExchangeRateException` → 422
- `ExternalServiceException` → 502 Bad Gateway
- `OptimisticLockException` (after retry) → 409 Conflict
- Everything else → 500 (logged with stack trace, sanitised response body)

All responses in RFC 7807 format.

---

## 10. Testing Strategy

This is often where the interview score is decided.

- **Unit tests** — services, `Money` value object, rounding behaviour, exchange math. Fast, no Spring context.
- **Repository tests** — `@DataJpaTest` against H2. Verify uniqueness constraints, optimistic locking.
- **Controller tests** — `@WebMvcTest` + MockMvc. Verify HTTP mapping, validation, error responses.
- **Integration tests** — `@SpringBootTest` with WireMock stubbing httpstat.us. Cover: happy path deposit → debit → balance, external call failure, insufficient funds, idempotency key replay.
- **Concurrency test** — spawn N threads hitting the same debit endpoint; assert the final balance and transaction count are consistent.
- Aim for **>80% line coverage on the service layer**, less important elsewhere. Quality over percentage.

---

## 11. Deliverables (Definition of Done)

A submission is "done" when **all** of the following are true:

1. `git clone && ./mvnw spring-boot:run` (or `docker compose up`) starts the service.
2. Swagger UI loads at `/swagger-ui.html` and every endpoint can be exercised from it.
3. All tests pass (`./mvnw verify`) with zero warnings.
4. README covers: what it is, how to run, how to test, design decisions, what's deliberately out of scope, what would change for production.
5. Flyway migrations are committed and run on startup.
6. Postman collection or `.http` file in the repo with example requests for the interview demo.
7. Actuator endpoints respond.
8. Example `curl` commands in README produce sensible responses.

---

## 12. Interview Preparation

The assignment explicitly says the solution must be presented. Expect questions on:

- **Why `BigDecimal`, not `double`?** Binary floats cannot represent 0.1 exactly → rounding drift → lost or fabricated money.
- **How do you prevent double-debit under concurrency?** Optimistic vs pessimistic locking tradeoffs; `SERIALIZABLE` isolation costs; why idempotency keys matter.
- **What if the external call succeeds but the DB commit fails?** Discuss the dual-write problem, compensating transactions, outbox pattern, why it's a known-hard problem.
- **Why fixed exchange rates here, and how would you do it in production?** Rate provider feed, caching with TTL, staleness detection, rate at transaction time vs lookup time.
- **Why a microservice for this?** Bounded context (account management), independent scaling, isolation of the persistence layer. Acknowledge the cost (ops overhead, distributed transactions).
- **Scaling to N accounts and M TPS?** Read replicas, partitioning by account ID, CQRS, caching balance reads.
- **How would you add auth?** OAuth2 resource server, client credentials for service-to-service, mTLS, scopes per operation.
- **What's your test pyramid?** Lots of fast unit tests, fewer integration tests, a handful of end-to-end.
- **Trade-offs in your design?** Pick two or three you actively chose against (e.g. "I didn't do event sourcing because…"). Showing you considered alternatives is stronger than showing you implemented one.

---

## 13. Deliberately Out of Scope (state in the README)

- Authentication / authorisation
- Real currency rate provider
- Customer / KYC model
- Multi-tenant isolation
- Distributed tracing (though a correlation ID header is a nice touch)
- Event publishing to a message broker
- Internationalised error messages

Listing these proactively is itself a signal of seniority.

---

## 14. Timeboxing Suggestion

For a candidate working on this as a take-home:

| Day | Focus |
|---|---|
| 1 | Project skeleton, domain model, Flyway, Account + Deposit happy path, Swagger up |
| 2 | Debit + external call + Resilience4j, Exchange endpoint, validation, error handling |
| 3 | Concurrency (locking), idempotency, Transaction log, polish |
| 4 | Tests (unit + integration + concurrency), Postman collection, README, Docker |

Four focused half-days gets you from zero to a submission that stands out.
