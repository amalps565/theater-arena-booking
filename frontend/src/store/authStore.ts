import { create } from 'zustand'
import type { AuthResponse, User } from '../api/types'

const SESSION_KEY = 'arena.session'

export interface Session {
  token: string
  expiresAt: number
  user: User
}

interface AuthState {
  session: Session | null
  signIn: (response: AuthResponse) => void
  signOut: () => void
}

function readStoredSession(): Session | null {
  try {
    const raw = localStorage.getItem(SESSION_KEY)
    if (!raw) {
      return null
    }
    const session = JSON.parse(raw) as Session
    return session.expiresAt > Date.now() ? session : null
  } catch {
    return null
  }
}

function writeStoredSession(session: Session | null): boolean {
  try {
    if (session) {
      localStorage.setItem(SESSION_KEY, JSON.stringify(session))
    } else {
      localStorage.removeItem(SESSION_KEY)
    }
    return true
  } catch {
    return false
  }
}

export const useAuthStore = create<AuthState>()((set) => ({
  session: readStoredSession(),
  signIn: (response) => {
    const session: Session = {
      token: response.token,
      expiresAt: Date.parse(response.expiresAt),
      user: response.user,
    }
    writeStoredSession(session)
    set({ session })
  },
  signOut: () => {
    writeStoredSession(null)
    set({ session: null })
  },
}))
