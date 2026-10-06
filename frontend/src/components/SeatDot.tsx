import { memo } from 'react'
import type { SeatStatus, Tier } from '../api/types'
import { useSeatStore } from '../store/seatStore'

const SEAT_RADIUS = 4.5

const AVAILABLE_CLASS: Record<Tier, string> = {
  VIP: 'fill-tier-vip cursor-pointer hover:stroke-slate-900 hover:stroke-2',
  PREMIUM: 'fill-tier-premium cursor-pointer hover:stroke-slate-900 hover:stroke-2',
  STANDARD: 'fill-tier-standard cursor-pointer hover:stroke-slate-900 hover:stroke-2',
}

const MINE_CLASS = 'fill-seat-mine cursor-pointer stroke-white stroke-1 hover:stroke-slate-900'

const TAKEN_CLASS: Record<Exclude<SeatStatus, 'AVAILABLE'>, string> = {
  HELD: 'fill-seat-held',
  SOLD: 'fill-seat-sold',
}

function seatClass(status: SeatStatus, tier: Tier, mine: boolean): string {
  if (mine) {
    return MINE_CLASS
  }
  return status === 'AVAILABLE' ? AVAILABLE_CLASS[tier] : TAKEN_CLASS[status]
}

export const SeatDot = memo(function SeatDot({ id }: { id: number }) {
  const seat = useSeatStore((s) => s.seats[id])
  const tier = useSeatStore((s) => s.sections[s.seats[id].sectionId].tier)
  const mine = useSeatStore((s) => s.myHolds[id] !== undefined)
  return (
    <circle
      data-seat-id={id}
      cx={seat.x}
      cy={seat.y}
      r={SEAT_RADIUS}
      className={seatClass(seat.status, tier, mine)}
    />
  )
})
