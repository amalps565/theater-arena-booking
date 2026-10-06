const currency = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' })

// probe comment
export function formatPrice(cents: number): string {
  return currency.format(cents / 100)
}

export function formatCountdown(ms: number): string {
  const totalSeconds = Math.max(0, Math.ceil(ms / 1000))
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes}:${String(seconds).padStart(2, '0')}`
}
