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
  formatOrderSupportCode,
  orderStatusLabel,
  paymentMethodLabel,
  paymentStatusLabel,
} from '../shared/lib/format'
import type { OrderStatus } from '../shared/types'

const ORDER_FLOW: OrderStatus[] = ['CREATED', 'CONFIRMED', 'ASSEMBLING', 'OUT_FOR_DELIVERY', 'DELIVERED']

function buildStatusSteps(status: OrderStatus) {
  if (status === 'CANCELLED') {
    return ORDER_FLOW.map((value) => ({ status: value, state: 'idle' as const }))
  }

  const currentIndex = ORDER_FLOW.indexOf(status)

  return ORDER_FLOW.map((value, index) => ({
    status: value,
    state: index < currentIndex ? ('done' as const) : index === currentIndex ? ('active' as const) : ('idle' as const),
  }))
}

export function CheckoutSuccessPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { showToast } = useToast()
  const [itemsExpanded, setItemsExpanded] = useState(false)
  const [confirmCancelOpen, setConfirmCancelOpen] = useState(false)

  const orderQuery = useQuery({
    queryKey: ['my-order', id],
    queryFn: () => orderApi.myOrder(id!),
    enabled: Boolean(id),
  })

  const cancelMutation = useMutation({
    mutationFn: () => orderApi.cancelMyOrder(id!),
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
        <div className="success-actions success-actions--compact">
          <Button variant="secondary" onClick={() => navigate('/orders')}>
            К заказам
          </Button>
          <Button variant="ghost" onClick={() => navigate('/')}>
            В каталог
          </Button>
        </div>
      </Card>
    )
  }

  const order = orderQuery.data
  const displayOrderNumber = formatOrderNumber(order.id)
  const supportCode = formatOrderSupportCode(order.id)
  const canCancel = order.status === 'CREATED' || order.status === 'CONFIRMED'
  const statusSteps = buildStatusSteps(order.status)

  return (
    <div className="page">
      <section className="page-head">
        <div>
          <h1 className="page-title">Заказ оформлен</h1>
          <p className="page-subtitle">
            №{displayOrderNumber} · создан {formatDate(order.createdAt)}
          </p>
          <p className="muted">Код заказа: {supportCode}</p>
        </div>
        <Badge tone={order.status === 'CANCELLED' ? 'danger' : 'success'}>{orderStatusLabel(order.status)}</Badge>
      </section>

      <div className="checkout-layout">
        <Card>
          <h3>Доставка</h3>
          <div className="order-summary">
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
                {paymentMethodLabel(order.paymentMethod)} / {paymentStatusLabel(order.paymentStatus)}
              </strong>
            </div>
            <div className="order-summary__line">
              <span>ETA</span>
              <strong>
                {formatDate(order.deliveryDate)} до {order.deliveryEndTime}
              </strong>
            </div>
          </div>

          <div className="status-block">
            <div className="muted">Статус</div>
            <div className="status-line">
              {statusSteps.map((step) => (
                <span key={step.status} className={['status-step', step.state].join(' ')}>
                  {orderStatusLabel(step.status)}
                </span>
              ))}
              {order.status === 'CANCELLED' ? <Badge tone="danger">Отменен</Badge> : null}
            </div>
          </div>
        </Card>

        <Card>
          <h3>Итог</h3>
          <div className="order-summary">
            <div className="order-summary__line">
              <span>Товаров</span>
              <strong>{order.items.length}</strong>
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

          <div className="success-actions success-actions--stack">
            <Button variant="primary" onClick={() => navigate(`/orders/${order.id}`)}>
              Перейти к заказу
            </Button>
            {canCancel ? (
              <Button variant="danger" onClick={() => setConfirmCancelOpen(true)} disabled={cancelMutation.isPending}>
                Отменить
              </Button>
            ) : null}
            <Button variant="secondary" onClick={() => navigate('/')}>
              В каталог
            </Button>
          </div>
        </Card>
      </div>

      <Card>
        <div className="title-row">
          <h3>Товары в заказе</h3>
          <Button variant="ghost" size="sm" onClick={() => setItemsExpanded((value) => !value)}>
            {itemsExpanded ? 'Скрыть' : 'Показать'}
          </Button>
        </div>

        {itemsExpanded ? (
          <table className="table">
            <thead>
              <tr>
                <th>Товар</th>
                <th>Количество</th>
                <th>Сумма</th>
              </tr>
            </thead>
            <tbody>
              {order.items.map((item) => (
                <tr key={item.id}>
                  <td>{item.productName}</td>
                  <td>{item.quantity}</td>
                  <td>{formatMoney(item.lineTotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p className="muted">Список товаров свернут для удобства.</p>
        )}
      </Card>

      <Modal
        open={confirmCancelOpen}
        title="Отменить заказ?"
        description="После отмены заказ восстановить нельзя."
        confirmLabel="Отменить заказ"
        confirmTone="danger"
        pending={cancelMutation.isPending}
        onClose={() => setConfirmCancelOpen(false)}
        onConfirm={() => cancelMutation.mutate()}
      />
    </div>
  )
}
