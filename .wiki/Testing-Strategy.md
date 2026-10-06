# Testing Strategy

> The gate commands are in `CLAUDE.md` and on [Getting Started](Getting-Started.md). Every code path a change adds or modifies needs a test.

## Test levels

| Level | Tool | What it covers |
|---|---|---|
| Backend unit | JUnit 5 + Mockito | `PricingService` (tiers, row ends, demand thresholds, rounding); service rules with mocked repositories |
| Backend web | `@WebMvcTest` + `MockMvc` | Every endpoint's status codes, error codes, validation, and sign-in (`AuthFlowTest`: tokens, 401s, forged tokens, holds owned by the account) |
| Backend integration | `@SpringBootTest` on H2 | Hold, release, expiry, and checkout against the real schema |
| Backend concurrency | `@SpringBootTest` + `CountDownLatch` | The double-booking fix (recipe below) |
| Frontend unit | Vitest | Store rules: `seq` ordering, batching, buffering before the snapshot |
| Frontend component | Vitest + Testing Library + `userEvent` | Tooltip, hold on click, cart countdown, error toasts |
| Manual | Two browser windows + DevTools Performance | Same-seat click, expiry in both windows, no long tasks while hovering |

## Concurrency test recipe

The double-booking fix isn't done until this passes repeatedly.

1. Seed a venue and pick one free seat.
2. Create N threads (for example 20), each with its own customer UUID.
3. Hold every thread at a `CountDownLatch` start gate, then release them together.
4. **Scenario A, same seat:** every thread holds the same seat.
5. **Scenario B, overlapping sets:** threads hold overlapping pairs of seats, such as {1,2}, {2,3}, and {3,4}.
6. Assert:
   - Scenario A: exactly one success, every other thread got `SEAT_TAKEN`, and the database has exactly one `HELD` row for the seat.
   - Scenario B: no seat is held by two customers, every request either holds all its seats or none, and there were no 500s or deadlocks.
7. Repeat with `@RepeatedTest` (for example 20 times). One pass proves little.

H2's row locking is weaker evidence than PostgreSQL's. If the project moves to PostgreSQL, run this test against it with Testcontainers.

## Expiry and checkout cases

All of these use a fixed `Clock` that the test moves forward. None of them sleep.

- A hold placed at T is free to another customer at T+61s, before the job runs.
- After the job runs at T+61s, the seat is `AVAILABLE` and a `SEAT` update is published.
- Checkout at T+30s buys the seats at their frozen prices, even if demand changed the live price.
- Checkout at T+61s returns `410 HOLD_EXPIRED` and buys nothing.
- A sold seat can't be held or bought again.

## Publishing cases

- A successful hold publishes one `SEAT` message per seat, after commit.
- A failed hold (409) publishes nothing.
- Crossing a demand threshold publishes a `PRICES` message for that section.

## Frontend cases

- An update with `seq` ≤ the stored `seq` is ignored.
- Messages that arrive before the snapshot are applied after it, in `seq` order.
- One seat update re-renders one `SeatDot` (checked with a render counter in the test).
- Hovering a seat shows its section, row, number, status, and price.
- A 409 on hold shows a toast and doesn't mark the seat as mine.
- The countdown follows `expiresAt`, and the seat leaves the cart at zero.
