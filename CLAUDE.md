# CLAUDE.md

Thin router for this repository. It stays small and loads every session; everything else loads on
demand. When work starts from an issue, run `/start-issue <N>` first and load only what that work
needs. Do not pre-read the convention skills "just in case".

## Routing (run these even if the user never typed the command)

| The user wants to… | Run |
|---|---|
| decide what to work on next ("what's next", "triage the backlog") | `/triage-issues` |
| start an issue (a pasted issue URL or number, "work on this", "pick up #N") | `/start-issue <N>` |
| create an issue ("file an issue", "raise a ticket", "log this bug", "track this") | `/create-issue` |
| open a PR ("raise the PR", "open the PR") | `/raise-pr` |
| review a PR ("review this PR", a pasted PR URL) | `/pr-review <N>` |
| act on a review ("fix the review comments on PR N", "address the review") | `/fix-review-comments <N>` |

`/start-issue` maps the issue's labels and the folders it touches to the stack conventions in
**Label Routing** below and loads only those. Stack conventions are skills named
`<stack>-conventions`; they are never pre-read.

## Guardrails (hooks)

`.claude/hooks/` block secrets on write, block comments in source files, block commits to protected
branches, block unsafe shell commands, and lint touched files (Spotless for `backend/`, ESLint for
`frontend/`). They fire automatically. If one blocks you, fix the cause; do not work around it.
Run `node .claude/hooks/tests/run-all.cjs` after changing a hook.

## Where the rules live

| Topic | Source |
|---|---|
| Stack standards, gate commands, what review flags | the `<stack>-conventions` skill(s) named below |
| Comments in source files | `.claude/conventions/comment-conventions.md` |
| Git, commit and branch hygiene | `.claude/conventions/git-hygiene.md` |
| Issues, PR titles, boards, merge rules | `.claude/conventions/github-pr.md` |
| Review method and the review body shape | `.claude/conventions/review-conventions.md`, `review-template.md` |
| Reviewer agents | `.claude/agents/` |

Each rule lives in one place; everything else points there. Do not repeat a rule here.

## Project-specific

This is the **only** register of this repo's values. Every convention file and skill that needs one
reads it here; none of them restates it, so a value changes in exactly one place. The exception is
each `<stack>-conventions` skill, which holds the rules and the **Scope** of its own stack.

- **Repo**: amalps565/theater-arena-booking — interactive arena seat map with live dynamic pricing,
  60-second seat holds and checkout.
- **Stack**: monorepo. `backend/`: Spring Boot over H2 with Flyway, STOMP over WebSocket.
  `frontend/`: Vite + React + TypeScript SPA with zustand and `@stomp/stompjs`.
- **Package Manager**: maven wrapper in `backend/`; npm in `frontend/`
- **Default Branch**: `main`
- **Protected Branches**: `main`, `master`
- **Promotion Chain**: `feature -> main`
- **Branch Name**: `<N>-short-slug`
- **Ticket Prefix**: NULL
- **PR Title**: `#<N> | <short description>` — spaces around the pipe.
- **PR Title Regex**: NULL
- **PR Template**: NULL
- **Project Board**: NULL
- **Assign On Start**: yes
- **Milestones**: no
- **Gate Commands**:
  - `cd backend && ./mvnw spotless:apply && ./mvnw test`
  - `cd frontend && npm run lint && npm test -- --run && npm run build`
- **Version Bump**: one root `CHANGELOG.md` for the whole repo, never one per folder.
  `## [x.y.z] - YYYY-MM-DD` (Added/Changed/Fixed/Removed), exactly one patch above the PR's base,
  normally `main`. Each entry opens with a bold one-line outcome a non-developer can read, prefixed
  `Backend:`, `Frontend:` or `Full stack:` by the area it changes (`Repo:` for tooling and setup),
  then says what changed and why, and ends with
  `[#<N>](https://github.com/amalps565/theater-arena-booking/issues/<N>)`. `backend/pom.xml`
  `<version>` and `frontend/package.json` `version` must equal the heading.
- **Merge Method**: merge commit — `gh pr merge <N> --merge`. Close the linked issue through the
  PR's `Closes #N`.
- **Conventions Skills**: `java-conventions`, `react-conventions`
- **Reviewer Agents**: `senior-java-engineer`, `senior-react-engineer`
- **Label Routing**:
  - `backend` label or any change under `backend/` → `java-conventions`, `senior-java-engineer`
  - `frontend` label or any change under `frontend/` → `react-conventions`, `senior-react-engineer`
- **Repo-specific skills**: NULL
- **Siblings**: NULL — backend and frontend live in this repo.
