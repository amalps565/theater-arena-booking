# Dynamic Pricing

Every seat has a live price. `PricingService` is the **only** place a price is computed; everything else asks it. Built in issue [#2](https://github.com/amalps565/theater-arena-booking/issues/2).

## Formula

```
price = basePrice(tier) × rowMultiplier × demandMultiplier
```

rounded to the nearest **50 cents**, and stored in cents.

| Factor | Rule |
|---|---|
| **Base price** | Set per section by its tier: `VIP` > `PREMIUM` > `STANDARD` |
| **Row multiplier** | Front row is **1.3×**, falling linearly to **1.0×** at the back row of the section |
| **Demand multiplier** | From the section's fill level, where held and sold seats both count: **1.0×** up to 50%, **1.15×** above 50%, **1.35×** above 80% |

## Example

A `PREMIUM` section with a base price of $80.00 and 40 rows:

| Seat | Row multiplier | Section fill | Demand | Price |
|---|---|---|---|---|
| Row 1 | 1.30 | 20% | 1.00 | $104.00 |
| Row 40 | 1.00 | 20% | 1.00 | $80.00 |
| Row 1 | 1.30 | 60% | 1.15 | $119.50 |
| Row 1 | 1.30 | 85% | 1.35 | $140.50 |

## When prices are computed

- **Venue snapshot:** every seat's current price is in `GET /api/venue`.
- **Hold:** the price at that moment is frozen into `held_price_cents`. Checkout always charges that price, even if demand has moved since.
- **After a change:** when a hold, release, expiry, or sale changes a section's fill level across a threshold, the server publishes the section's new prices. See [Real-Time Updates](Real-Time-Updates.md).

## Rules

- Prices are `long` cents, never floating point. The multipliers are applied with integer arithmetic, then rounded.
- No other class computes or adjusts a price.
- Pricing has unit tests for each tier, the row ends, each demand threshold, and the rounding.
