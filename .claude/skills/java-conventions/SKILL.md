---
name: java-conventions
description: >-
  Engineering standard for the Java code in this repo. States the bar code is held to, what a
  review flags as blocking, and the gate commands that must pass. Loaded on demand by /start-issue,
  /pr-review and /fix-review-comments for issues and diffs that touch Java code; never pre-read.
---

# Java conventions

This skill is a **standard, not a description**: it states the bar this repo holds `Java` code to,
not a narration of what the code currently does. Apply it to the lines a change **adds or modifies**;
pre-existing violations in untouched code are backlog issues, not blockers. Surrounding code is never
precedent for a new violation.

## How this skill is used

- `/start-issue` loads it when an issue touches this stack, before any code is written.
- `/pr-review` hands it to the **Reviewer Agents**, which report only the
  violations listed under **Blocking in review** below.
- `/fix-review-comments` verifies each finding against it before changing code.
- `.claude/conventions/comment-conventions.md` applies to every source file in this stack and is a
  gate: a comment in a source file fails the change.

## What a blocking finding is

A review flags a change only for: a correctness bug, a security hole, data loss or a crash; a
regression the issue did not ask for; a break of a contract or interface without its accompanying
change; a rule in **Blocking in review** below; or an unmet acceptance criterion. Style preferences,
alternative designs and "consider" remarks are not findings and are never posted.

## Project-specific

**Package Manager** and **Gate Commands** are registered once in `CLAUDE.md` under
`## Project-specific`, with **Stack** naming every stack in the repo. Read them there; they are not
restated here.

What lives here is the standard itself — the **Scope** this skill governs and the rules a change is
held to. Keep it free of counts, versions, issue numbers, file inventories and dates; those move.

- **Scope**: `backend/`

**Architecture rules**:
- Package root is `com.arena`; each feature (`venue`, `hold`, `pricing`, `order`, `ws`) is a
  top-level package under it.
- A feature is layered `controller -> service -> repository -> entity`, with `dto` records and an
  `exception` package as needed. Controllers hold no business logic and never expose entities.
- Price is computed in `PricingService` and nowhere else. Every other class asks it.
- Seat changes reach browsers through an application event published from the service and sent by a
  `@TransactionalEventListener(phase = AFTER_COMMIT)` listener, never by a direct broker call inside
  the transaction.

**Concurrency and hold rules**:
- A hold is taken by one conditional `UPDATE ... WHERE status = 'AVAILABLE' OR (status = 'HELD' AND
  hold_expires_at < :now)` and its affected-row count is compared with the seats asked for. A
  mismatch throws and rolls the whole hold back, so a multi-seat hold is all or nothing.
- Never read a seat's status in Java and then write it in a separate statement; the check and the
  write are one statement or happen under `SELECT ... FOR UPDATE`.
- Rows locked together are locked in ascending id order.
- No `synchronized`, in-memory lock or JVM-local map stands in for a database guarantee.
- Every hold carries an expiry. Expired holds count as free in every query that decides
  availability, and a scheduled job releases them and broadcasts the change.
- Checkout locks the caller's seats, rejects any seat not held by the caller or already expired, and
  charges the `held_price` frozen at hold time.
- Time comes from an injected `Clock`, never `Instant.now()` or `LocalDateTime.now()` directly, so
  expiry is testable.

**State, data and API rules**:
- Schema changes are Flyway migrations under `src/main/resources/db/migration`; `ddl-auto` stays
  `validate`. A released migration is never edited.
- Money is a `long` of cents, never `double`, `float` or `BigDecimal`.
- Enums persist as `@Enumerated(EnumType.STRING)`.
- A read-only service method carries `@Transactional(readOnly = true)`.
- A `@Query` binds its parameters and is never assembled by string concatenation; a bulk update is
  `@Modifying` and clears the persistence context.
- Paths sit under `/api/{resource}` with the HTTP verb the operation actually is. Request bodies are
  `@Valid` records.
- Errors leave through one `@RestControllerAdvice` as `{code, message}` with 400, 404, 409 or 410.
  A domain exception carries its status and code; no bare `RuntimeException`, no empty catch, no
  `Optional.get()`.
- The customer id is the subject of the verified JWT (`CurrentCustomer.id(jwt)`), never a header,
  body or path value. Passwords are BCrypt hashes and are never logged or returned.
- Dependency injection is constructor injection via `@RequiredArgsConstructor`. Logging is `@Slf4j`
  with parameterized messages.

**Testing rules**:
- Every code path a change adds or modifies has a test.
- Concurrency is proven with a `@SpringBootTest` that starts many threads on a `CountDownLatch` against
  the same seat and asserts exactly one winner.
- Expiry is tested by moving a fixed `Clock`, never `Thread.sleep`.
- Service tests use `@ExtendWith(MockitoExtension.class)`; controller tests use `@WebMvcTest` with
  `MockMvc`.
- A test arranges, acts and asserts in that order, separated by blank lines and named locals.
- Exceptions are asserted with `assertThrows`; async work is waited on with Awaitility.

**Blocking in review**:
- A seat status read and written in separate statements without a lock, or any path that lets two
  customers hold or buy the same seat.
- A hold that can outlive its expiry, or an availability query that ignores expired holds.
- A seat change broadcast before its transaction commits.
- Price computed outside `PricingService`, or checkout charging anything but the frozen held price.
- Money held as a floating type or `BigDecimal`.
- `synchronized` or an in-memory lock used for booking correctness.
- A customer id taken from anywhere but the verified JWT, or an endpoint that changes holds or orders
  without requiring sign-in.
- A schema change outside Flyway, or an edited released migration.
- A `@Query` assembled by string concatenation.
- A swallowed exception, a bare `Optional.get()`, or an error response leaking a stack trace or SQL.
- A changed code path with no test.
- Any comment in a source file, per `.claude/conventions/comment-conventions.md`.

**Not flagged**:
- Generated sources, and Lombok-generated boilerplate.
- Seed-data volume or layout in the seeder.
- Test data setup verbosity in `@BeforeEach`.

**Definition of done**:
- The gate commands are green.
- Holds are atomic, expire, and are released and broadcast.
- Every changed path has a test, including the concurrency test where booking changed.
