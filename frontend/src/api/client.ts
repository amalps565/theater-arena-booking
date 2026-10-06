import type { HoldResponse, MyHoldsResponse, OrderResponse, VenueResponse } from './types'

const CUSTOMER_ID_KEY = 'arena.customerId'
const CUSTOMER_HEADER = 'X-Customer-Id'

export class ApiError extends Error {
  readonly status: number
  readonly code: string

  constructor(status: number, code: string, message: string) {
    super(message)
    this.status = status
    this.code = code
  }
}

let sessionCustomerId: string | null = null

function readStoredCustomerId(): string | null {
  try {
    return localStorage.getItem(CUSTOMER_ID_KEY)
  } catch {
    return null
  }
}

function storeCustomerId(id: string): boolean {
  try {
    localStorage.setItem(CUSTOMER_ID_KEY, id)
    return true
  } catch {
    return false
  }
}

export function customerId(): string {
  if (!sessionCustomerId) {
    sessionCustomerId = readStoredCustomerId() ?? crypto.randomUUID()
    storeCustomerId(sessionCustomerId)
  }
  return sessionCustomerId
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = { [CUSTOMER_HEADER]: customerId() }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  const response = await fetch(path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  if (!response.ok) {
    const error = await response.json().catch(() => null)
    throw new ApiError(
      response.status,
      error?.code ?? 'NETWORK_ERROR',
      error?.message ?? 'The server could not be reached. Please try again.',
    )
  }
  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

export const api = {
  venue: () => request<VenueResponse>('GET', '/api/venue'),
  myHolds: () => request<MyHoldsResponse>('GET', '/api/holds/me'),
  hold: (seatIds: number[]) => request<HoldResponse>('POST', '/api/holds', { seatIds }),
  release: (seatId: number) => request<void>('DELETE', `/api/holds/${seatId}`),
  checkout: () => request<OrderResponse>('POST', '/api/checkout'),
}
