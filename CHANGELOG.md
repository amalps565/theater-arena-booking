# Changelog

All notable changes to this project will be documented in this file.

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
