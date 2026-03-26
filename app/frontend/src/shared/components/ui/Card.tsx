import type { HTMLAttributes, PropsWithChildren } from 'react'

interface CardProps extends HTMLAttributes<HTMLElement> {
  as?: 'section' | 'article' | 'div'
}

export function Card({ as = 'section', className, children, ...props }: PropsWithChildren<CardProps>) {
  const Tag = as
  return (
    <Tag className={['card', className ?? ''].filter(Boolean).join(' ')} {...props}>
      {children}
    </Tag>
  )
}
