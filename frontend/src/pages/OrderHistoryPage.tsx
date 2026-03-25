import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router-dom'

import { orderApi } from '../api/orderApi'
import { Badge } from '../shared/components/ui/Badge'
import { Button } from '../shared/components/ui/Button'
import { Card } from '../shared/components/ui/Card'
import { EmptyState } from '../shared/components/ui/EmptyState'
import { Loader } from '../shared/components/ui/Loader'
import { extractErrorMessage } from '../shared/lib/errors'
import { formatDate, formatMoney, formatOrderNumber, orderStatusLabel } from '../shared/lib/format'

export function OrderHistoryPage() {
  const [page, setPage] = useState(0)

  const query = useQuery({
    queryKey: ['my-orders', page],
    queryFn: () => orderApi.myOrders({ page, size: 10 }),
  })

  if (query.isLoading) {
    return <Loader label="Загружаем историю заказов" />
  }

  if (query.isError || !query.data) {
    return (
      <Card>
        <div className="error">{extractErrorMessage(query.error)}</div>
      </Card>
    )
  }

  if (query.data.content.length === 0 && page === 0) {
    return <EmptyState title="Заказов пока нет" description="Оформите первый заказ в каталоге." />
  }

  const canPrev = page > 0
  const canNext = page + 1 < Math.max(query.data.totalPages, 1)

  return (
    <div className="page">
      <section className="page-head">
        <div>
          <h1 className="page-title">Мои заказы</h1>
          <p className="page-subtitle">История покупок и текущие статусы доставки на одной странице.</p>
        </div>
      </section>

      {query.data.content.length === 0 ? (
        <Card>
          <div className="muted">На этой странице пока нет заказов.</div>
        </Card>
      ) : (
        <div className="grid">
          {query.data.content.map((order) => (
            <Link key={order.id} to={`/orders/${order.id}`}>
              <Card className="card--interactive">
                <div className="order-summary">
                  <div className="order-summary__line">
                    <strong>Заказ №{formatOrderNumber(order.id)}</strong>
                    <Badge
                      tone={order.status === 'CANCELLED' ? 'danger' : order.status === 'DELIVERED' ? 'success' : 'info'}
                    >
                      {orderStatusLabel(order.status)}
                    </Badge>
                  </div>
                  <div className="order-summary__line">
                    <span>Доставка</span>
                    <strong>
                      {formatDate(order.deliveryDate)} {order.deliveryStartTime}-{order.deliveryEndTime}
                    </strong>
                  </div>
                  <div className="order-summary__line order-summary__total">
                    <span>Сумма</span>
                    <strong>{formatMoney(order.total)}</strong>
                  </div>
                </div>
              </Card>
            </Link>
          ))}
        </div>
      )}

      <Card>
        <div className="cart-row-actions cart-row-actions--between">
          <div className="muted">
            Страница {page + 1} из {Math.max(query.data.totalPages, 1)}
          </div>
          <div className="cart-row-actions">
            <Button variant="ghost" disabled={!canPrev} onClick={() => setPage((value) => value - 1)}>
              Назад
            </Button>
            <Button variant="ghost" disabled={!canNext} onClick={() => setPage((value) => value + 1)}>
              Далее
            </Button>
          </div>
        </div>
      </Card>
    </div>
  )
}
