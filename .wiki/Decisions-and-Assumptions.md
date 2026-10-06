# Decisions and Assumptions

## Decisions

| Topic | Decision | Why |
|---|---|---|
| Repo layout | One repo with `backend/` and `frontend/` | The two sides ship together; a contract change lands in one PR |
| Backend | Java 21, Spring Boot 4.1 | Mature transactions, scheduling, and STOMP support |
| Frontend | React + TypeScript + Vite, zustand | Per-seat subscriptions keep re-renders to the seat that changed |
| Database | H2 in memory, `MODE=PostgreSQL` | No setup needed for the 2-hour build; the SQL stays portable to PostgreSQL |
| Schema | Flyway migrations, `ddl-auto: validate` | Schema changes are reviewed and repeatable |
| Hold correctness | One conditional `UPDATE` per hold, with the affected-row count checked | No read-then-write gap, and no reliance on in-memory locks |
| Hold length | 60 seconds, set by the server | The countdown in the browser always matches the server |
| Expiry | Expired holds count as free immediately, plus a clean-up job every 5 seconds | Correct even if the job is late; maps still update within moments |
| Money | `long` cents | No floating-point rounding errors |
| Price at checkout | The price frozen at hold time | The customer pays exactly what they saw |
| Identity | Username and password sign-in; the API takes the customer from a JWT (HS256, 8 hours) and the WebSocket checks it on `CONNECT` | Holds and orders belong to an account, not a browser, and can't be claimed by copying an id |
| Live updates | STOMP over WebSocket, one message per seat change, with a `seq` | Small messages that can be ordered and de-duplicated |
| Seat map | One SVG, memoized seats, delegated events, one shared tooltip | Thousands of seats without freezing |
| Styling | Tailwind CSS v4 with theme tokens for tier and seat colours | Utility classes keep 12,000 seats on a handful of class strings; tokens keep the colours in one place |
| Changelog | One root `CHANGELOG.md`, entries tagged `Backend:`, `Frontend:`, `Full stack:` or `Repo:` | One version per PR across both apps |

## Assumptions

These keep the scope small. If one turns out to be wrong, change it here and in the repo `CLAUDE.md`.

- **One event, one arena.** The venue is created by a seeder at startup. There's no admin screen.
- **No payment, password reset or roles.** Checkout creates an order; nothing is charged. Three demo accounts (`alice`, `bob`, `carol`, password `arena123`) are seeded.
- **Hold limit:** a customer can hold at most 8 seats at once.
- **Data is in memory.** Restarting the backend resets every hold and order.
- **One backend instance** in development. The hold logic is still written so that it stays correct with several instances.
