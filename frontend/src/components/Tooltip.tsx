import { useEffect, useImperativeHandle, useRef, useState, type Ref } from 'react'
import { formatPrice } from '../lib/format'
import { useSeatStore } from '../store/seatStore'

export interface TooltipHandle {
  show: (seatId: number, clientX: number, clientY: number) => void
  hide: () => void
}

const OFFSET_PX = 14

const STATUS_TEXT = {
  AVAILABLE: 'Available',
  HELD: 'On hold',
  SOLD: 'Sold',
} as const

export function Tooltip({ ref }: { ref: Ref<TooltipHandle> }) {
  const [seatId, setSeatId] = useState<number | null>(null)
  const boxRef = useRef<HTMLDivElement>(null)
  const frame = useRef(0)
  const position = useRef({ x: 0, y: 0 })

  useImperativeHandle(
    ref,
    () => ({
      show: (id, clientX, clientY) => {
        position.current = { x: clientX + OFFSET_PX, y: clientY + OFFSET_PX }
        setSeatId(id)
        if (!frame.current) {
          frame.current = requestAnimationFrame(() => {
            frame.current = 0
            if (boxRef.current) {
              boxRef.current.style.transform = `translate(${position.current.x}px, ${position.current.y}px)`
            }
          })
        }
      },
      hide: () => setSeatId(null),
    }),
    [],
  )

  useEffect(() => () => cancelAnimationFrame(frame.current), [])

  const seat = useSeatStore((s) => (seatId === null ? undefined : s.seats[seatId]))
  const section = useSeatStore((s) => (seat ? s.sections[seat.sectionId] : undefined))
  const mine = useSeatStore((s) => seatId !== null && s.myHolds[seatId] !== undefined)

  return (
    <div
      ref={boxRef}
      role="tooltip"
      className={`pointer-events-none fixed top-0 left-0 z-20 min-w-44 rounded-lg bg-slate-900/95 px-3 py-2 text-sm text-white shadow-lg ${seat ? '' : 'hidden'}`}
    >
      {seat && section && (
        <>
          <div className="font-semibold">{section.name}</div>
          <div className="text-slate-300">
            Row {seat.row} · Seat {seat.number}
          </div>
          <div className="mt-1 flex items-center justify-between gap-4">
            <span className="text-base font-semibold">{formatPrice(seat.priceCents)}</span>
            <span className={mine ? 'text-sky-300' : 'text-slate-300'}>
              {mine ? 'Held by you' : STATUS_TEXT[seat.status]}
            </span>
          </div>
        </>
      )}
    </div>
  )
}
