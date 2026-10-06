import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useAuthStore } from '../store/authStore'
import { LoginPage } from './LoginPage'

function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('LoginPage', () => {
  beforeEach(() => {
    useAuthStore.setState({ session: null })
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('signs the customer in with their username and password', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      jsonResponse(200, {
        token: 'token-value',
        expiresAt: '2099-01-01T00:00:00Z',
        user: { id: 'u-1', username: 'alice', displayName: 'Alice Anders' },
      }),
    )
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()
    render(<LoginPage />)

    await user.type(screen.getByLabelText('Username'), 'alice')
    await user.type(screen.getByLabelText('Password'), 'arena123')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(fetchMock).toHaveBeenCalledWith('/api/auth/login', expect.objectContaining({ method: 'POST' }))
    expect(useAuthStore.getState().session?.user.displayName).toBe('Alice Anders')
  })

  it('shows the server message and stays signed out when the password is wrong', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        jsonResponse(401, { code: 'INVALID_CREDENTIALS', message: 'Wrong username or password.' }),
      ),
    )
    const user = userEvent.setup()
    render(<LoginPage />)

    await user.type(screen.getByLabelText('Username'), 'alice')
    await user.type(screen.getByLabelText('Password'), 'nope')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Wrong username or password.')
    expect(useAuthStore.getState().session).toBeNull()
  })
})
