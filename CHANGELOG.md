# Changelog

All notable changes to this project will be documented in this file.

## [0.0.9] - 2026-10-06

### Changed

- **Repo: CI now enforces the repository's own rules, following the Lambdabooks CI.** A new
  `Quality` job checks that `backend/pom.xml`, `frontend/package.json` and this changelog carry the
  same version and that a PR moves it by exactly one +1 increment (`scripts/validate-versions.cjs`),
  that the PR title matches `#<N> | <description>` in 10–100 characters, that no source file gains a
  comment, that every guardrail hook test passes, and that gitleaks finds no secrets
  (`.gitleaks.toml`). The backend and frontend jobs keep their checks, the frontend job also runs the
  react-lint hook suite against the installed ESLint, and Node comes from `frontend/.nvmrc`. Steps run
  with `if: !cancelled()` so one failure does not hide the rest, every job has a timeout, and a new
  push to a PR cancels the run it replaces. `CLAUDE.md` now registers the PR title regex.
  [#13](https://github.com/amalps565/theater-arena-booking/issues/13)

## [0.0.8] - 2026-10-06

### Added

- **Repo: Newcomers can understand and run the app from the repository's front page.** A root
  `README.md` explains what the app does, names the three flaws from the brief with a one-line fix
  and a link to the page that explains each, lists the prerequisites, and gives copy-paste commands
  to run the backend and frontend. A five-step walkthrough shows the fixes in action with a second,
  private browser window, and the pre-PR commands match what CI runs. It links to the wiki, the
  development workflow, this changelog and `CLAUDE.md` rather than repeating them, and shows the CI
  status badge. Every command was run on a fresh clone.
  [#14](https://github.com/amalps565/theater-arena-booking/issues/14)

## [0.0.7] - 2026-10-06

### Added

- **Repo: Every pull request to `main`, and every push to it, is checked automatically.**
  `.github/workflows/ci.yml` runs three separate checks: the backend's Spotless format check and
  test suite, the frontend's lint with no warnings allowed plus its tests and production build, and
  `scripts/check-added-comments.cjs` against the PR base so no source file gains a comment. Both apps
  are checked on every PR, so a contract break between them is caught whichever side changed. Maven
  and npm downloads are cached between runs. `backend/mvnw` is now marked executable so it runs on
  the Linux runner, and the workflow is documented in `.wiki/Development-Workflow.md`.
  [#13](https://github.com/amalps565/theater-arena-booking/issues/13)

## [0.0.6] - 2026-10-06

### Fixed

- **Repo: Every text file is checked out with LF line endings, whatever the machine's git
  settings.** With `core.autocrlf=true`, Windows checkouts turned files into CRLF, so scripted edits
  that matched on `\n` silently changed nothing, as happened to the 0.0.5 changelog entry. A root
  `.gitattributes` now sets `* text=auto eol=lf`, keeps `.cmd`, `.bat` and `.ps1` as CRLF, and marks
  images, fonts and jars as binary. It replaces `backend/.gitattributes`, so the rule lives in one
  place. The files stored in git were already LF, so no file content changed.

## [0.0.5] - 2026-10-06

### Fixed

- **Repo: The roadmap shows #6 and #7 as merged.** `.wiki/Roadmap.md` still listed both as "Built in
  PR 3" because the PR number was not known when the docs were written; they now link to PR #10, so
  every issue on the roadmap reads as merged.

## [0.0.4] - 2026-10-06

### Added

- **Frontend: Customers can hold seats, watch each hold count down, and check out, while the map
  updates live.** Clicking a free seat holds it and it turns blue only once the server confirms. A
  cart lists each held seat with its price, a total, and a countdown taken from the server's
  `expiresAt`; at zero the seat leaves the cart. Seats can be released, and checkout confirms the
  order with its total. A seat someone else took, or a hold that expired, shows a clear message. One
  hook owns the STOMP connection: it subscribes first, buffers, loads the snapshot, applies only
  updates with a newer `seq`, batches them once per animation frame, and reloads after every
  reconnect while a badge says the map may be out of date. Styling is Tailwind CSS v4, with tier and
  seat colours as theme tokens, and the React conventions now say so.
  [#7](https://github.com/amalps565/theater-arena-booking/issues/7)

### Fixed

- **Frontend: The seat map stays responsive while showing and hovering all 12,000 seats.** The arena
  is one SVG. Each seat is a memoized component that subscribes to its own seat only, so one change
  redraws one circle. Pointer and click events are handled once on the map root through
  `data-seat-id` rather than on every seat, a single tooltip shows the hovered seat's section, row,
  number, status and live price, and zoom and pan move one SVG group without a React render. In a
  headless browser, 200 rapid seat hovers produced no task over 50 ms.
  [#6](https://github.com/amalps565/theater-arena-booking/issues/6)

## [0.0.3] - 2026-10-06

### Added

- **Backend: Customers can load the whole arena with a live price for every seat.**
  `GET /api/venue` returns six sections in three tiers and 12,000 seats; Flyway creates the schema
  and a startup seeder fills it. Seats travel as compact arrays and the response is gzip-compressed, so the
  snapshot is about 65 KB on the wire. `PricingService` is the only place prices are computed: base
  price by tier, 1.3× for the front row falling to 1.0× at the back, and 1.15× or 1.35× once a
  section is more than half or four-fifths full, rounded to 50 cents and held as `long` cents. A
  hold that has expired is reported as available.
  [#2](https://github.com/amalps565/theater-arena-booking/issues/2)
- **Backend: Seats that someone abandons come back on sale, and held seats can be bought at the
  price shown.** Every hold lasts 60 seconds. An expired hold counts as free at once in every
  availability check, and a job releases expired holds every 5 seconds. `POST /api/checkout` locks
  the customer's seats, refuses with `410 HOLD_EXPIRED` if any hold ran out, and otherwise marks them
  sold and records an order at the frozen hold prices. A unique seat per order item stops any seat
  being sold twice. [#4](https://github.com/amalps565/theater-arena-booking/issues/4)
- **Backend: Open seat maps receive seat and price changes as they happen.** STOMP over WebSocket
  at `/ws` broadcasts one `SEAT` message per changed seat on `/topic/venue`, and a `PRICES` message
  for a section whose demand band changed. Messages are sent only after the transaction commits, so a
  failed hold is never broadcast, and each carries a `seq` so clients can drop stale updates.
  Clients may only subscribe. [#5](https://github.com/amalps565/theater-arena-booking/issues/5)

### Fixed

- **Backend: Two customers can no longer hold the same seat at the same time.** A hold is one
  conditional `UPDATE` that only succeeds while the seat is free or its hold has expired, and the
  number of rows changed is checked, so the database lets exactly one request win. A request for
  several seats holds all of them or none, and losers get `409 SEAT_TAKEN`. A customer can hold up
  to 8 seats, release one with `DELETE /api/holds/{seatId}`, and list theirs with
  `GET /api/holds/me`. A test fires 20 simultaneous holds at one seat, five times over, and always
  sees exactly one winner. [#3](https://github.com/amalps565/theater-arena-booking/issues/3)

## [0.0.2] - 2026-10-06

### Added

- **Full stack: Developers can build, test and run the backend and the frontend.** `backend/` is a
  Spring Boot 4.1 app on Java 21 with Web MVC, JPA, Flyway, WebSocket, validation and actuator over an
  in-memory H2 database in PostgreSQL mode, with `ddl-auto: validate` so only Flyway changes the
  schema. Spotless with Google Java Format runs on `verify`, and Awaitility is available for async
  tests. `frontend/` is a Vite + React + TypeScript app in strict mode with zustand,
  `@stomp/stompjs`, Vitest, Testing Library and ESLint; Vite's default oxlint was swapped for ESLint
  so the lint hook and the gate commands use the same tool. The dev server proxies `/api` and `/ws` to
  the backend on port 8080. The backend, the frontend and this changelog all carry `0.0.2`.
  [#1](https://github.com/amalps565/theater-arena-booking/issues/1)
- **Repo: The design is documented in `.wiki/`, versioned with the code.** Fifteen pages cover the
  problem statement, decisions, architecture, data model, the atomic hold that stops double-booking,
  hold expiry and checkout, dynamic pricing, the REST and WebSocket contract, the seat map design,
  testing and the workflow. `CLAUDE.md` points to them and requires a PR that changes the contract,
  schema or pricing to update the matching page.
  [#1](https://github.com/amalps565/theater-arena-booking/issues/1)

## [0.0.1] - 2026-10-06

### Added

- **Repo: The repository is set up for Claude Code, with guardrails for both the backend and the frontend.**
  `CLAUDE.md` routes issue, PR and review work to the `create-issue`, `start-issue`, `raise-pr`,
  `pr-review`, `fix-review-comments` and `triage-issues` skills, and registers the repo's branch, gate,
  version and merge values in one place. The `java-conventions` and `react-conventions` skills hold
  `backend/` and `frontend/` to the rules this app depends on: atomic seat holds, hold expiry, pricing
  in one service, and a seat map that re-renders only the seats that change. Hooks block secrets,
  source comments, commits to `main` and unsafe shell commands, and lint edited files with Spotless and
  ESLint; `node .claude/hooks/tests/run-all.cjs` tests them.
- **Repo: Build output, dependencies, local settings and `.env` files stay out of the repository.** A
  `.gitignore` covers `backend/target/`, `frontend/node_modules/`, `frontend/dist/`,
  `.claude/settings.local.json` and every `.env` file except `.env.example`.
- **Repo: Every change to the backend and the frontend is recorded in this one changelog.** Each PR
  adds one version heading here, whichever folders it touches, and each entry names its area.
  `CLAUDE.md` registers the rule under **Version Bump**.
