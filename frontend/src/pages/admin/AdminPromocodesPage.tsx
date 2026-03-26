import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'

import { adminApi, type PromoPayload } from '../../api/adminApi'
import { useToast } from '../../shared/components/ToastProvider'
import { Badge } from '../../shared/components/ui/Badge'
import { Button } from '../../shared/components/ui/Button'
import { Card } from '../../shared/components/ui/Card'
import { Input } from '../../shared/components/ui/Input'
import { Modal } from '../../shared/components/ui/Modal'
import { Select } from '../../shared/components/ui/Select'
import { extractErrorMessage } from '../../shared/lib/errors'
import { formatDate, formatMoney } from '../../shared/lib/format'
import type { PromoCode } from '../../shared/types'
import { AdminEntityDrawer } from './AdminEntityDrawer'
import { AdminFilterToolbar } from './AdminFilterToolbar'
import { AdminPageLayout } from './AdminPageLayout'
import { AdminPageState } from './AdminPageState'
import { activityFilterOptions, type AdminActivityFilter, normalizeOptionalText } from './adminShared'

type DrawerMode = 'create' | 'edit' | null
type PromoLifecycle = 'ACTIVE' | 'SCHEDULED' | 'EXPIRED' | 'EXHAUSTED' | 'DISABLED'

const emptyPromo: PromoPayload = {
  code: '',
  type: 'FIXED',
  value: 100,
  minOrderAmount: 0,
  active: true,
  validFrom: null,
  validTo: null,
  usageLimit: null,
}

function toPromoPayload(promo: PromoCode): PromoPayload {
  return {
    code: promo.code,
    type: promo.type,
    value: Number(promo.value),
    minOrderAmount: Number(promo.minOrderAmount),
    active: promo.active,
    validFrom: promo.validFrom ? promo.validFrom.slice(0, 16) : null,
    validTo: promo.validTo ? promo.validTo.slice(0, 16) : null,
    usageLimit: promo.usageLimit ?? null,
  }
}

function getPromoLifecycle(promo: PromoCode): PromoLifecycle {
  const now = Date.now()
  const validFrom = promo.validFrom ? new Date(promo.validFrom).getTime() : null
  const validTo = promo.validTo ? new Date(promo.validTo).getTime() : null

  if (!promo.active) {
    return 'DISABLED'
  }

  if (promo.usageLimit !== null && promo.usedCount >= promo.usageLimit) {
    return 'EXHAUSTED'
  }

  if (validFrom !== null && validFrom > now) {
    return 'SCHEDULED'
  }

  if (validTo !== null && validTo < now) {
    return 'EXPIRED'
  }

  return 'ACTIVE'
}

function promoLifecycleLabel(state: PromoLifecycle): string {
  const labels: Record<PromoLifecycle, string> = {
    ACTIVE: 'Активен',
    SCHEDULED: 'Запланирован',
    EXPIRED: 'Истек',
    EXHAUSTED: 'Исчерпан',
    DISABLED: 'Отключен',
  }

  return labels[state]
}

function promoLifecycleTone(state: PromoLifecycle): 'success' | 'info' | 'danger' | 'neutral' {
  const tones: Record<PromoLifecycle, 'success' | 'info' | 'danger' | 'neutral'> = {
    ACTIVE: 'success',
    SCHEDULED: 'info',
    EXPIRED: 'danger',
    EXHAUSTED: 'danger',
    DISABLED: 'neutral',
  }

  return tones[state]
}

