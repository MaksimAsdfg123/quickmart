import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState } from 'react'

import { adminApi } from '../../api/adminApi'
import { useToast } from '../../shared/components/ToastProvider'
import { Badge } from '../../shared/components/ui/Badge'
import { Button } from '../../shared/components/ui/Button'
import { Card } from '../../shared/components/ui/Card'
import { Input } from '../../shared/components/ui/Input'
import { extractErrorMessage } from '../../shared/lib/errors'
import { formatMoney } from '../../shared/lib/format'
import type { InventoryStock } from '../../shared/types'
import { AdminFilterToolbar } from './AdminFilterToolbar'
import { AdminPageLayout } from './AdminPageLayout'
import { AdminPageState } from './AdminPageState'
import type { AdminFilterOption } from './adminShared'

type InventoryFilter = 'ACTIVE' | 'ALL'

const inventoryFilters: Array<AdminFilterOption<InventoryFilter>> = [
  { value: 'ACTIVE', label: 'Активные товары' },
  { value: 'ALL', label: 'Все' },
]

function getInventoryTone(quantity: number): 'success' | 'warning' | 'danger' {
  if (quantity === 0) {
    return 'danger'
  }

  if (quantity <= 10) {
    return 'warning'
  }

  return 'success'
}

function getInventoryLabel(quantity: number): string {
  if (quantity === 0) {
    return 'Нет в наличии'
  }

  if (quantity <= 10) {
    return 'Заканчивается'
  }

  return 'В норме'
}

export function AdminInventoryPage() {
  const queryClient = useQueryClient()
  const { showToast } = useToast()
  const [values, setValues] = useState<Record<string, number>>({})
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<InventoryFilter>('ACTIVE')
  const [pendingRowId, setPendingRowId] = useState<string | null>(null)

  const inventoryQuery = useQuery({
    queryKey: ['admin-inventory'],
    queryFn: adminApi.listInventory,
  })

  const updateMutation = useMutation({
    mutationFn: ({ productId, qty }: { productId: string; qty: number }) => adminApi.updateInventory(productId, qty),
    onMutate: ({ productId }) => {
      setPendingRowId(productId)
    },
    onSuccess: (updatedRow) => {
      queryClient.setQueryData<InventoryStock[] | undefined>(
        ['admin-inventory'],
        (current) => current?.map((row) => (row.productId === updatedRow.productId ? updatedRow : row)) ?? current,
      )
      setValues((prev) => {
        const next = { ...prev }
        delete next[updatedRow.productId]
        return next
      })
      showToast({
        type: 'success',
        title: 'Остаток обновлен',
        description: updatedRow.productName,
      })
    },
    onError: (error) => {
      showToast({
        type: 'error',
        title: 'Не удалось обновить остаток',
        description: extractErrorMessage(error),
      })
    },
    onSettled: () => {
      setPendingRowId(null)
    },
  })

  const filteredRows = useMemo(() => {
    const query = search.trim().toLowerCase()

    return (inventoryQuery.data ?? []).filter((row) => {
      if (statusFilter === 'ACTIVE' && !row.productActive) {
        return false
      }

      if (!query) {
        return true
      }

      return row.productName.toLowerCase().includes(query) || row.categoryName.toLowerCase().includes(query)
    })
  }, [inventoryQuery.data, search, statusFilter])

  const updateDraft = (productId: string, value: number) => {
    setValues((prev) => ({ ...prev, [productId]: value }))
  }

  const resetDraft = (productId: string) => {
    setValues((prev) => {
      const next = { ...prev }
      delete next[productId]
      return next
    })
  }

  return (
    <AdminPageLayout
      title="Остатки"
      subtitle="Операционное управление доступным количеством товаров без ухода со списка."
    >
      <AdminFilterToolbar
        search={search}
        searchPlaceholder="Поиск по товару или категории"
        onSearchChange={setSearch}
        filters={inventoryFilters}
        activeFilter={statusFilter}
        onFilterChange={setStatusFilter}
        filtersAriaLabel="Фильтр по статусу товаров"
      />

      <AdminPageState
        isLoading={inventoryQuery.isLoading}
        isError={inventoryQuery.isError || !inventoryQuery.data}
        error={inventoryQuery.error}
        isEmpty={filteredRows.length === 0}
        loadingLabel="Загружаем остатки"
        emptyTitle="Ничего не найдено"
        emptyDescription="Измените поисковый запрос или переключите фильтр отображения."
      >
        <Card>
          <table className="table">
            <thead>
              <tr>
                <th>Товар</th>
                <th>Цена</th>
                <th>Остаток</th>
                <th>Изменить</th>
              </tr>
            </thead>
            <tbody>
              {filteredRows.map((row) => {
                const draftValue = values[row.productId] ?? row.availableQuantity
                const normalizedValue = Math.max(0, Math.floor(draftValue))
                const isDirty = normalizedValue !== row.availableQuantity
                const isPending = pendingRowId === row.productId
                const isValid = Number.isFinite(draftValue) && draftValue >= 0

                return (
                  <tr key={row.id}>
                    <td>
                      <div className="admin-row-main">
                        <strong>{row.productName}</strong>
                        <div className="admin-row-support">
                          <span className="muted muted--compact">{row.categoryName}</span>
                          {!row.productActive ? <Badge tone="neutral">Товар неактивен</Badge> : null}
                        </div>
                      </div>
                    </td>
                    <td>{formatMoney(row.price)}</td>
                    <td>
                      <div className="inventory-cell">
                        <strong>{row.availableQuantity} шт.</strong>
                        <Badge tone={getInventoryTone(row.availableQuantity)}>
                          {getInventoryLabel(row.availableQuantity)}
                        </Badge>
                      </div>
                    </td>
                    <td>
                      <div className="inventory-actions">
                        <Input
                          className={['admin-qty-input', isDirty ? 'inventory-input--dirty' : ''].join(' ').trim()}
                          type="number"
                          min={0}
                          value={draftValue}
                          onChange={(event) => {
                            const value = Number(event.target.value)
                            updateDraft(row.productId, Number.isFinite(value) ? value : 0)
                          }}
                        />
                        <div className="admin-inline-actions">
                          <Button
                            variant="ghost"
                            size="sm"
                            type="button"
                            disabled={isPending}
                            onClick={() => updateDraft(row.productId, Math.max(0, normalizedValue - 10))}
                          >
                            -10
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            type="button"
                            disabled={isPending}
                            onClick={() => updateDraft(row.productId, normalizedValue + 10)}
                          >
                            +10
                          </Button>
                          <Button
                            variant="neutral"
                            size="sm"
                            type="button"
                            disabled={isPending || !isDirty}
                            onClick={() => resetDraft(row.productId)}
                          >
                            Сбросить
                          </Button>
                          <Button
                            size="sm"
                            type="button"
                            disabled={isPending || !isDirty || !isValid}
                            onClick={() => updateMutation.mutate({ productId: row.productId, qty: normalizedValue })}
                          >
                            {isPending ? 'Сохраняем...' : 'Сохранить'}
                          </Button>
                        </div>
                        {isDirty ? <div className="muted muted--compact">Есть несохраненные изменения</div> : null}
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </Card>
      </AdminPageState>
    </AdminPageLayout>
  )
}
