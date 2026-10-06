import { useSeatStore, type Connection } from '../store/seatStore'

const BADGE: Record<Connection, { text: string; className: string }> = {
  connecting: { text: 'Connecting…', className: 'bg-slate-200 text-slate-700' },
  live: { text: 'Live', className: 'bg-emerald-100 text-emerald-800' },
  reconnecting: { text: 'Reconnecting…', className: 'bg-amber-100 text-amber-800' },
}

export function ConnectionBadge() {
  const connection = useSeatStore((s) => s.connection)
  const badge = BADGE[connection]
  return (
    <span
      role="status"
      className={`rounded-full px-3 py-1 text-xs font-medium whitespace-nowrap ${badge.className}`}
    >
      {badge.text}
      {connection === 'reconnecting' && <span className="hidden sm:inline"> map may be out of date</span>}
    </span>
  )
}
