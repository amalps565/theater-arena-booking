import { useAuthStore } from '../store/authStore'
import type {
  AuthResponse,
  HoldResponse,
  MyHoldsResponse,
  OrderResponse,
  VenueResponse,
} from './types'

export class ApiError extends Error {
  readonly status: number
  readonly code: string

  constructor(status: number, code: string, message: string) {
    super(message)
    this.status = status
    this.code = code
  }
}

const SIGN_IN_PATHS = ['/api/auth/login', '/api/auth/register']

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = {}
  const token = useAuthStore.getState().session?.token
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
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
    if (response.status === 401 && !SIGN_IN_PATHS.includes(path)) {
      useAuthStore.getState().signOut()
    }
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
  login: (username: string, password: string) =>
    request<AuthResponse>('POST', '/api/auth/login', { username, password }),
  register: (username: string, displayName: string, password: string) =>
    request<AuthResponse>('POST', '/api/auth/register', { username, displayName, password }),
  venue: () => request<VenueResponse>('GET', '/api/venue'),
  myHolds: () => request<MyHoldsResponse>('GET', '/api/holds/me'),
  hold: (seatIds: number[]) => request<HoldResponse>('POST', '/api/holds', { seatIds }),
  release: (seatId: number) => request<void>('DELETE', `/api/holds/${seatId}`),
  checkout: () => request<OrderResponse>('POST', '/api/checkout'),
}
