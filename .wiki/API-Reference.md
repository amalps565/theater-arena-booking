# API Reference

> REST base path `/api`. The WebSocket contract is in [Real-Time Updates](Real-Time-Updates.md). A change to either contract updates the backend, the frontend, and this page in the same PR.

## Customer identity

Every request sends `X-Customer-Id: <uuid>`. The browser creates the id once and keeps it in `localStorage`. A missing or malformed id returns **400 `INVALID_CUSTOMER_ID`**. The id is never taken from a request body.

## Endpoints

| Method | Path | Body | Success | Errors |
|---|---|---|---|---|
| `GET` | `/api/venue` | — | 200 venue snapshot | — |
| `POST` | `/api/holds` | `{ "seatIds": [101, 102] }` | 201 holds | 400, 404, 409 |
| `DELETE` | `/api/holds/{seatId}` | — | 204 | 404 |
| `GET` | `/api/holds/me` | — | 200 holds | — |
| `POST` | `/api/checkout` | — | 201 order | 400, 410 |

### `GET /api/venue`

Seats are sent as arrays rather than objects, which keeps a 12,000-seat response small.

```json
{
  "sections": [
    { "id": 1, "name": "Floor A", "tier": "VIP", "basePriceCents": 15000 }
  ],
  "seatFields": ["id", "sectionId", "x", "y", "row", "number", "status", "priceCents", "seq"],
  "seats": [
    [101, 1, 120, 340, "A", 12, "AVAILABLE", 19500, 0]
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

The same shape as the hold response, with `expiresAt` given per hold. It lists only holds that haven't expired.

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
| 400 | `INVALID_CUSTOMER_ID` | `X-Customer-Id` is missing or isn't a UUID |
| 400 | `VALIDATION_FAILED` | The body is invalid, for example an empty `seatIds` list |
| 400 | `NOTHING_TO_CHECK_OUT` | Checkout with no current holds |
| 404 | `SEAT_NOT_FOUND` | A requested seat id doesn't exist |
| 404 | `HOLD_NOT_FOUND` | Releasing a seat the customer doesn't hold |
| 409 | `SEAT_TAKEN` | At least one requested seat is held by someone else or sold |
| 409 | `HOLD_LIMIT_REACHED` | The hold would take the customer over 8 seats |
| 410 | `HOLD_EXPIRED` | Checkout after one of the customer's holds expired |

Error responses never include stack traces, SQL, or another customer's id.
