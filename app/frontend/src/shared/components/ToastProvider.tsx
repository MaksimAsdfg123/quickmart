import { createContext, type ReactNode, useCallback, useContext, useMemo, useState } from 'react'

type ToastType = 'success' | 'error' | 'info'

interface ToastPayload {
  title: string
  description?: string
  type?: ToastType
}

interface ToastItem extends ToastPayload {
  id: number
  type: ToastType
}

interface ToastContextValue {
  showToast: (payload: ToastPayload) => void
}

const ToastContext = createContext<ToastContextValue | null>(null)

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([])

  const removeToast = useCallback((id: number) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id))
  }, [])

  const showToast = useCallback(
    ({ title, description, type = 'info' }: ToastPayload) => {
      const id = Date.now() + Math.floor(Math.random() * 1000)
      setToasts((prev) => [...prev, { id, title, description, type }])

      window.setTimeout(() => {
        removeToast(id)
      }, 3600)
    },
    [removeToast],
  )

  const value = useMemo(() => ({ showToast }), [showToast])

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-stack" aria-live="polite" aria-atomic="true">
        {toasts.map((toast) => (
          <div key={toast.id} className={['toast', `toast--${toast.type}`].join(' ')}>
            <div className="toast__content">
              <strong>{toast.title}</strong>
              {toast.description ? <span>{toast.description}</span> : null}
            </div>
            <button className="toast__close" onClick={() => removeToast(toast.id)} aria-label="Закрыть уведомление">
              ×
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast() {
  const context = useContext(ToastContext)
  if (!context) {
    throw new Error('useToast must be used within ToastProvider')
  }

  return context
}
