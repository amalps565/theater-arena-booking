# Changelog

All notable changes to this project will be documented in this file.

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
