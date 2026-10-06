# Theater Arena Booking

[![CI](https://github.com/amalps565/theater-arena-booking/actions/workflows/ci.yml/badge.svg)](https://github.com/amalps565/theater-arena-booking/actions/workflows/ci.yml)

An interactive seating chart for a concert arena. Customers see all 12,000 seats on one map, hover a seat to see its live price, click to hold it for 60 seconds, and check out. Prices rise for better tiers, front rows and sections that are filling up, and every open map updates live as other people hold, release and buy seats.

React + TypeScript + Tailwind on the front, Spring Boot 4 on Java 21 behind it, talking over REST and STOMP WebSockets.

## The three flaws, and how they're fixed

| Flaw | Fix | Details |
|---|---|---|
| Two customers clicking the same seat at once could both get it | A hold is one conditional `UPDATE` that only succeeds while the seat is free, so the database lets exactly one request win | [Seat Holds and Concurrency](.wiki/Seat-Holds-and-Concurrency.md) |
| The page froze when drawing large sections | One SVG; each seat redraws only when it changes; one event handler and one tooltip for the whole map | [Frontend Design](.wiki/Frontend-Design.md) |
| An abandoned purchase locked seats forever | Holds expire after 60 seconds, count as free the moment they expire, and a job releases them every 5 seconds | [Hold Expiry and Checkout](.wiki/Hold-Expiry-and-Checkout.md) |

## Run it

You need **JDK 21**, **Node.js 24** with npm, and **Git**. No database or Docker is needed; the backend uses an in-memory database.

```bash
git clone https://github.com/amalps565/theater-arena-booking.git
cd theater-arena-booking
```

Backend, on http://localhost:8080:

```bash
cd backend
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run
```

Frontend, on http://localhost:5173, in a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**. The dev server forwards `/api` and `/ws` to the backend.

Sign in with a demo account, **alice**, **bob** or **carol**, all with the password `arena123`, or create your own account on the sign-in page. Data lives in memory, so restarting the backend resets every hold, order and new account, and signs everyone out.

## See the fixes in action

1. Hover any seat: the tooltip shows its section, row, seat, status and live price.
2. Click a seat. It turns blue and appears in **Your seats** with a 60-second countdown.
3. Open the app in a **private window** next to the first one and sign in as a different demo account. The seat you held is grey there, and clicking it says it's on hold.
4. Hold a seat in one window and watch it change colour in the other within moments. Check out, and it shows as sold in both.
5. Hold a seat and wait a minute without checking out. It leaves your cart and comes back on sale in both windows.

## Before you open a pull request

These are the checks CI runs on every pull request:

```bash
cd backend
./mvnw spotless:apply
./mvnw test

cd ../frontend
npm run lint
npm test -- --run
npm run build
```

## Learn more

- [Wiki](.wiki/README.md): problem statement, architecture, data model, API and WebSocket contract, pricing, testing
- [Development workflow](.wiki/Development-Workflow.md): issues, branches, pull requests, CI and the Claude Code skills
- [Changelog](CHANGELOG.md): what changed in each version
- [`CLAUDE.md`](CLAUDE.md): the repository's rules and values, which win if anything here disagrees
