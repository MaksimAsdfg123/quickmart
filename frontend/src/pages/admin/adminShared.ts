export type AdminActivityFilter = 'ACTIVE' | 'INACTIVE' | 'ALL'

export interface AdminFilterOption<T extends string> {
  value: T
  label: string
}

export const activityFilterOptions: Array<AdminFilterOption<AdminActivityFilter>> = [
  { value: 'ACTIVE', label: 'Активные' },
  { value: 'INACTIVE', label: 'Неактивные' },
  { value: 'ALL', label: 'Все' },
]

export function matchesActivityFilter(active: boolean, filter: AdminActivityFilter): boolean {
  if (filter === 'ALL') {
    return true
  }

  return filter === 'ACTIVE' ? active : !active
}

export function normalizeOptionalText(value?: string | null) {
  const trimmed = value?.trim()
  return trimmed ? trimmed : null
}
