import { create } from 'zustand'

export type ToastTone = 'info' | 'success' | 'error'

export interface Toast {
  id: number
  tone: ToastTone
  text: string
}

interface ToastState {
  toasts: Toast[]
  show: (tone: ToastTone, text: string) => void
  dismiss: (id: number) => void
}

const TOAST_MS = 4000
let nextId = 1

export const useToastStore = create<ToastState>()((set, get) => ({
  toasts: [],
  show: (tone, text) => {
    const id = nextId++
    set((state) => ({ toasts: [...state.toasts.slice(-2), { id, tone, text }] }))
    setTimeout(() => get().dismiss(id), TOAST_MS)
  },
  dismiss: (id) => set((state) => ({ toasts: state.toasts.filter((t) => t.id !== id) })),
}))
