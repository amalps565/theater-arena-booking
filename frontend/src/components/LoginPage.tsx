import { useState, type FormEvent } from 'react'
import { api, ApiError } from '../api/client'
import { useAuthStore } from '../store/authStore'

type Mode = 'signIn' | 'register'

const DEMO_ACCOUNTS = ['alice', 'bob', 'carol']
const DEMO_PASSWORD = 'arena123'

const INPUT_CLASS =
  'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200'

export function LoginPage() {
  const signIn = useAuthStore((s) => s.signIn)
  const [mode, setMode] = useState<Mode>('signIn')
  const [username, setUsername] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setBusy(true)
    setError(null)
    try {
      const response =
        mode === 'signIn'
          ? await api.login(username, password)
          : await api.register(username, displayName, password)
      signIn(response)
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : 'Something went wrong. Please try again.')
      setBusy(false)
    }
  }

  const switchMode = (next: Mode) => {
    setMode(next)
    setError(null)
  }

  return (
    <div className="flex min-h-full items-center justify-center bg-slate-100 px-4 py-10">
      <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-lg">
        <h1 className="text-2xl font-bold text-slate-900">Theater Arena Booking</h1>
        <p className="mt-1 text-sm text-slate-500">Sign in to hold seats and check out.</p>

        <div
          role="tablist"
          aria-label="Sign in or create an account"
          className="mt-5 grid grid-cols-2 rounded-lg bg-slate-100 p-1 text-sm font-medium"
        >
          <button
            type="button"
            role="tab"
            aria-selected={mode === 'signIn'}
            className={`rounded-md py-1.5 ${mode === 'signIn' ? 'bg-white shadow' : 'text-slate-500'}`}
            onClick={() => switchMode('signIn')}
          >
            Sign in
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={mode === 'register'}
            className={`rounded-md py-1.5 ${mode === 'register' ? 'bg-white shadow' : 'text-slate-500'}`}
            onClick={() => switchMode('register')}
          >
            Create account
          </button>
        </div>

        <form className="mt-5 space-y-4" onSubmit={(event) => void onSubmit(event)}>
          <label className="block text-sm font-medium text-slate-700">
            Username
            <input
              className={`mt-1 ${INPUT_CLASS}`}
              autoComplete="username"
              required
              value={username}
              onChange={(event) => setUsername(event.target.value)}
            />
          </label>
          {mode === 'register' && (
            <label className="block text-sm font-medium text-slate-700">
              Display name
              <input
                className={`mt-1 ${INPUT_CLASS}`}
                autoComplete="name"
                required
                value={displayName}
                onChange={(event) => setDisplayName(event.target.value)}
              />
            </label>
          )}
          <label className="block text-sm font-medium text-slate-700">
            Password
            <input
              className={`mt-1 ${INPUT_CLASS}`}
              type="password"
              autoComplete={mode === 'signIn' ? 'current-password' : 'new-password'}
              minLength={mode === 'register' ? 8 : undefined}
              required
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>
          {error && (
            <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
              {error}
            </p>
          )}
          <button
            type="submit"
            disabled={busy}
            className="w-full rounded-lg bg-indigo-600 px-4 py-2.5 font-semibold text-white hover:bg-indigo-700 disabled:bg-slate-300"
          >
            {busy ? 'Please wait…' : mode === 'signIn' ? 'Sign in' : 'Create account'}
          </button>
        </form>

        {mode === 'signIn' && (
          <p className="mt-5 rounded-lg bg-slate-50 px-3 py-2 text-xs text-slate-500">
            Demo accounts: {DEMO_ACCOUNTS.join(', ')}, all with the password{' '}
            <code className="font-mono text-slate-700">{DEMO_PASSWORD}</code>. Sign in as two
            different people in two windows to watch seats change live.
          </p>
        )}
      </div>
    </div>
  )
}
