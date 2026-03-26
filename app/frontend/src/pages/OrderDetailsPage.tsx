import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

import { orderApi } from '../api/orderApi'
import { useToast } from '../shared/components/ToastProvider'
import { Badge } from '../shared/components/ui/Badge'
import { Button } from '../shared/components/ui/Button'
import { Card } from '../shared/components/ui/Card'
import { Loader } from '../shared/components/ui/Loader'
import { Modal } from '../shared/components/ui/Modal'
import { extractErrorMessage } from '../shared/lib/errors'
import {
  formatDate,
  formatMoney,
  formatOrderNumber,
  orderStatusLabel,
  paymentMethodLabel,
  paymentStatusLabel,
} from '../shared/lib/format'
import type { OrderStatus } from '../shared/types'

const ORDER_FLOW: OrderStatus[] = ['CREATED', 'CONFIRMED', 'ASSEMBLING', 'OUT_FOR_DELIVERY', 'DELIVERED']

export function OrderDetailsPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { showToast } = useToast()
  const [confirmCancelOpen, setConfirmCancelOpen] = useState(false)

  const orderQuery = useQuery({
    queryKey: ['my-order', id],
    queryFn: () => orderApi.myOrder(id!),
    enabled: Boolean(id),
  })

  const cancelMutation = useMutation({
    mutationFn: (orderId: string) => orderApi.cancelMyOrder(orderId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-order', id] })
      queryClient.invalidateQueries({ queryKey: ['my-orders'] })
      showToast({ type: 'success', title: 'Заказ отменен' })
      setConfirmCancelOpen(false)
    },
    onError: (error) => {
      showToast({ type: 'error', title: 'Не удалось отменить заказ', description: extractErrorMessage(error) })
    },
  })

  if (orderQuery.isLoading) {
    return <Loader label="Загружаем заказ" />
  }

  if (orderQuery.isError || !orderQuery.data) {
    return (
      <Card>
        <div className="error">{extractErrorMessage(orderQuery.error)}</div>
      </Card>
    )
  }

  const order = orderQuery.data
  const canCancel = order.status === 'CREATED' || order.status === 'CONFIRMED'
  const statusSteps =
    order.status === 'CANCELLED'
      ? ORDER_FLOW.map((status) => ({ status, state: 'idle' as const }))
      : ORDER_FLOW.map((status, index) => {
          const currentIndex = ORDER_FLOW.indexOf(order.status)
          return {
            status,
            state:
              index < currentIndex
                ? ('done' as const)
                : index === currentIndex
                  ? ('active' as const)
                  : ('idle' as const),
          }
        })

  return (
    <div className="page" data-order-id={order.id}>
      <section className="page-head">
        <div>
          <h1 className="page-title">Заказ №{formatOrderNumber(order.id)}</h1>
          <p className="page-subtitle">Создан {formatDate(order.createdAt)}</p>
        </div>
        <div className="page-head__actions">
          <Button variant="ghost" onClick={() => navigate(-1)}>
            Назад
          </Button>
          {canCancel ? (
            <Button variant="danger" onClick={() => setConfirmCancelOpen(true)} disabled={cancelMutation.isPending}>
              Отменить заказ
            </Button>
          ) : null}
        </div>
      </section>

      <Card>
        <div className="status-line">
          {statusSteps.map((step) => (
            <span key={step.status} className={['status-step', step.state].join(' ')}>
              {orderStatusLabel(step.status)}
            </span>
          ))}
          {order.status === 'CANCELLED' ? <Badge tone="danger">Отменен</Badge> : null}
        </div>
      </Card>

      <div className="details-layout">
        <Card className="details-stack">
          <h3>Состав заказа</h3>
          <div className="table-wrap">
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
                {order.items.map((item) => (
                  <tr key={item.id}>
                    <td>{item.productName}</td>
                    <td>{formatMoney(item.unitPrice)}</td>
                    <td>{item.quantity}</td>
                    <td>{formatMoney(item.lineTotal)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>

        <Card className="summary-card">
          <div className="section-stack">
            <h3>Доставка и оплата</h3>
            <div className="order-summary">
              <div className="order-summary__line">
                <span>Статус</span>
                <strong>{orderStatusLabel(order.status)}</strong>
              </div>
              <div className="order-summary__line">
                <span>Адрес</span>
                <strong>{order.addressSnapshot}</strong>
              </div>
              <div className="order-summary__line">
                <span>Слот</span>
                <strong>
                  {formatDate(order.deliveryDate)} {order.deliveryStartTime}-{order.deliveryEndTime}
                </strong>
              </div>
              <div className="order-summary__line">
                <span>Оплата</span>
                <strong>
                  {paymentMethodLabel(order.paymentMethod)} · {paymentStatusLabel(order.paymentStatus)}
                </strong>
              </div>
              <div className="order-summary__line">
                <span>Подытог</span>
                <strong>{formatMoney(order.subtotal)}</strong>
              </div>
              <div className="order-summary__line">
                <span>Скидка</span>
                <strong>{formatMoney(order.discount)}</strong>
              </div>
              <div className="order-summary__line">
                <span>Доставка</span>
                <strong>{formatMoney(order.deliveryFee)}</strong>
              </div>
              <div className="order-summary__line order-summary__total">
                <span>Итого</span>
                <strong>{formatMoney(order.total)}</strong>
              </div>
            </div>
          </div>
        </Card>
      </div>

      {cancelMutation.isError ? <div className="error">{extractErrorMessage(cancelMutation.error)}</div> : null}

      <Modal
        open={confirmCancelOpen}
        title="Отменить заказ?"
        description="После отмены заказ восстановить нельзя."
        confirmLabel="Отменить заказ"
        confirmTone="danger"
        pending={cancelMutation.isPending}
        onClose={() => setConfirmCancelOpen(false)}
        onConfirm={() => cancelMutation.mutate(order.id)}
      />
    </div>
  )
}
