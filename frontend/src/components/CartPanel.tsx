import { useEffect, useState } from 'react'
import { useNow } from '../hooks/useNow'
import { formatCountdown, formatPrice } from '../lib/format'
import { checkout, releaseSeat } from '../lib/seatActions'
import { useSeatStore } from '../store/seatStore'

const TICK_MS = 250
const WARNING_MS = 15_000

export function CartPanel() {
  const myHolds = useSeatStore((s) => s.myHolds)
  const seats = useSeatStore((s) => s.seats)
  const sections = useSeatStore((s) => s.sections)
  const removeMyHolds = useSeatStore((s) => s.removeMyHolds)
  const now = useNow(TICK_MS)
  const [busy, setBusy] = useState(false)

  const entries = Object.entries(myHolds)
    .map(([id, hold]) => ({ seatId: Number(id), ...hold }))
    .sort((a, b) => a.seatId - b.seatId)
  const total = entries.reduce((sum, entry) => sum + entry.priceCents, 0)

  useEffect(() => {
    const expired = Object.entries(myHolds)
      .filter(([, hold]) => hold.expiresAt <= now)
      .map(([id]) => Number(id))
    if (expired.length > 0) {
      removeMyHolds(expired)
    }
  }, [now, myHolds, removeMyHolds])

  const onCheckout = async () => {
    setBusy(true)
    await checkout()
    setBusy(false)
  }

  return (
    <aside className="flex w-full shrink-0 flex-col border-t border-slate-200 bg-white lg:w-80 lg:border-t-0 lg:border-l">
      <div className="border-b border-slate-200 px-4 py-3">
        <h2 className="text-lg font-semibold">Your seats</h2>
        <p className="text-sm text-slate-500">Each hold lasts 60 seconds.</p>
      </div>
      {entries.length === 0 ? (
        <p className="px-4 py-6 text-sm text-slate-500">Click a seat on the map to hold it.</p>
      ) : (
        <ul className="flex-1 divide-y divide-slate-100 overflow-y-auto">
          {entries.map((entry) => {
            const seat = seats[entry.seatId]
            const remaining = entry.expiresAt - now
            return (
              <li key={entry.seatId} className="flex items-center gap-3 px-4 py-3">
                <div className="min-w-0 flex-1">
                  <div className="truncate text-sm font-medium">
                    {seat ? sections[seat.sectionId]?.name : 'Seat'}
                  </div>
                  <div className="text-xs text-slate-500">
                    {seat ? `Row ${seat.row} · Seat ${seat.number}` : `#${entry.seatId}`} ·{' '}
                    {formatPrice(entry.priceCents)}
                  </div>
                </div>
                <span
                  className={`font-mono text-sm tabular-nums ${remaining <= WARNING_MS ? 'text-red-600' : 'text-slate-700'}`}
                  aria-label="Time left on hold"
                >
                  {formatCountdown(remaining)}
                </span>
                <button
                  type="button"
                  className="rounded px-2 py-1 text-xs text-slate-500 hover:bg-slate-100 hover:text-slate-900"
                  onClick={() => void releaseSeat(entry.seatId)}
                >
                  Release
                </button>
              </li>
            )
          })}
        </ul>
      )}
      <div className="mt-auto border-t border-slate-200 px-4 py-4">
        <div className="mb-3 flex items-baseline justify-between">
          <span className="text-sm text-slate-500">Total</span>
          <span className="text-xl font-semibold">{formatPrice(total)}</span>
        </div>
        <button
          type="button"
          disabled={entries.length === 0 || busy}
          onClick={() => void onCheckout()}
          className="w-full rounded-lg bg-indigo-600 px-4 py-2.5 font-semibold text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-slate-300"
        >
          {busy ? 'Checking out…' : 'Check out'}
        </button>
      </div>
    </aside>
  )
}
