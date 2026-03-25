import type { PropsWithChildren } from 'react'
import type { LinkProps } from 'react-router-dom'
import { Link } from 'react-router-dom'

import type { ButtonSize, ButtonVariant } from './Button'
import { getButtonClassName } from './Button'

interface ButtonLinkProps extends LinkProps {
  variant?: ButtonVariant
  size?: ButtonSize
  block?: boolean
}

export function ButtonLink({
  children,
  variant = 'primary',
  size = 'md',
  block = false,
  className,
  ...props
}: PropsWithChildren<ButtonLinkProps>) {
  return (
    <Link className={getButtonClassName({ variant, size, block, className })} {...props}>
      {children}
    </Link>
  )
}
