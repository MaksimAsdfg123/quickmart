import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'

import { cartApi } from '../api/cartApi'
import { useToast } from '../shared/components/ToastProvider'
import { Button } from '../shared/components/ui/Button'
import { ButtonLink } from '../shared/components/ui/ButtonLink'
import { Card } from '../shared/components/ui/Card'
import { EmptyState } from '../shared/components/ui/EmptyState'
import { Loader } from '../shared/components/ui/Loader'
import { Modal } from '../shared/components/ui/Modal'
import { optimisticRemoveCartItem, optimisticUpdateCartItem } from '../shared/lib/cartOptimistic'
import { extractErrorMessage } from '../shared/lib/errors'
import { formatMoney } from '../shared/lib/format'
import type { Cart } from '../shared/types'

export function CartPage() {
  const queryClient = useQueryClient()
  const { showToast } = useToast()
  const [removingProductId, setRemovingProductId] = useState<string | null>(null)
  const [changingProductId, setChangingProductId] = useState<string | null>(null)

  const cartQuery = useQuery({ queryKey: ['cart'], queryFn: cartApi.get })

  const updateMutation = useMutation({
    mutationFn: ({ productId, quantity }: { productId: string; quantity: number }) =>
      cartApi.updateItem(productId, quantity),
    onMutate: async ({ productId, quantity }) => {
      setChangingProductId(productId)
      await queryClient.cancelQueries({ queryKey: ['cart'] })
      const previousCart = queryClient.getQueryData<Cart>(['cart'])
      queryClient.setQueryData<Cart | undefined>(['cart'], (current) =>
        optimisticUpdateCartItem(current, productId, quantity),
      )
      return { previousCart }
    },
    onError: (error, _, context) => {
      if (context?.previousCart) {
        queryClient.setQueryData(['cart'], context.previousCart)
      }
      showToast({ type: 'error', title: 'Не удалось обновить корзину', description: extractErrorMessage(error) })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cart'] })
    },
    onSettled: () => {
      setChangingProductId(null)
    },
  })

  const removeMutation = useMutation({
    mutationFn: (productId: string) => cartApi.removeItem(productId),
    onMutate: async (productId) => {
      await queryClient.cancelQueries({ queryKey: ['cart'] })
      const previousCart = queryClient.getQueryData<Cart>(['cart'])
      queryClient.setQueryData<Cart | undefined>(['cart'], (current) => optimisticRemoveCartItem(current, productId))
      return { previousCart }
    },
    onError: (error, _, context) => {
      if (context?.previousCart) {
        queryClient.setQueryData(['cart'], context.previousCart)
      }
      showToast({ type: 'error', title: 'Не удалось удалить товар', description: extractErrorMessage(error) })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cart'] })
      showToast({ type: 'success', title: 'Товар удален из корзины' })
    },
    onSettled: () => setRemovingProductId(null),
  })

  if (cartQuery.isLoading) {
    return <Loader label="Загружаем корзину" />
  }

  if (cartQuery.isError || !cartQuery.data) {
    return (
      <Card>
        <div className="error">{extractErrorMessage(cartQuery.error)}</div>
      </Card>
    )
  }

  const cart = cartQuery.data
  const itemToRemove = cart.items.find((item) => item.productId === removingProductId)

  return (
    <div className="page">
      <section className="page-head">
        <div>
          <h1 className="page-title">Корзина</h1>
          <p className="page-subtitle">Проверьте состав заказа и переходите к оформлению, когда все готово.</p>
        </div>
      </section>

      {cart.items.length === 0 ? (
        <EmptyState
          title="Корзина пустая"
          description="Добавьте товары из каталога, чтобы оформить заказ."
          action={<ButtonLink to="/">Открыть каталог</ButtonLink>}
        />
      ) : (
        <div className="cart-layout">
          <Card>
            <div className="title-row">
              <h3>Товары в корзине</h3>
            </div>
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Товар</th>
                    <th>Цена</th>
                    <th>Количество</th>
                    <th>Сумма</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {cart.items.map((item) => {
                    const pending = changingProductId === item.productId || removingProductId === item.productId
                    return (
                      <tr
                        key={item.id}
                        data-product-id={item.productId}
                        data-product-name={item.productName}
                      >
                        <td>
                          <strong>{item.productName}</strong>
                          <div className="muted">Доступно: {item.availableQuantity}</div>
                        </td>
                        <td>{formatMoney(item.unitPrice)}</td>
                        <td>
                          <div className="cart-row-actions">
                            <Button
                              variant="ghost"
                              size="sm"
                              disabled={pending || item.quantity <= 1}
                              onClick={() =>
                                updateMutation.mutate({ productId: item.productId, quantity: item.quantity - 1 })
                              }
                            >
                              −
                            </Button>
                            <span>{item.quantity}</span>
                            <Button
                              variant="ghost"
                              size="sm"
                              disabled={pending || item.quantity >= item.availableQuantity}
                              onClick={() =>
                                updateMutation.mutate({ productId: item.productId, quantity: item.quantity + 1 })
                              }
                            >
                              +
                            </Button>
                          </div>
                        </td>
                        <td>{formatMoney(item.lineTotal)}</td>
                        <td>
                          <Button
                            variant="danger"
                            size="sm"
                            disabled={pending}
                            onClick={() => setRemovingProductId(item.productId)}
                          >
                            Удалить
                          </Button>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          </Card>

          <Card className="summary-card">
            <div className="section-stack">
              <h3>Итог</h3>
              <div className="order-summary">
                <div className="order-summary__line">
                  <span>Товаров</span>
                  <strong>{cart.totalItems}</strong>
                </div>
                <div className="order-summary__line">
                  <span>Подытог</span>
                  <strong>{formatMoney(cart.subtotal)}</strong>
                </div>
                <div className="order-summary__line order-summary__total">
                  <span>Предварительно к оплате</span>
                  <strong>{formatMoney(cart.subtotal)}</strong>
                </div>
              </div>
            </div>

            <div className="summary-card__actions">
              <ButtonLink block to="/checkout">
                Перейти к оформлению
              </ButtonLink>
              <div className="summary-card__hint">Скидки и стоимость доставки будут рассчитаны на следующем шаге.</div>
            </div>
          </Card>
        </div>
      )}

      {(updateMutation.isError || removeMutation.isError) && (
        <div className="error">
          {extractErrorMessage(updateMutation.error) || extractErrorMessage(removeMutation.error)}
        </div>
      )}

      <Modal
        open={Boolean(itemToRemove)}
        title="Удалить товар из корзины?"
        description={itemToRemove ? `Товар «${itemToRemove.productName}» будет удален.` : undefined}
        confirmLabel="Удалить"
        confirmTone="danger"
        pending={removeMutation.isPending}
        onClose={() => setRemovingProductId(null)}
        onConfirm={() => {
          if (itemToRemove) {
            removeMutation.mutate(itemToRemove.productId)
          }
        }}
      />
    </div>
  )
}
