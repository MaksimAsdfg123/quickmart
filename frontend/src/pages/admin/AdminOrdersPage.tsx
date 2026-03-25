import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState } from 'react'

import { adminApi } from '../../api/adminApi'
import { useToast } from '../../shared/components/ToastProvider'
import { Badge } from '../../shared/components/ui/Badge'
import { Button } from '../../shared/components/ui/Button'
import { Card } from '../../shared/components/ui/Card'
import { Drawer } from '../../shared/components/ui/Drawer'
import { EmptyState } from '../../shared/components/ui/EmptyState'
import { Input } from '../../shared/components/ui/Input'
import { Loader } from '../../shared/components/ui/Loader'
import { Modal } from '../../shared/components/ui/Modal'
import { extractErrorMessage } from '../../shared/lib/errors'
import {
  formatDate,
  formatDateTime,
  formatMoney,
  formatOrderNumber,
  formatOrderSupportCode,
  orderStatusLabel,
  orderStatusTone,
  paymentMethodLabel,
  paymentStatusLabel,
  paymentStatusTone,
} from '../../shared/lib/format'
import type { Order, OrderStatus, OrderSummary, PageResponse } from '../../shared/types'
import { AdminTabs } from './AdminTabs'

type OrderListFilter = 'ACTIVE' | 'ALL' | OrderStatus

const orderFilters: Array<{ value: OrderListFilter; label: string }> = [
  { value: 'ACTIVE', label: 'Активные' },
  { value: 'CREATED', label: 'Создан' },
  { value: 'CONFIRMED', label: 'Подтвержден' },
  { value: 'ASSEMBLING', label: 'Собирается' },
  { value: 'OUT_FOR_DELIVERY', label: 'В доставке' },
  { value: 'DELIVERED', label: 'Доставлен' },
  { value: 'CANCELLED', label: 'Отменен' },
  { value: 'ALL', label: 'Все' },
]

const workflowSteps: OrderStatus[] = ['CREATED', 'CONFIRMED', 'ASSEMBLING', 'OUT_FOR_DELIVERY', 'DELIVERED']
const activeStatuses: OrderStatus[] = ['CREATED', 'CONFIRMED', 'ASSEMBLING', 'OUT_FOR_DELIVERY']

function toOrderSummary(order: Order): OrderSummary {
  return {
    id: order.id,
    status: order.status,
    total: order.total,
    paymentMethod: order.paymentMethod,
    paymentStatus: order.paymentStatus,
    itemsCount: order.items.reduce((sum, item) => sum + item.quantity, 0),
    deliveryDate: order.deliveryDate,
    deliveryStartTime: order.deliveryStartTime,
    deliveryEndTime: order.deliveryEndTime,
    createdAt: order.createdAt,
  }
}

function matchesFilter(statusFilter: OrderListFilter, status: OrderStatus): boolean {
  if (statusFilter === 'ALL') {
    return true
  }

  if (statusFilter === 'ACTIVE') {
    return activeStatuses.includes(status)
  }

  return status === statusFilter
}

function getOrderActions(
  order: Order,
): Array<{ label: string; status: OrderStatus; variant: 'primary' | 'danger-muted' }> {
  switch (order.status) {
    case 'CREATED':
      return [
        { label: 'Подтвердить', status: 'CONFIRMED', variant: 'primary' },
        { label: 'Отменить', status: 'CANCELLED', variant: 'danger-muted' },
      ]
    case 'CONFIRMED':
      return [
        { label: 'Передать в сборку', status: 'ASSEMBLING', variant: 'primary' },
        { label: 'Отменить', status: 'CANCELLED', variant: 'danger-muted' },
      ]
    case 'ASSEMBLING':
      return [{ label: 'Передать курьеру', status: 'OUT_FOR_DELIVERY', variant: 'primary' }]
    case 'OUT_FOR_DELIVERY':
      return [{ label: 'Отметить доставленным', status: 'DELIVERED', variant: 'primary' }]
    default:
      return []
  }
}

