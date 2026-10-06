import { CartPanel } from './components/CartPanel'
import { ConnectionBadge } from './components/ConnectionBadge'
import { Legend } from './components/Legend'
import { SeatMap } from './components/SeatMap'
import { Toasts } from './components/Toasts'
import { useVenueSocket } from './hooks/useVenueSocket'
import { useSeatStore } from './store/seatStore'

export default function App() {
  useVenueSocket()
  const loaded = useSeatStore((s) => s.seatIds.length > 0)

  return (
    <div className="flex h-full flex-col bg-slate-100 text-slate-900">
      <header className="flex items-center justify-between gap-4 border-b border-slate-200 bg-white px-4 py-3">
        <div>
          <h1 className="text-xl font-bold">Theater Arena Booking</h1>
          <p className="text-sm text-slate-500">
            Hover a seat for its live price, click to hold it for 60 seconds, then check out.
          </p>
        </div>
        <ConnectionBadge />
      </header>
      <main className="flex min-h-0 flex-1 flex-col lg:flex-row">
        <section className="relative min-h-[60vh] flex-1 overflow-hidden lg:min-h-0">
          {loaded ? (
            <SeatMap />
          ) : (
            <div className="flex h-full items-center justify-center text-slate-500">
              Loading the arena…
            </div>
          )}
          <Legend />
        </section>
        <CartPanel />
      </main>
      <Toasts />
    </div>
  )
}