export function AdminPromocodesPage() {
  const queryClient = useQueryClient()
  const { showToast } = useToast()
  const [drawerMode, setDrawerMode] = useState<DrawerMode>(null)
  const [selectedPromo, setSelectedPromo] = useState<PromoCode | null>(null)
  const [statusFilter, setStatusFilter] = useState<AdminActivityFilter>('ACTIVE')
  const [search, setSearch] = useState('')
  const [pendingRowId, setPendingRowId] = useState<string | null>(null)
  const [deactivationTarget, setDeactivationTarget] = useState<PromoCode | null>(null)
  const [discardModalOpen, setDiscardModalOpen] = useState(false)

  const promosQuery = useQuery({
    queryKey: ['admin-promos'],
    queryFn: adminApi.listPromos,
  })

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isDirty },
  } = useForm<PromoPayload>({
    defaultValues: emptyPromo,
  })

  const promoType = watch('type')
  const validFrom = watch('validFrom')

  const syncPromoInCache = (nextPromo: PromoCode) => {
    queryClient.setQueryData<PromoCode[] | undefined>(['admin-promos'], (current) => {
      if (!current) {
        return current
      }

      const exists = current.some((item) => item.id === nextPromo.id)
      const next = exists
        ? current.map((item) => (item.id === nextPromo.id ? nextPromo : item))
        : [nextPromo, ...current]

      return [...next].sort((left, right) => left.code.localeCompare(right.code, 'ru'))
    })
  }

  const closeDrawerImmediately = () => {
    setDrawerMode(null)
    setSelectedPromo(null)
    setDiscardModalOpen(false)
    reset(emptyPromo)
  }

  const saveMutation = useMutation({
    mutationFn: (payload: PromoPayload) => {
      if (drawerMode === 'edit' && selectedPromo) {
        return adminApi.updatePromo(selectedPromo.id, payload)
      }

      return adminApi.createPromo(payload)
    },
    onSuccess: (promo) => {
      syncPromoInCache(promo)
      queryClient.invalidateQueries({ queryKey: ['admin-promos'] })
      showToast({
        type: 'success',
        title: drawerMode === 'edit' ? 'Промокод обновлен' : 'Промокод создан',
        description: promo.code,
      })
      closeDrawerImmediately()
    },
    onError: (error) => {
      showToast({
        type: 'error',
        title: 'Не удалось сохранить промокод',
        description: extractErrorMessage(error),
      })
    },
  })

  const toggleMutation = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) => adminApi.togglePromo(id, active),
    onMutate: ({ id }) => {
      setPendingRowId(id)
    },
    onSuccess: (promo) => {
      syncPromoInCache(promo)
      queryClient.invalidateQueries({ queryKey: ['admin-promos'] })
      showToast({
        type: 'success',
        title: promo.active ? 'Промокод активирован' : 'Промокод деактивирован',
        description: promo.code,
      })
    },
    onError: (error) => {
      showToast({
        type: 'error',
        title: 'Не удалось изменить статус промокода',
        description: extractErrorMessage(error),
      })
    },
    onSettled: () => {
      setPendingRowId(null)
      setDeactivationTarget(null)
    },
  })

  const handleDrawerClose = () => {
    if (saveMutation.isPending) {
      return
    }

    if (isDirty) {
      setDiscardModalOpen(true)
      return
    }

    closeDrawerImmediately()
  }

  const openCreateDrawer = () => {
    setSelectedPromo(null)
    setDrawerMode('create')
    setDiscardModalOpen(false)
    saveMutation.reset()
    reset(emptyPromo)
  }

  const openEditDrawer = (promo: PromoCode) => {
    setSelectedPromo(promo)
    setDrawerMode('edit')
    setDiscardModalOpen(false)
    saveMutation.reset()
    reset(toPromoPayload(promo))
  }

  const filteredPromos = useMemo(() => {
    const query = search.trim().toLowerCase()

    return (promosQuery.data ?? []).filter((promo) => {
      if (statusFilter === 'ACTIVE' && !promo.active) {
        return false
      }

      if (statusFilter === 'INACTIVE' && promo.active) {
        return false
      }

      if (!query) {
        return true
      }

      return promo.code.toLowerCase().includes(query)
    })
  }, [promosQuery.data, search, statusFilter])

  const drawerError = saveMutation.isError ? extractErrorMessage(saveMutation.error) : null
  const submitLabel = saveMutation.isPending ? 'Сохраняем...' : drawerMode === 'edit' ? 'Сохранить' : 'Создать промокод'

  const handleStatusChange = (promo: PromoCode) => {
    if (promo.active) {
      setDeactivationTarget(promo)
      return
    }

    toggleMutation.mutate({ id: promo.id, active: true })
  }

  const handleSubmitForm = handleSubmit((values) => {
    const payload: PromoPayload = {
      code: values.code.trim().toUpperCase(),
      type: values.type,
      value: values.value,
      minOrderAmount: values.minOrderAmount,
      active: selectedPromo?.active ?? true,
      validFrom: normalizeOptionalText(values.validFrom),
      validTo: normalizeOptionalText(values.validTo),
      usageLimit: values.usageLimit ?? null,
    }

    saveMutation.mutate(payload)
  })

  return (
    <AdminPageLayout
      title="Промокоды"
      subtitle="Создание скидок, управление периодами действия и доступностью."
      actions={
        <Button type="button" onClick={openCreateDrawer}>
          Создать промокод
        </Button>
      }
    >
      <AdminFilterToolbar
        search={search}
        searchPlaceholder="Поиск по коду промокода"
        onSearchChange={setSearch}
        filters={activityFilterOptions}
        activeFilter={statusFilter}
        onFilterChange={setStatusFilter}
        filtersAriaLabel="Фильтр по статусу промокодов"
      />

      <AdminPageState
        isLoading={promosQuery.isLoading}
        isError={promosQuery.isError || !promosQuery.data}
        error={promosQuery.error}
        isEmpty={filteredPromos.length === 0}
        loadingLabel="Загружаем промокоды"
        emptyTitle="Ничего не найдено"
        emptyDescription="Измените фильтр или создайте новый промокод."
        emptyAction={
          <Button type="button" onClick={openCreateDrawer}>
            Создать промокод
          </Button>
        }
      >
        <Card>
          <table className="table">
            <thead>
              <tr>
                <th>Код</th>
                <th>Скидка</th>
                <th>Мин. сумма</th>
                <th>Период</th>
                <th>Использование</th>
                <th>Состояние</th>
                <th>Действия</th>
              </tr>
            </thead>
            <tbody>
              {filteredPromos.map((promo) => {
                const lifecycle = getPromoLifecycle(promo)
                const isPending = pendingRowId === promo.id

                return (
                  <tr key={promo.id}>
                    <td>
                      <strong>{promo.code}</strong>
                    </td>
                    <td>{promo.type === 'FIXED' ? formatMoney(promo.value) : `${promo.value}%`}</td>
                    <td>{formatMoney(promo.minOrderAmount)}</td>
                    <td className="muted">
                      {promo.validFrom ? formatDate(promo.validFrom) : 'Без даты начала'}
                      <br />
                      {promo.validTo ? `До ${formatDate(promo.validTo)}` : 'Без даты окончания'}
                    </td>
                    <td>
                      {promo.usageLimit ? `${promo.usedCount} / ${promo.usageLimit}` : `${promo.usedCount} использ.`}
                    </td>
                    <td>
                      <Badge tone={promoLifecycleTone(lifecycle)}>{promoLifecycleLabel(lifecycle)}</Badge>
                    </td>
                    <td>
                      <div className="admin-inline-actions">
                        <Button
                          variant="neutral"
                          size="sm"
                          type="button"
                          onClick={() => openEditDrawer(promo)}
                          disabled={isPending}
                        >
                          Редактировать
                        </Button>
                        <Button
                          variant={promo.active ? 'danger-muted' : 'neutral'}
                          size="sm"
                          type="button"
                          onClick={() => handleStatusChange(promo)}
                          disabled={isPending}
                        >
                          {isPending ? 'Обновляем...' : promo.active ? 'Деактивировать' : 'Активировать'}
                        </Button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </Card>
      </AdminPageState>

      <AdminEntityDrawer
        open={drawerMode !== null}
        title={drawerMode === 'edit' ? 'Редактировать промокод' : 'Новый промокод'}
        description={
          drawerMode === 'edit'
            ? 'Изменения сохраняются без потери текущего списка и фильтра.'
            : 'Создайте промокод и сразу вернитесь к списку активных акций.'
        }
        error={drawerError}
        onClose={handleDrawerClose}
        footer={
          <>
            <Button variant="neutral" type="button" onClick={handleDrawerClose} disabled={saveMutation.isPending}>
              Отмена
            </Button>
            <Button type="submit" form="promo-drawer-form" disabled={saveMutation.isPending}>
              {submitLabel}
            </Button>
          </>
        }
      >
        <form id="promo-drawer-form" onSubmit={handleSubmitForm}>
          <div className="form-row">
            <label htmlFor="promo-code">Код</label>
            <Input
              id="promo-code"
              hasError={Boolean(errors.code)}
              placeholder="SAVE10"
              {...register('code', { required: 'Введите код промокода' })}
            />
            {errors.code ? <div className="error-text">{errors.code.message}</div> : null}
          </div>

          <div className="admin-form-grid">
            <div className="form-row">
              <label htmlFor="promo-type">Тип скидки</label>
              <Select id="promo-type" {...register('type')}>
                <option value="FIXED">Фиксированная скидка</option>
                <option value="PERCENT">Процент</option>
              </Select>
            </div>

            <div className="form-row">
              <label htmlFor="promo-value">Размер скидки</label>
              <Input
                id="promo-value"
                hasError={Boolean(errors.value)}
                type="number"
                step="0.01"
                min={0.01}
                {...register('value', {
                  valueAsNumber: true,
                  required: 'Укажите размер скидки',
                  min: { value: 0.01, message: 'Скидка должна быть больше 0' },
                  validate: (value) =>
                    promoType !== 'PERCENT' || value <= 100 || 'Процент скидки не может быть больше 100',
                })}
              />
              {errors.value ? <div className="error-text">{errors.value.message}</div> : null}
            </div>
          </div>

          <div className="admin-form-grid">
            <div className="form-row">
              <label htmlFor="promo-min-order">Минимальная сумма заказа</label>
              <Input
                id="promo-min-order"
                hasError={Boolean(errors.minOrderAmount)}
                type="number"
                step="0.01"
                min={0}
                {...register('minOrderAmount', {
                  valueAsNumber: true,
                  required: 'Укажите минимальную сумму заказа',
                  min: { value: 0, message: 'Минимальная сумма не может быть отрицательной' },
                })}
              />
              {errors.minOrderAmount ? <div className="error-text">{errors.minOrderAmount.message}</div> : null}
            </div>

            <div className="form-row">
              <label htmlFor="promo-usage-limit">Лимит использований</label>
              <Input
                id="promo-usage-limit"
                hasError={Boolean(errors.usageLimit)}
                type="number"
                min={1}
                placeholder="Необязательно"
                {...register('usageLimit', {
                  setValueAs: (value) => (value === '' ? null : Number(value)),
                  validate: (value) => value == null || value >= 1 || 'Лимит должен быть больше 0',
                })}
              />
              {errors.usageLimit ? <div className="error-text">{errors.usageLimit.message}</div> : null}
            </div>
          </div>

          <div className="admin-form-grid">
            <div className="form-row">
              <label htmlFor="promo-valid-from">Начало действия</label>
              <Input id="promo-valid-from" type="datetime-local" {...register('validFrom')} />
            </div>

            <div className="form-row">
              <label htmlFor="promo-valid-to">Окончание действия</label>
              <Input
                id="promo-valid-to"
                type="datetime-local"
                hasError={Boolean(errors.validTo)}
                {...register('validTo', {
                  validate: (value) => {
                    if (!value || !validFrom) {
                      return true
                    }

                    return value >= validFrom || 'Дата окончания не может быть раньше даты начала'
                  },
                })}
              />
              {errors.validTo ? <div className="error-text">{errors.validTo.message}</div> : null}
            </div>
          </div>
        </form>
      </AdminEntityDrawer>

      <Modal
        open={deactivationTarget !== null}
        title="Деактивировать промокод?"
        description={
          deactivationTarget
            ? `Промокод «${deactivationTarget.code}» перестанет применяться, но его можно будет снова активировать.`
            : undefined
        }
        confirmLabel="Деактивировать"
        confirmTone="danger-muted"
        cancelTone="neutral"
        pending={toggleMutation.isPending}
        onClose={() => {
          if (!toggleMutation.isPending) {
            setDeactivationTarget(null)
          }
        }}
        onConfirm={() => {
          if (deactivationTarget) {
            toggleMutation.mutate({ id: deactivationTarget.id, active: false })
          }
        }}
      />

      <Modal
        open={discardModalOpen}
        title="Закрыть без сохранения?"
        description="Несохраненные изменения будут потеряны."
        confirmLabel="Не сохранять"
        cancelLabel="Продолжить редактирование"
        confirmTone="danger-muted"
        cancelTone="neutral"
        onClose={() => setDiscardModalOpen(false)}
        onConfirm={closeDrawerImmediately}
      />
    </AdminPageLayout>
  )
}
