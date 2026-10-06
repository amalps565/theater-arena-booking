import { create } from 'zustand'
import {
  seatFromWire,
  type HeldSeat,
  type Seat,
  type Section,
  type VenueMessage,
  type VenueResponse,
} from '../api/types'

export type Connection = 'connecting' | 'live' | 'reconnecting'

export interface MyHold {
  priceCents: number
  expiresAt: number
}

export interface SectionLabel {
  id: number
  x: number
  y: number
}

export interface Bounds {
  width: number
  height: number
}

interface SeatState {
  sections: Record<number, Section>
  seats: Record<number, Seat>
  seatIds: number[]
  bounds: Bounds
  labels: SectionLabel[]
  sectionSeq: Record<number, number>
  myHolds: Record<number, MyHold>
  connection: Connection
  loadSnapshot: (venue: VenueResponse, holds: HeldSeat[]) => void
  applyMessages: (messages: VenueMessage[]) => void
  addMyHolds: (holds: HeldSeat[]) => void
  removeMyHolds: (seatIds: number[]) => void
  setConnection: (connection: Connection) => void
  clearSession: () => void
}

const EMPTY_BOUNDS: Bounds = { width: 0, height: 0 }
const MAP_MARGIN = 24

function toMyHolds(holds: HeldSeat[]): Record<number, MyHold> {
  const mine: Record<number, MyHold> = {}
  for (const hold of holds) {
    mine[hold.seatId] = { priceCents: hold.priceCents, expiresAt: Date.parse(hold.expiresAt) }
  }
  return mine
}

function labelsOf(seats: Seat[]): SectionLabel[] {
  const labels = new Map<number, SectionLabel>()
  for (const seat of seats) {
    const label = labels.get(seat.sectionId)
    if (!label) {
      labels.set(seat.sectionId, { id: seat.sectionId, x: seat.x, y: seat.y })
    } else {
      label.x = Math.min(label.x, seat.x)
      label.y = Math.min(label.y, seat.y)
    }
  }
  return [...labels.values()]
}

function boundsOf(seats: Seat[]): Bounds {
  let width = 0
  let height = 0
  for (const seat of seats) {
    width = Math.max(width, seat.x)
    height = Math.max(height, seat.y)
  }
  return { width: width + MAP_MARGIN, height: height + MAP_MARGIN }
}

export const useSeatStore = create<SeatState>()((set) => ({
  sections: {},
  seats: {},
  seatIds: [],
  bounds: EMPTY_BOUNDS,
  labels: [],
  sectionSeq: {},
  myHolds: {},
  connection: 'connecting',

  loadSnapshot: (venue, holds) => {
    const seatList = venue.seats.map(seatFromWire)
    const seats: Record<number, Seat> = {}
    for (const seat of seatList) {
      seats[seat.id] = seat
    }
    const sections: Record<number, Section> = {}
    const sectionSeq: Record<number, number> = {}
    for (const section of venue.sections) {
      sections[section.id] = section
      sectionSeq[section.id] = section.priceSeq
    }
    set((state) => ({
      sections,
      seats,
      seatIds: state.seatIds.length === seatList.length ? state.seatIds : seatList.map((s) => s.id),
      bounds: state.bounds.width > 0 ? state.bounds : boundsOf(seatList),
      labels: state.labels.length > 0 ? state.labels : labelsOf(seatList),
      sectionSeq,
      myHolds: toMyHolds(holds),
    }))
  },

  applyMessages: (messages) =>
    set((state) => {
      let seats = state.seats
      let sectionSeq = state.sectionSeq
      let myHolds = state.myHolds
      for (const message of messages) {
        if (message.type === 'SEAT') {
          const current = seats[message.seatId]
          if (!current || message.seq <= current.seq) {
            continue
          }
          if (seats === state.seats) {
            seats = { ...seats }
          }
          seats[message.seatId] = { ...current, status: message.status, seq: message.seq }
          if (message.status !== 'HELD' && myHolds[message.seatId]) {
            myHolds = { ...myHolds }
            delete myHolds[message.seatId]
          }
        } else {
          if (message.seq <= (sectionSeq[message.sectionId] ?? 0)) {
            continue
          }
          sectionSeq = { ...sectionSeq, [message.sectionId]: message.seq }
          if (seats === state.seats) {
            seats = { ...seats }
          }
          for (const [seatId, priceCents] of message.prices) {
            const current = seats[seatId]
            if (current && current.priceCents !== priceCents) {
              seats[seatId] = { ...current, priceCents }
            }
          }
        }
      }
      if (seats === state.seats && sectionSeq === state.sectionSeq && myHolds === state.myHolds) {
        return state
      }
      return { seats, sectionSeq, myHolds }
    }),

  addMyHolds: (holds) => set((state) => ({ myHolds: { ...state.myHolds, ...toMyHolds(holds) } })),

  removeMyHolds: (seatIds) =>
    set((state) => {
      if (!seatIds.some((id) => state.myHolds[id])) {
        return state
      }
      const myHolds = { ...state.myHolds }
      for (const id of seatIds) {
        delete myHolds[id]
      }
      return { myHolds }
    }),

  setConnection: (connection) => set({ connection }),

  clearSession: () => set({ myHolds: {}, connection: 'connecting' }),
}))
