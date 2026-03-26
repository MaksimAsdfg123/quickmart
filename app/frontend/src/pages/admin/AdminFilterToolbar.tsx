import { Card } from '../../shared/components/ui/Card'
import { Input } from '../../shared/components/ui/Input'
import type { AdminFilterOption } from './adminShared'

interface AdminFilterToolbarProps<T extends string> {
  search: string
  searchPlaceholder: string
  onSearchChange: (value: string) => void
  filters?: Array<AdminFilterOption<T>>
  activeFilter?: T
  onFilterChange?: (value: T) => void
  filtersAriaLabel?: string
}

export function AdminFilterToolbar<T extends string>({
  search,
  searchPlaceholder,
  onSearchChange,
  filters,
  activeFilter,
  onFilterChange,
  filtersAriaLabel,
}: AdminFilterToolbarProps<T>) {
  return (
    <Card>
      <div className="admin-toolbar">
        <Input
          placeholder={searchPlaceholder}
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
        />
        {filters && activeFilter && onFilterChange && filtersAriaLabel ? (
          <div className="admin-filters" aria-label={filtersAriaLabel}>
            {filters.map((filter) => (
              <button
                key={filter.value}
                type="button"
                className={['chip', activeFilter === filter.value ? 'active' : ''].join(' ').trim()}
                onClick={() => onFilterChange(filter.value)}
              >
                {filter.label}
              </button>
            ))}
          </div>
        ) : null}
      </div>
    </Card>
  )
}
