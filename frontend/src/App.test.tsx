import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import App from './App'

describe('App', () => {
  it('shows the arena booking heading', () => {
    render(<App />)

    expect(screen.getByRole('heading', { name: 'Theater Arena Booking' })).toBeInTheDocument()
  })
})
