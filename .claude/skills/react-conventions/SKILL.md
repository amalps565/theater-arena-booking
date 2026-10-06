---
name: react-conventions
description: >-
  Engineering standard for the React code in this repo. States the bar code is held to, what a
  review flags as blocking, and the gate commands that must pass. Loaded on demand by /start-issue,
  /pr-review and /fix-review-comments for issues and diffs that touch React code; never pre-read.
---

# React conventions

This skill is a **standard, not a description**: it states the bar this repo holds `React` code to,
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

- **Scope**: `frontend/`

This app is a client of the backend REST and STOMP API: hold, expiry and pricing rules belong to the
backend, and this app displays what the server decides.

**Architecture rules**:
- TypeScript in strict mode; no `any` in a changed line.
- Every REST call goes through `src/api/client.ts`, which attaches the `X-Customer-Id` header and maps
  `{code, message}` errors. No component calls `fetch` itself or hardcodes a URL.
- Seat state lives in one zustand store, normalized by seat id. A component selects only the slice it
  renders.
- The STOMP connection is owned by one hook; components never open their own socket.
- A reusable component lives in `src/components/` with its test beside it; a hook lives in
  `src/hooks/` and is named `use*`.
- Styling is Tailwind CSS v4 utility classes. Colours the app names (tiers, seat states) are theme
  tokens in the `@theme` block of `src/index.css`, used as `fill-tier-vip`, `bg-seat-mine` and so
  on, never hex values in components. A class that depends on state is picked from a lookup of full
  class strings, never assembled from fragments, so Tailwind can see it. No CSS modules, no other CSS
  files, and no inline `style` except a transform or position written imperatively through a ref.

**Seat map performance rules**:
- A seat is a `React.memo` component that subscribes to its own seat only, so one update re-renders
  one seat.
- Pointer and click handling is delegated to the map root and resolved through `data-seat-id`. No
  per-seat event handler.
- One shared tooltip is positioned imperatively through a ref; hovering never re-renders the map.
- Socket updates are applied in batches per animation frame.
- No derived per-seat array or object is rebuilt on every render of the map.

**State and data rules**:
- A socket update is applied only when its `seq` is greater than the stored `seq` for that seat.
- Subscribe first, buffer, then load the snapshot; reload the snapshot after every reconnect and show
  that the map is stale while disconnected.
- Hold countdowns are computed from the server's `expiresAt`, never a client-side timer started at
  click.
- The UI never marks a seat as held before the server confirms it; a 409 or 410 is shown to the user
  and the seat reverts.
- An effect declares every dependency it reads and cleans up subscriptions, timers and animation
  frames on unmount.

**Testing rules**:
- Tests use Vitest and Testing Library; a test drives the UI through `userEvent` and asserts what the
  user can observe.
- Store logic, such as the `seq` rule and batching, has unit tests.

**Blocking in review**:
- A per-seat event handler, a non-memoized seat, or a selector that re-renders the whole map on one
  seat change.
- A socket update applied without the `seq` check, or no snapshot reload after reconnect.
- A countdown or hold state not derived from the server response.
- A component calling `fetch` directly or bypassing the customer-id header.
- An effect with a missing dependency or no cleanup, causing a leak or a stale value.
- User-supplied data rendered as raw HTML.
- A secret in source or personal data written to the console.
- A comment in a changed source file.

**Definition of done**:
- The gate commands registered in `CLAUDE.md` pass.
- Hovering and panning the full arena causes no long task in the browser profiler.
- Every new or changed component and store rule has a test.
