import type { PropsWithChildren } from 'react'

type BadgeTone = 'neutral' | 'success' | 'warning' | 'danger' | 'info'

interface BadgeProps {
  tone?: BadgeTone
}

export function Badge({ tone = 'neutral', children }: PropsWithChildren<BadgeProps>) {
  return <span className={['badge', `badge--${tone}`].join(' ')}>{children}</span>
}
