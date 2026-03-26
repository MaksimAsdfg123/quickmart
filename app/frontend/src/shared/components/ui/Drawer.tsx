import { type PropsWithChildren, type ReactNode, useEffect } from 'react'
import { createPortal } from 'react-dom'

import { Button } from './Button'

interface DrawerProps {
  open: boolean
  title: string
  description?: string
  footer?: ReactNode
  onClose: () => void
}

export function Drawer({ open, title, description, footer, onClose, children }: PropsWithChildren<DrawerProps>) {
  useEffect(() => {
    if (!open) {
      return
    }

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    const handleEsc = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose()
      }
    }

    document.addEventListener('keydown', handleEsc)
    return () => {
      document.body.style.overflow = previousOverflow
      document.removeEventListener('keydown', handleEsc)
    }
  }, [open, onClose])

  if (!open) {
    return null
  }

  return createPortal(
    <div className="drawer-overlay" role="dialog" aria-modal="true" onClick={onClose}>
      <aside className="drawer" onClick={(event) => event.stopPropagation()}>
        <header className="drawer__header">
          <div className="drawer__headings">
            <h3>{title}</h3>
            {description ? <p className="muted">{description}</p> : null}
          </div>
          <Button variant="ghost" size="sm" onClick={onClose} aria-label="Закрыть панель">
            Закрыть
          </Button>
        </header>
        <div className="drawer__body">{children}</div>
        {footer ? <div className="drawer__footer">{footer}</div> : null}
      </aside>
    </div>,
    document.body,
  )
}
