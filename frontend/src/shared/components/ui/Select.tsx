import { forwardRef, type SelectHTMLAttributes } from 'react'

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  hasError?: boolean
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(function Select(
  { hasError = false, className, children, ...props },
  ref,
) {
  return (
    <select
      ref={ref}
      className={['select', hasError ? 'input--error' : '', className ?? ''].filter(Boolean).join(' ')}
      {...props}
    >
      {children}
    </select>
  )
})
