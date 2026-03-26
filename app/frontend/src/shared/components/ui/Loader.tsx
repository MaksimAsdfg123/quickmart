interface LoaderProps {
  label?: string
  compact?: boolean
}

export function Loader({ label = 'Загрузка...', compact = false }: LoaderProps) {
  return (
    <div className={['loader-wrap', compact ? 'loader-wrap--compact' : ''].filter(Boolean).join(' ')}>
      <span className="loader" aria-hidden="true" />
      <span>{label}</span>
    </div>
  )
}
