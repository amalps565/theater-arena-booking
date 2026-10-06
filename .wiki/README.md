# Theater Arena Booking

An interactive **arena seating chart** for a high-profile concert. Customers open a visual map of the venue, hover over seats to see live prices, click to place a **60-second hold**, and check out.

> **Status:** designed and split into seven issues. Nothing is merged yet; #1 (scaffold) is in progress. See [Roadmap](Roadmap.md).

## The problem

The baseline skeleton has three critical flaws:

1. **Double-booking.** Two customers clicking the same seat at the same moment can both get it.
2. **UI freeze.** Rendering large seating sections locks up the page.
3. **Seats locked forever.** A customer who doesn't finish a purchase leaves their seats permanently held.

See [Problem Statement](Problem-Statement.md) for details.

## Pages

**Overview**
- [Problem Statement](Problem-Statement.md): the three flaws and the expected behaviour
- [Decisions and Assumptions](Decisions-and-Assumptions.md): stack, rules, and what was assumed
- [Getting Started](Getting-Started.md): prerequisites, running the app, and running the tests
- [Roadmap](Roadmap.md): the seven issues and their order

**Design**
- [Architecture](Architecture.md): how the backend, frontend, and database fit together
- [Data Model](Data-Model.md): tables, columns, and constraints
- [Seat Holds and Concurrency](Seat-Holds-and-Concurrency.md): the double-booking race and the atomic hold that fixes it
- [Hold Expiry and Checkout](Hold-Expiry-and-Checkout.md): how abandoned holds are released and how purchase works
- [Dynamic Pricing](Dynamic-Pricing.md): tier, row, and demand pricing
- [API Reference](API-Reference.md): REST endpoints and error codes
- [Real-Time Updates](Real-Time-Updates.md): the WebSocket contract and client resync
- [Frontend Design](Frontend-Design.md): the seat map, store, tooltip, and cart

**Process**
- [Testing Strategy](Testing-Strategy.md): test levels and the concurrency test recipe
- [Development Workflow](Development-Workflow.md): issues, branches, PRs, changelog, Claude Code skills and agents

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4.1, Spring Data JPA, Flyway, WebSocket (STOMP), Lombok |
| Frontend | React, TypeScript, Vite, zustand, `@stomp/stompjs` |
| Database | H2 in memory, in PostgreSQL compatibility mode |
| Testing | JUnit 5, Mockito, Awaitility, Vitest, Testing Library |
| Formatting and lint | Spotless (Google Java Format), ESLint |

---

`CLAUDE.md` and the `.claude/` conventions skills are the source of truth for rules and values. If these pages disagree with them, the repo wins and the page is corrected in the same PR.
