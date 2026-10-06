# Getting Started

> These commands are the target set up by issue [#1](https://github.com/amalps565/theater-arena-booking/issues/1). They work once it's merged.

## Prerequisites

- **JDK 21**. Maven isn't needed; the backend ships the Maven wrapper.
- **Node.js 24** and npm
- **Git** and the **GitHub CLI** (`gh`), used by the workflow skills
- **Claude Code** (optional), started from the repo root so the hooks and skills load

No database install or Docker is needed; the backend uses an in-memory H2 database.

## Clone

```bash
git clone https://github.com/amalps565/theater-arena-booking.git
cd theater-arena-booking
```

## Run locally

```bash
# 1. Backend on http://localhost:8080 (from backend/)
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run

# 2. Frontend on http://localhost:5173 (from frontend/, in a second terminal)
npm install
npm run dev
```

Open **http://localhost:5173**. The Vite dev server forwards `/api` and `/ws` to the backend.

To see the concurrency and live-update fixes, sign in as a different demo account in a second, private browser window and click the same seat in both.

## Gate commands

These must pass before a PR is raised. They're registered in `CLAUDE.md`.

```bash
# Backend (from backend/)
./mvnw spotless:apply
./mvnw test

# Frontend (from frontend/)
npm run lint
npm test -- --run
npm run build
```

## Hook tests

After changing anything in `.claude/hooks/`:

```bash
node .claude/hooks/tests/run-all.cjs
```

On Windows, 8 `protect-branches` cases about home-directory paths fail. They fail the same way in the Lambdabooks repos the hooks came from.
