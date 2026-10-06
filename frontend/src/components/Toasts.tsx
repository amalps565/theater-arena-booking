import { useToastStore, type ToastTone } from '../store/toastStore'

const TONE_CLASS: Record<ToastTone, string> = {
  info: 'bg-slate-800',
  success: 'bg-emerald-600',
  error: 'bg-red-600',
}

export function Toasts() {
  const toasts = useToastStore((s) => s.toasts)
  const dismiss = useToastStore((s) => s.dismiss)
  return (
    <div className="pointer-events-none fixed top-3 left-1/2 z-30 flex w-full max-w-md -translate-x-1/2 flex-col gap-2 px-4 sm:top-auto sm:bottom-4">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          role="status"
          className={`pointer-events-auto flex items-start gap-3 rounded-lg px-4 py-3 text-sm text-white shadow-lg ${TONE_CLASS[toast.tone]}`}
        >
          <span className="flex-1">{toast.text}</span>
          <button
            type="button"
            aria-label="Dismiss"
            className="opacity-70 hover:opacity-100"
            onClick={() => dismiss(toast.id)}
          >
            ×
          </button>
        </div>
      ))}
    </div>
  )
}
