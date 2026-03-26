import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'

import { cartApi } from '../api/cartApi'
import { catalogApi } from '../api/catalogApi'
import { useToast } from '../shared/components/ToastProvider'
import { Badge } from '../shared/components/ui/Badge'
import { Button } from '../shared/components/ui/Button'
import { Card } from '../shared/components/ui/Card'
import { Loader } from '../shared/components/ui/Loader'
import { useAuthStore } from '../shared/lib/authStore'
import { optimisticAddToCart } from '../shared/lib/cartOptimistic'
import { extractErrorMessage } from '../shared/lib/errors'
import { formatMoney } from '../shared/lib/format'
import type { Cart, Product } from '../shared/types'

export function ProductDetailsPage() {
  const { id } = useParams<{ id: string }>()
  const token = useAuthStore((state) => state.token)
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const { showToast } = useToast()

  const productQuery = useQuery({
    queryKey: ['product', id],
    queryFn: () => catalogApi.getProduct(id!),
    enabled: Boolean(id),
  })

  const addMutation = useMutation({
    mutationFn: ({ productId }: { productId: string; product: Product }) => cartApi.addItem({ productId, quantity: 1 }),
    onMutate: async ({ product }) => {
      await queryClient.cancelQueries({ queryKey: ['cart'] })
      const previousCart = queryClient.getQueryData<Cart>(['cart'])
      queryClient.setQueryData<Cart | undefined>(['cart'], (current) => optimisticAddToCart(current, product, 1))
      return { previousCart }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cart'] })
      showToast({ type: 'success', title: 'Товар добавлен в корзину' })
    },
    onError: (error, _, context) => {
      if (context?.previousCart) {
        queryClient.setQueryData(['cart'], context.previousCart)
      }
      showToast({ type: 'error', title: 'Не удалось добавить товар', description: extractErrorMessage(error) })
    },
  })

  if (productQuery.isLoading) {
    return <Loader label="Загружаем товар" />
  }

  if (productQuery.isError || !productQuery.data) {
    return (
      <Card>
        <div className="error">{extractErrorMessage(productQuery.error)}</div>
      </Card>
    )
  }

  const product = productQuery.data

  return (
    <div className="page">
      <section className="page-head">
        <div>
          <h1 className="page-title">{product.name}</h1>
          <p className="page-subtitle">{product.categoryName}</p>
        </div>
        <div className="page-head__actions">
          <Button variant="ghost" onClick={() => navigate(-1)}>
            Назад
          </Button>
        </div>
      </section>

      <div className="details-layout">
        <Card className="details-stack">
          <Badge tone={product.availableQuantity > 0 ? 'success' : 'danger'}>
            {product.availableQuantity > 0 ? `В наличии: ${product.availableQuantity}` : 'Нет в наличии'}
          </Badge>
          <p>{product.description || 'Описание скоро появится.'}</p>
        </Card>

        <Card className="summary-card">
          <div className="section-stack">
            <h3>О товаре</h3>
            <div className="order-summary">
              <div className="order-summary__line">
                <span>Категория</span>
                <strong>{product.categoryName}</strong>
              </div>
              <div className="order-summary__line order-summary__total">
                <span>Цена</span>
                <strong>{formatMoney(product.price)}</strong>
              </div>
            </div>
          </div>

          <div className="summary-card__actions">
            <Button
              variant="primary"
              size="lg"
              block
              disabled={!token || addMutation.isPending || product.availableQuantity <= 0}
              onClick={() => addMutation.mutate({ productId: product.id, product })}
            >
              {addMutation.isPending ? 'Добавляем...' : 'Добавить в корзину'}
            </Button>
            {!token ? (
              <div className="summary-card__hint">Войдите в аккаунт, чтобы добавить товар в корзину.</div>
            ) : null}
          </div>
        </Card>
      </div>

      {addMutation.isError ? <div className="error">{extractErrorMessage(addMutation.error)}</div> : null}
    </div>
  )
}
