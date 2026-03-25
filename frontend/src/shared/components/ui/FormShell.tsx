import type { HTMLAttributes, PropsWithChildren, ReactNode } from 'react'

import { Card } from './Card'

type FormShellWidth = 'sm' | 'md' | 'lg' | 'fluid'
type FormShellVariant = 'page' | 'section'

interface FormShellProps extends HTMLAttributes<HTMLElement> {
  title: string
  subtitle?: string
  footer?: ReactNode
  width?: FormShellWidth
  variant?: FormShellVariant
}

export function FormShell({
  title,
  subtitle,
  footer,
  width = 'md',
  variant = 'section',
  className,
  children,
  ...props
}: PropsWithChildren<FormShellProps>) {
  const TitleTag = variant === 'page' ? 'h1' : 'h3'

  return (
    <Card className={['form-shell', `form-shell--${width}`, className ?? ''].filter(Boolean).join(' ')} {...props}>
      <div className="form-shell__intro">
        <TitleTag className={variant === 'page' ? 'page-title' : 'form-shell__title'}>{title}</TitleTag>
        {subtitle ? <p className={variant === 'page' ? 'page-subtitle' : 'muted'}>{subtitle}</p> : null}
      </div>
      <div className="form-shell__content">{children}</div>
      {footer ? <div className="form-shell__footer">{footer}</div> : null}
    </Card>
  )
}
