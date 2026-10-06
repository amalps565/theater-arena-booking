const ITEMS = [
  { label: 'VIP', swatch: 'bg-tier-vip' },
  { label: 'Premium', swatch: 'bg-tier-premium' },
  { label: 'Standard', swatch: 'bg-tier-standard' },
  { label: 'Your hold', swatch: 'bg-seat-mine' },
  { label: 'On hold', swatch: 'bg-seat-held' },
  { label: 'Sold', swatch: 'bg-seat-sold' },
]

export function Legend() {
  return (
    <ul className="absolute bottom-3 left-3 flex flex-wrap gap-x-4 gap-y-1 rounded-lg bg-white/90 px-3 py-2 text-xs text-slate-600 shadow">
      {ITEMS.map((item) => (
        <li key={item.label} className="flex items-center gap-1.5">
          <span className={`inline-block size-3 rounded-full ${item.swatch}`} />
          {item.label}
        </li>
      ))}
    </ul>
  )
}
