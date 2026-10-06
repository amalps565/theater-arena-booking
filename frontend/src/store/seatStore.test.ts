import { beforeEach, describe, expect, it } from 'vitest'
import type { VenueResponse } from '../api/types'
import { useSeatStore } from './seatStore'

const venue: VenueResponse = {
  sections: [{ id: 1, name: 'Center VIP', tier: 'VIP', basePriceCents: 15000, priceSeq: 2 }],
  seatFields: ['id', 'sectionId', 'x', 'y', 'row', 'number', 'status', 'priceCents', 'seq'],
  seats: [
    [10, 1, 6, 126, 'A', 1, 'AVAILABLE', 19500, 3],
    [11, 1, 18, 126, 'A', 2, 'AVAILABLE', 19500, 0],
  ],
}

function seat(id: number) {
  return useSeatStore.getState().seats[id]
}

describe('seat store', () => {
  beforeEach(() => {
    useSeatStore.setState({ seatIds: [], labels: [], bounds: { width: 0, height: 0 } })
    useSeatStore.getState().loadSnapshot(venue, [])
  })

  it('applies a seat update whose seq is newer', () => {
    useSeatStore.getState().applyMessages([{ type: 'SEAT', seatId: 10, status: 'HELD', seq: 4 }])

    expect(seat(10).status).toBe('HELD')
    expect(seat(10).seq).toBe(4)
  })

  it('ignores a stale or repeated seat update', () => {
    useSeatStore.getState().applyMessages([
      { type: 'SEAT', seatId: 10, status: 'HELD', seq: 3 },
      { type: 'SEAT', seatId: 10, status: 'SOLD', seq: 2 },
    ])

    expect(seat(10).status).toBe('AVAILABLE')
  })

  it('keeps the newest status when updates in one batch arrive out of order', () => {
    useSeatStore.getState().applyMessages([
      { type: 'SEAT', seatId: 11, status: 'AVAILABLE', seq: 2 },
      { type: 'SEAT', seatId: 11, status: 'HELD', seq: 1 },
    ])

    expect(seat(11).status).toBe('AVAILABLE')
    expect(seat(11).seq).toBe(2)
  })

  it('replaces only the seat that changed', () => {
    const untouched = seat(11)

    useSeatStore.getState().applyMessages([{ type: 'SEAT', seatId: 10, status: 'HELD', seq: 4 }])

    expect(seat(11)).toBe(untouched)
  })

  it('applies section prices only when their seq is newer', () => {
    useSeatStore.getState().applyMessages([
      { type: 'PRICES', sectionId: 1, seq: 2, prices: [[10, 1]] },
      { type: 'PRICES', sectionId: 1, seq: 3, prices: [[10, 22500]] },
    ])

    expect(seat(10).priceCents).toBe(22500)
    expect(seat(11).priceCents).toBe(19500)
  })

  it('drops my hold when the server says the seat is available again', () => {
    useSeatStore
      .getState()
      .addMyHolds([{ seatId: 10, priceCents: 19500, expiresAt: '2026-10-06T12:01:00Z' }])

    useSeatStore.getState().applyMessages([{ type: 'SEAT', seatId: 10, status: 'AVAILABLE', seq: 9 }])

    expect(useSeatStore.getState().myHolds[10]).toBeUndefined()
  })
})
