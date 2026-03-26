import { useEffect } from 'react'
import { createPortal } from 'react-dom'

import { Button, type ButtonVariant } from './Button'

interface ModalProps {
  open: boolean
  title: string
  description?: string
  confirmLabel?: string
  cancelLabel?: string
  confirmTone?: 'primary' | 'danger' | 'danger-muted'
  cancelTone?: ButtonVariant
  pending?: boolean
  onConfirm: () => void
  onClose: () => void
}

export function Modal({
  open,
  title,
  description,
  confirmLabel = 'Подтвердить',
  cancelLabel = 'Отмена',
  confirmTone = 'primary',
  cancelTone = 'secondary',
  pending = false,
  onConfirm,
  onClose,
}: ModalProps) {
  useEffect(() => {
    if (!open) {
      return
    }

    const handleEsc = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose()
      }
    }

    document.addEventListener('keydown', handleEsc)
    return () => document.removeEventListener('keydown', handleEsc)
  }, [open, onClose])

  if (!open) {
    return null
  }

  return createPortal(
    <div className="modal-overlay" role="dialog" aria-modal="true" onClick={onClose}>
      <div className="modal" onClick={(event) => event.stopPropagation()}>
        <h3>{title}</h3>
        {description ? <p className="muted">{description}</p> : null}
        <div className="modal__actions">
          <Button variant={cancelTone} onClick={onClose} disabled={pending}>
            {cancelLabel}
          </Button>
          <Button variant={confirmTone === 'primary' ? 'primary' : confirmTone} onClick={onConfirm} disabled={pending}>
            {confirmLabel}
          </Button>
        </div>
      </div>
    </div>,
    document.body,
  )
}
