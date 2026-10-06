# Problem Statement

A ticketing company is launching an interactive seating chart for a high-profile concert arena. Customers expect to:

- open a visual map of the venue,
- hover over seats to see live prices,
- click to place a temporary **60-second hold** on their seats,
- check out.

The baseline skeleton is critically flawed in three ways.

## 1. Double-booking

When two customers click the same seat at the same moment, both requests read the seat as free, and both then mark it as theirs. Each customer is told the seat is held, and the arena sells it twice.

This is a classic **read-then-write race**. Checking the status in Java and saving it in a separate statement leaves a gap that another request can slip through. An in-memory lock such as `synchronized` doesn't fix it either, because it doesn't hold across server instances.

**Expected:** among concurrent requests for the same seat, exactly one succeeds. Every other request gets a clear conflict error (HTTP 409 `SEAT_TAKEN`). A request for several seats holds all of them or none. See [Seat Holds and Concurrency](Seat-Holds-and-Concurrency.md).

## 2. UI freezes on large sections

The map draws every seat as its own component with its own event handlers, and redraws the whole map when any seat changes. With thousands of seats, hovering and live updates lock up the page.

**Expected:**
- the whole arena stays responsive while hovering, panning, and zooming;
- a change to one seat redraws only that seat;
- hovering shows the price without re-rendering the map.

See [Frontend Design](Frontend-Design.md).

## 3. Seats locked forever

A customer who holds seats and walks away never releases them, so those seats can never be sold.

**Expected:**
- every hold ends 60 seconds after it was placed;
- an expired hold never blocks another customer, even before clean-up runs;
- open maps see the seat come back on sale within a few seconds;
- checkout only succeeds for seats the customer still holds, at the price they saw.

See [Hold Expiry and Checkout](Hold-Expiry-and-Checkout.md).

## Also required

- **Live prices:** every seat shows its current price. Prices depend on the section's tier, the row, and how full the section is. See [Dynamic Pricing](Dynamic-Pricing.md).
- **Live map:** seat and price changes reach every open map as they happen. See [Real-Time Updates](Real-Time-Updates.md).