export function AdminOrdersPage() {
  const queryClient = useQueryClient()
  const { showToast } = useToast()
  const [selectedOrderId, setSelectedOrderId] = useState<string | null>(null)
  const [statusFilter, setStatusFilter] = useState<OrderListFilter>('ACTIVE')
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [cancelTarget, setCancelTarget] = useState<Order | null>(null)

  const normalizedQuery = search.trim()
  const useServerSearch = normalizedQuery.length > 0 && !/^\d{1,6}$/.test(normalizedQuery)
  const ordersQueryKey = ['admin-orders', page, statusFilter, normalizedQuery] as const

  const ordersQuery = useQuery({
    queryKey: ordersQueryKey,
    queryFn: () =>
      adminApi.listOrders({
        page,
        size: 10,
        status: statusFilter === 'ALL' ? undefined : statusFilter,
        query: useServerSearch ? normalizedQuery : undefined,
      }),
  })

  const detailsQuery = useQuery({
    queryKey: ['admin-order-details', selectedOrderId],
    queryFn: () => adminApi.getOrder(selectedOrderId!),
    enabled: Boolean(selectedOrderId),
  })

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: OrderStatus }) => adminApi.updateOrderStatus(id, status),
    onSuccess: (order) => {
      queryClient.setQueryData<Order | undefined>(['admin-order-details', order.id], order)
      queryClient.setQueryData<PageResponse<OrderSummary> | undefined>(ordersQueryKey, (current) => {
        if (!current) {
          return current
        }

        const nextSummary = toOrderSummary(order)
        const exists = current.content.some((item) => item.id === order.id)

        if (!exists) {
          return current
        }

        const shouldKeep = matchesFilter(statusFilter, order.status)
        const content = shouldKeep
          ? current.content.map((item) => (item.id === order.id ? nextSummary : item))
          : current.content.filter((item) => item.id !== order.id)

        return {
          ...current,
          content,
          totalElements: shouldKeep ? current.totalElements : Math.max(0, current.totalElements - 1),
        }
      })
      queryClient.invalidateQueries({ queryKey: ['admin-orders'] })
      showToast({
        type: 'success',
        title: 'Статус заказа обновлен',
        description: `Заказ №${formatOrderNumber(order.id)}`,
      })
      setCancelTarget(null)
    },
    onError: (error) => {
      showToast({
        type: 'error',
        title: 'Не удалось обновить статус заказа',
        description: extractErrorMessage(error),
      })
    },
  })

  const currentOrder = detailsQuery.data
  const currentActions = currentOrder ? getOrderActions(currentOrder) : []

  const localFilteredOrders = useMemo(() => {
    const query = normalizedQuery.toLowerCase()

    return (ordersQuery.data?.content ?? []).filter((order) => {
      if (!query) {
        return true
      }

      return (
        formatOrderSupportCode(order.id).toLowerCase().includes(query) ||
        formatOrderNumber(order.id).includes(query.replace(/\D/g, ''))
      )
    })
  }, [normalizedQuery, ordersQuery.data])

  return (
    <div className="page admin-page">
      <AdminTabs />

      <section className="page-head">
        <div>
          <h1 className="page-title">Заказы</h1>
          <p className="page-subtitle">
            Операционная очередь: быстрый triage и смена статусов только в деталях заказа.
          </p>
        </div>
      </section>

      <Card>
        <div className="admin-toolbar">
          <Input
            placeholder="Поиск по коду заказа, номеру или адресу"
            value={search}
            onChange={(event) => {
              setSearch(event.target.value)
              setPage(0)
            }}
          />
          <div className="admin-filters" aria-label="Фильтр по статусу заказов">
            {orderFilters.map((filter) => (
              <button
                key={filter.value}
                type="button"
                className={['chip', statusFilter === filter.value ? 'active' : ''].join(' ').trim()}
                onClick={() => {
                  setStatusFilter(filter.value)
                  setPage(0)
                }}
              >
                {filter.label}
              </button>
            ))}
          </div>
        </div>
      </Card>

      {ordersQuery.isLoading ? (
        <Loader label="Загружаем заказы" />
      ) : ordersQuery.isError || !ordersQuery.data ? (
        <Card>
          <div className="error">{extractErrorMessage(ordersQuery.error)}</div>
        </Card>
      ) : localFilteredOrders.length === 0 ? (
        <EmptyState
          title="Заказы не найдены"
          description="Измените фильтр, номер заказа, код поддержки или адрес доставки."
        />
      ) : (
        <>
          <Card>
            <table className="table">
              <thead>
                <tr>
                  <th>Заказ</th>
                  <th>Статус</th>
                  <th>Доставка</th>
                  <th>Оплата</th>
                  <th>Сумма</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {localFilteredOrders.map((order) => (
                  <tr key={order.id}>
                    <td>
                      <div className="admin-row-main">
                        <strong>№{formatOrderNumber(order.id)}</strong>
                        <div className="admin-row-support">
                          <span className="muted muted--compact">Код {formatOrderSupportCode(order.id)}</span>
                          <span className="muted muted--compact">{order.itemsCount} поз.</span>
                        </div>
                      </div>
                    </td>
                    <td>
                      <Badge tone={orderStatusTone(order.status)}>{orderStatusLabel(order.status)}</Badge>
                    </td>
                    <td>
                      <div className="admin-row-main">
                        <strong>{formatDate(order.deliveryDate)}</strong>
                        <div className="muted muted--compact">
                          {order.deliveryStartTime}-{order.deliveryEndTime}
                        </div>
                      </div>
                    </td>
                    <td>
                      <div className="admin-row-main">
                        <Badge tone={paymentStatusTone(order.paymentStatus)}>
                          {paymentStatusLabel(order.paymentStatus)}
                        </Badge>
                        <div className="muted muted--compact">{paymentMethodLabel(order.paymentMethod)}</div>
                      </div>
                    </td>
                    <td>
                      <div className="admin-row-main">
                        <strong>{formatMoney(order.total)}</strong>
                        <div className="muted muted--compact">{formatDateTime(order.createdAt)}</div>
                      </div>
                    </td>
                    <td>
                      <Button variant="neutral" size="sm" type="button" onClick={() => setSelectedOrderId(order.id)}>
                        Открыть
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </Card>

          {ordersQuery.data.totalPages > 1 ? (
            <div className="admin-pagination">
              <span className="muted">
                Страница {ordersQuery.data.page + 1} из {ordersQuery.data.totalPages}
              </span>
              <div className="admin-inline-actions">
                <Button
                  variant="neutral"
                  type="button"
                  disabled={page === 0}
                  onClick={() => setPage((prev) => prev - 1)}
                >
                  Назад
                </Button>
                <Button
                  type="button"
                  disabled={page + 1 >= ordersQuery.data.totalPages}
                  onClick={() => setPage((prev) => prev + 1)}
                >
                  Далее
                </Button>
              </div>
            </div>
          ) : null}
        </>
      )}

      <Drawer
        open={selectedOrderId !== null}
        title={currentOrder ? `Заказ №${formatOrderNumber(currentOrder.id)}` : 'Детали заказа'}
        description={
          currentOrder
            ? `Код ${formatOrderSupportCode(currentOrder.id)} • создан ${formatDateTime(currentOrder.createdAt)}`
            : 'Проверьте состав, оплату и только затем меняйте статус.'
        }
        onClose={() => {
          if (!updateStatusMutation.isPending) {
            setSelectedOrderId(null)
          }
        }}
        footer={
          <>
            <Button
              variant="neutral"
              type="button"
              onClick={() => setSelectedOrderId(null)}
              disabled={updateStatusMutation.isPending}
            >
              Закрыть
            </Button>
            {currentActions.map((action) => (
              <Button
                key={action.status}
                variant={action.variant}
                type="button"
                disabled={updateStatusMutation.isPending}
                onClick={() => {
                  if (!currentOrder) {
                    return
                  }

                  if (action.status === 'CANCELLED') {
                    setCancelTarget(currentOrder)
                    return
                  }

                  updateStatusMutation.mutate({ id: currentOrder.id, status: action.status })
                }}
              >
                {updateStatusMutation.isPending ? 'Обновляем...' : action.label}
              </Button>
            ))}
          </>
        }
      >
        {detailsQuery.isLoading ? (
          <Loader label="Загружаем детали заказа" />
        ) : detailsQuery.isError || !currentOrder ? (
          <div className="error">{extractErrorMessage(detailsQuery.error)}</div>
        ) : (
          <div className="grid">
            <Card>
              <div className="title-row">
                <h3>Статус заказа</h3>
                <Badge tone={orderStatusTone(currentOrder.status)}>{orderStatusLabel(currentOrder.status)}</Badge>
              </div>
              {currentOrder.status === 'CANCELLED' ? (
                <p className="muted muted--spaced">Заказ отменен. Дополнительные переходы статуса недоступны.</p>
              ) : (
                <div className="status-block">
                  <div className="status-line">
                    {workflowSteps.map((step, index) => {
                      const currentIndex = workflowSteps.indexOf(currentOrder.status)
                      const done = index < currentIndex
                      const active = step === currentOrder.status
                      return (
                        <span
                          key={step}
                          className={['status-step', done ? 'done' : '', active ? 'active' : ''].join(' ').trim()}
                        >
                          {orderStatusLabel(step)}
                        </span>
                      )
                    })}
                  </div>
                </div>
              )}
            </Card>

            <div className="admin-form-grid">
              <Card>
                <h3>Доставка</h3>
                <div className="order-summary">
                  <div className="order-summary__line">
                    <span>Адрес</span>
                    <strong>{currentOrder.addressSnapshot}</strong>
                  </div>
                  <div className="order-summary__line">
                    <span>Слот</span>
                    <strong>
                      {formatDate(currentOrder.deliveryDate)} {currentOrder.deliveryStartTime}-
                      {currentOrder.deliveryEndTime}
                    </strong>
                  </div>
                </div>
              </Card>

              <Card>
                <h3>Оплата</h3>
                <div className="order-summary">
                  <div className="order-summary__line">
                    <span>Способ</span>
                    <strong>{paymentMethodLabel(currentOrder.paymentMethod)}</strong>
                  </div>
                  <div className="order-summary__line">
                    <span>Статус</span>
                    <Badge tone={paymentStatusTone(currentOrder.paymentStatus)}>
                      {paymentStatusLabel(currentOrder.paymentStatus)}
                    </Badge>
                  </div>
                  {currentOrder.promoCode ? (
                    <div className="order-summary__line">
                      <span>Промокод</span>
                      <strong>{currentOrder.promoCode}</strong>
                    </div>
                  ) : null}
                </div>
              </Card>
            </div>

            <Card>
              <h3>Состав заказа</h3>
              <table className="table">
                <thead>
                  <tr>
                    <th>Товар</th>
                    <th>Цена</th>
                    <th>Кол-во</th>
                    <th>Сумма</th>
                  </tr>
                </thead>
                <tbody>
                  {currentOrder.items.map((item) => (
                    <tr key={item.id}>
                      <td>{item.productName}</td>
                      <td>{formatMoney(item.unitPrice)}</td>
                      <td>{item.quantity}</td>
                      <td>{formatMoney(item.lineTotal)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </Card>

            <Card>
              <h3>Итоги</h3>
              <div className="order-summary">
                <div className="order-summary__line">
                  <span>Товары</span>
                  <strong>{formatMoney(currentOrder.subtotal)}</strong>
                </div>
                <div className="order-summary__line">
                  <span>Скидка</span>
                  <strong>{formatMoney(currentOrder.discount)}</strong>
                </div>
                <div className="order-summary__line">
                  <span>Доставка</span>
                  <strong>{formatMoney(currentOrder.deliveryFee)}</strong>
                </div>
                <div className="order-summary__line order-summary__total">
                  <span>Итого</span>
                  <strong>{formatMoney(currentOrder.total)}</strong>
                </div>
              </div>
            </Card>
          </div>
        )}
      </Drawer>

      <Modal
        open={cancelTarget !== null}
        title="Отменить заказ?"
        description={
          cancelTarget ? `Заказ №${formatOrderNumber(cancelTarget.id)} будет переведен в статус «Отменен».` : undefined
        }
        confirmLabel="Отменить заказ"
        confirmTone="danger-muted"
        cancelTone="neutral"
        pending={updateStatusMutation.isPending}
        onClose={() => {
          if (!updateStatusMutation.isPending) {
            setCancelTarget(null)
          }
        }}
        onConfirm={() => {
          if (cancelTarget) {
            updateStatusMutation.mutate({ id: cancelTarget.id, status: 'CANCELLED' })
          }
        }}
      />
    </div>
  )
}
