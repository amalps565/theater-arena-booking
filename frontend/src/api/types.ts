export type Tier = 'VIP' | 'PREMIUM' | 'STANDARD'

export type SeatStatus = 'AVAILABLE' | 'HELD' | 'SOLD'

export interface Section {
  id: number
  name: string
  tier: Tier
  basePriceCents: number
  priceSeq: number
}

export interface Seat {
  id: number
  sectionId: number
  x: number
  y: number
  row: string
  number: number
  status: SeatStatus
  priceCents: number
  seq: number
}

export type SeatWire = [number, number, number, number, string, number, SeatStatus, number, number]

export interface VenueResponse {
  sections: Section[]
  seatFields: string[]
  seats: SeatWire[]
}

export interface HeldSeat {
  seatId: number
  priceCents: number
  expiresAt: string
}

export interface HoldResponse {
  expiresAt: string
  holds: HeldSeat[]
}

export interface MyHoldsResponse {
  holds: HeldSeat[]
}

export interface OrderResponse {
  orderId: number
  totalCents: number
  items: { seatId: number; priceCents: number }[]
}

export interface SeatMessage {
  type: 'SEAT'
  seatId: number
  status: SeatStatus
  seq: number
}

export interface PricesMessage {
  type: 'PRICES'
  sectionId: number
  seq: number
  prices: [number, number][]
}

export type VenueMessage = SeatMessage | PricesMessage

export function seatFromWire([id, sectionId, x, y, row, number, status, priceCents, seq]: SeatWire): Seat {
  return { id, sectionId, x, y, row, number, status, priceCents, seq }
}
