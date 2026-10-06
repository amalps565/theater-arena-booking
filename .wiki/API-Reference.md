# API Reference

> REST base path `/api`. The WebSocket contract is in [Real-Time Updates](Real-Time-Updates.md). A change to either contract updates the backend, the frontend, and this page in the same PR.

## Authentication

Customers sign in with a username and password and receive a JWT (HS256, valid 8 hours). Every other call except `GET /api/venue` sends it as `Authorization: Bearer <token>`. The customer is the token's subject, the user's UUID, and is never taken from a request body or path. A missing, expired or forged token returns **401 `UNAUTHENTICATED`**.

Demo accounts `alice`, `bob` and `carol` are seeded at startup, all with the password `arena123`. Passwords are stored only as BCrypt hashes. The signing key comes from `arena.auth.jwt-secret` when it is at least 32 bytes; otherwise a random key is generated at startup, so tokens end when the server restarts.

## Endpoints

| Method | Path | Body | Success | Errors |
|---|---|---|---|---|
| `POST` | `/api/auth/login` | `{ "username": "alice", "password": "…" }` | 200 token | 400, 401 |
| `POST` | `/api/auth/register` | `{ "username": "fan_1", "displayName": "Fan", "password": "…" }` | 201 token | 400, 409 |
| `GET` | `/api/auth/me` | — | 200 user | 401 |
| `GET` | `/api/venue` | — | 200 venue snapshot (no sign-in needed) | — |
| `POST` | `/api/holds` | `{ "seatIds": [101, 102] }` | 201 holds | 400, 404, 409 |
| `DELETE` | `/api/holds/{seatId}` | — | 204 | 404 |
| `GET` | `/api/holds/me` | — | 200 holds | — |
| `POST` | `/api/checkout` | — | 201 order | 400, 410 |

### `POST /api/auth/login` and `POST /api/auth/register`

```json
{
  "token": "eyJ…",
  "expiresAt": "2026-10-06T20:00:00Z",
  "user": { "id": "0b6f…", "username": "alice", "displayName": "Alice Anders" }
}
```

Usernames are 3–32 letters, digits or underscores and are stored in lower case. Passwords are 8–72 characters.

### `GET /api/venue`

Seats are sent as arrays rather than objects, and the response is gzip-compressed: about 530 KB of JSON for 12,000 seats travels as about 65 KB. A held seat whose hold has expired is reported as `AVAILABLE`. `priceSeq` is the section's latest price-update sequence; see [Real-Time Updates](Real-Time-Updates.md).

```json
{
  "sections": [
    { "id": 2, "name": "Center VIP", "tier": "VIP", "basePriceCents": 15000, "priceSeq": 0 }
  ],
  "seatFields": ["id", "sectionId", "x", "y", "row", "number", "status", "priceCents", "seq"],
  "seats": [
    [651, 2, 666, 126, "A", 1, "AVAILABLE", 19500, 0]
  ]
}
```

### `POST /api/holds`

```json
{
  "expiresAt": "2026-10-06T14:03:21Z",
  "holds": [
    { "seatId": 101, "priceCents": 19500 },
    { "seatId": 102, "priceCents": 19500 }
  ]
}
```

All seats are held, or none are.

### `GET /api/holds/me`

```json
{ "holds": [{ "seatId": 101, "priceCents": 19500, "expiresAt": "2026-10-06T14:03:21Z" }] }
```

It lists only holds that haven't expired.

### `POST /api/checkout`

```json
{
  "orderId": 7,
  "totalCents": 39000,
  "items": [
    { "seatId": 101, "priceCents": 19500 },
    { "seatId": 102, "priceCents": 19500 }
  ]
}
```

## Errors

Every error has the same body:

```json
{ "code": "SEAT_TAKEN", "message": "Seat A12 was just taken by someone else." }
```

| Status | Code | When |
|---|---|---|
| 400 | `VALIDATION_FAILED` | The body is invalid, for example an empty `seatIds` list |
| 400 | `NOTHING_TO_CHECK_OUT` | Checkout with no current holds |
| 401 | `UNAUTHENTICATED` | No token, or the token is expired or forged |
| 401 | `INVALID_CREDENTIALS` | Wrong username or password |
| 404 | `SEAT_NOT_FOUND` | A requested seat id doesn't exist |
| 404 | `HOLD_NOT_FOUND` | Releasing a seat the customer doesn't hold |
| 409 | `SEAT_TAKEN` | At least one requested seat is held by someone else or sold |
| 409 | `USERNAME_TAKEN` | Registering a username that already exists |
| 409 | `HOLD_LIMIT_REACHED` | The hold would take the customer over 8 seats |
| 410 | `HOLD_EXPIRED` | Checkout after one of the customer's holds expired |

Error responses never include stack traces, SQL, or another customer's id.
