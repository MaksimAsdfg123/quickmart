import type { ButtonHTMLAttributes, PropsWithChildren } from 'react'

export type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'ghost' | 'neutral' | 'danger-muted'
export type ButtonSize = 'sm' | 'md' | 'lg'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant
  size?: ButtonSize
  block?: boolean
}

interface ButtonClassOptions {
  variant?: ButtonVariant
  size?: ButtonSize
  block?: boolean
  className?: string
}

export function getButtonClassName({ variant = 'primary', size = 'md', block = false, className }: ButtonClassOptions) {
  return ['btn', `btn--${variant}`, `btn--${size}`, block ? 'btn--block' : '', className ?? '']
    .filter(Boolean)
    .join(' ')
}

export function Button({
  children,
  variant = 'primary',
  size = 'md',
  block = false,
  className,
  type = 'button',
  ...props
}: PropsWithChildren<ButtonProps>) {
  const classes = getButtonClassName({ variant, size, block, className })

  return (
    <button className={classes} type={type} {...props}>
      {children}
    </button>
  )
}
