# Frontend Design

> Vite + React + TypeScript (strict). The rules a review checks are in the `react-conventions` skill. Built in issues [#6](https://github.com/amalps565/theater-arena-booking/issues/6) (seat map) and [#7](https://github.com/amalps565/theater-arena-booking/issues/7) (holds, cart, live updates).

## Why the old map froze

Each seat was its own component with its own `onMouseEnter`, `onMouseLeave`, and `onClick` handlers, and every change re-rendered the whole map. With 12,000 seats, that's 36,000 listeners, and every hover or update reconciles every seat.

## Seat map rules

| Rule | Effect |
|---|---|
| **One SVG** for the whole arena, with zoom and pan as a transform on one `<g>` | Panning moves one element, not 12,000 |
| **`SeatDot` is `React.memo`** and selects only its own seat: `useSeatStore(s => s.seats[id])` | A change to one seat re-renders one circle |
| **Delegated events:** one `onPointerOver`, `onPointerOut`, and `onClick` on the root `<svg>`, reading `data-seat-id` | Three listeners instead of 36,000 |
| **One shared tooltip**, positioned through a ref inside `requestAnimationFrame` | Hovering never re-renders the map |
| **Batched socket updates**, applied once per animation frame | A burst of changes causes one render |
| **No derived per-seat arrays** rebuilt on each render | Rendering stays proportional to what changed |

If SVG still isn't fast enough, the fallback is a `<canvas>` with grid-bucket hit-testing behind the same store.

## Store

One zustand store:

```ts
{
  sections: Record<number, Section>;
  seats: Record<number, Seat>;          // id, sectionId, x, y, row, number, status, priceCents, seq
  sectionSeq: Record<number, number>;
  myHolds: Record<number, { priceCents: number; expiresAt: string }>;
  connection: 'connecting' | 'live' | 'reconnecting';
}
```

- Socket updates go through `applySeatUpdate` and `applyPrices`, which enforce the `seq` rule. See [Real-Time Updates](Real-Time-Updates.md).
- Store logic has unit tests.

## Components

| Component | Responsibility |
|---|---|
| `App` | Layout: header, map, cart, toasts |
| `SeatMap` | The SVG, zoom and pan, delegated events, legend |
| `SeatDot` | One memoized circle with `data-seat-id`, coloured by tier and status |
| `Tooltip` | Section, row, seat, status, and live price for the hovered seat |
| `CartPanel` | Held seats, prices, total, a countdown per hold, Release and Checkout |
| `Toasts` | Messages for 409 and 410 errors and confirmed orders, for example "Seat A12 was just taken" |
| `ConnectionBadge` | "Live", "Connecting…" or "Reconnecting… map may be out of date" |
| `Legend` | Tier and status colours |

## Styling and colours

Styling is Tailwind CSS v4. The colours below are theme tokens in `src/index.css` (`--color-tier-vip`, `--color-seat-mine`, …), used as classes such as `fill-tier-vip` on seats and `bg-seat-mine` in the legend.

| State | Colour |
|---|---|
| Available | By tier (VIP, Premium, Standard) |
| Held by me | Blue |
| Held by someone else | Grey |
| Sold | Dark |

## Holding and checkout

- Clicking a free seat sends `POST /api/holds`. The seat shows as **mine only after the server confirms it**; there's no optimistic hold.
- A 409 shows a toast, and the seat keeps the status the server reports.
- Countdowns are computed from the server's `expiresAt`, never from a timer started at the click.
- At zero, the seat leaves the cart. The server's expiry message then shows it as free.
- Checkout sends `POST /api/checkout`. A 410 shows "your hold expired", and the cart reloads from `GET /api/holds/me`.

## API calls

All REST calls go through `src/api/client.ts`. It attaches the signed-in customer's `Authorization: Bearer` token, signs out on a 401, and turns `{code, message}` errors into typed errors. No component calls `fetch` or builds a URL itself.
