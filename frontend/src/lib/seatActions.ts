import { api, ApiError } from '../api/client'
import { useSeatStore } from '../store/seatStore'
import { useToastStore } from '../store/toastStore'
import { formatPrice } from './format'

function showError(error: unknown): void {
  const text = error instanceof ApiError ? error.message : 'Something went wrong. Please try again.'
  useToastStore.getState().show('error', text)
}

async function reloadMyHolds(): Promise<void> {
  const { holds } = await api.myHolds()
  const store = useSeatStore.getState()
  store.removeMyHolds(Object.keys(store.myHolds).map(Number))
  store.addMyHolds(holds)
}

export async function toggleSeat(seatId: number): Promise<void> {
  const { seats, myHolds } = useSeatStore.getState()
  const seat = seats[seatId]
  if (!seat) {
    return
  }
  if (myHolds[seatId]) {
    await releaseSeat(seatId)
    return
  }
  if (seat.status !== 'AVAILABLE') {
    useToastStore
      .getState()
      .show('info', `Seat ${seat.row}${seat.number} is ${seat.status === 'SOLD' ? 'sold' : 'on hold'}.`)
    return
  }
  try {
    const response = await api.hold([seatId])
    useSeatStore.getState().addMyHolds(response.holds)
  } catch (error) {
    showError(error)
  }
}

export async function releaseSeat(seatId: number): Promise<void> {
  try {
    await api.release(seatId)
  } catch (error) {
    if (!(error instanceof ApiError && error.code === 'HOLD_NOT_FOUND')) {
      showError(error)
      return
    }
  }
  useSeatStore.getState().removeMyHolds([seatId])
}

export async function checkout(): Promise<void> {
  try {
    const order = await api.checkout()
    useSeatStore.getState().removeMyHolds(order.items.map((item) => item.seatId))
    useToastStore
      .getState()
      .show(
        'success',
        `Order #${order.orderId} confirmed: ${order.items.length} seat(s), ${formatPrice(order.totalCents)}.`,
      )
  } catch (error) {
    showError(error)
    if (error instanceof ApiError && (error.status === 410 || error.status === 400)) {
      await reloadMyHolds().catch(showError)
    }
  }
}
