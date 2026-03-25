import { forwardRef, type InputHTMLAttributes } from 'react'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  hasError?: boolean
}

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input(
  { hasError = false, className, ...props },
  ref,
) {
  return (
    <input
      ref={ref}
      className={['input', hasError ? 'input--error' : '', className ?? ''].filter(Boolean).join(' ')}
      {...props}
    />
  )
})
