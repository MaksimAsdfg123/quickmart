import type { PropsWithChildren, ReactNode } from 'react'

import { Drawer } from '../../shared/components/ui/Drawer'

interface AdminEntityDrawerProps {
  open: boolean
  title: string
  description?: string
  error?: string | null
  footer?: ReactNode
  onClose: () => void
}

export function AdminEntityDrawer({
  open,
  title,
  description,
  error,
  footer,
  onClose,
  children,
}: PropsWithChildren<AdminEntityDrawerProps>) {
  return (
    <Drawer open={open} title={title} description={description} footer={footer} onClose={onClose}>
      {error ? <div className="error">{error}</div> : null}
      {children}
    </Drawer>
  )
}
