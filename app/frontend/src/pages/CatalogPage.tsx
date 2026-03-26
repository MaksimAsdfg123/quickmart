import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState } from 'react'

import { cartApi } from '../api/cartApi'
import { catalogApi } from '../api/catalogApi'
import { useToast } from '../shared/components/ToastProvider'
import { Badge } from '../shared/components/ui/Badge'
import { Button } from '../shared/components/ui/Button'
import { ButtonLink } from '../shared/components/ui/ButtonLink'
import { Card } from '../shared/components/ui/Card'
import { EmptyState } from '../shared/components/ui/EmptyState'
import { Input } from '../shared/components/ui/Input'
import { Loader } from '../shared/components/ui/Loader'
import { Skeleton } from '../shared/components/ui/Skeleton'
import { useAuthStore } from '../shared/lib/authStore'
import { optimisticAddToCart } from '../shared/lib/cartOptimistic'
import { extractErrorMessage } from '../shared/lib/errors'
import { formatMoney } from '../shared/lib/format'
import type { Cart, Product } from '../shared/types'

export function CatalogPage() {
  const [categoryId, setCategoryId] = useState<string | undefined>()
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [pendingProductId, setPendingProductId] = useState<string | null>(null)
  const authToken = useAuthStore((state) => state.token)
  const queryClient = useQueryClient()
  const { showToast } = useToast()

  const categoriesQuery = useQuery({ queryKey: ['categories'], queryFn: catalogApi.getCategories })
  const productsQuery = useQuery({
    queryKey: ['products', categoryId, search, page],
    queryFn: () => catalogApi.getProducts({ categoryId, q: search || undefined, page, size: 12 }),
  })

  const addMutation = useMutation({
    mutationFn: ({ productId }: { productId: string; product: Product }) => cartApi.addItem({ productId, quantity: 1 }),
    onMutate: async ({ product }) => {
      setPendingProductId(product.id)
      await queryClient.cancelQueries({ queryKey: ['cart'] })
      const previousCart = queryClient.getQueryData<Cart>(['cart'])
      queryClient.setQueryData<Cart | undefined>(['cart'], (current) => optimisticAddToCart(current, product, 1))
      return { previousCart }
    },
    onError: (error, _, context) => {
      if (context?.previousCart) {
        queryClient.setQueryData(['cart'], context.previousCart)
      }
      showToast({ type: 'error', title: 'Не удалось добавить товар', description: extractErrorMessage(error) })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cart'] })
      showToast({ type: 'success', title: 'Товар добавлен в корзину' })
    },
    onSettled: () => {
      setPendingProductId(null)
    },
  })

  const products = productsQuery.data?.content ?? []
  const canPrev = (productsQuery.data?.page ?? 0) > 0
  const canNext = (productsQuery.data?.page ?? 0) + 1 < (productsQuery.data?.totalPages ?? 0)

  const helperMessage = useMemo(() => {
    if (!authToken) {
      return 'Войдите, чтобы быстро добавлять товары в корзину и оформлять заказ без лишних шагов.'
    }

    return null
  }, [authToken])

  return (
    <div className="page">
      <section className="page-head">
        <div>
          <h1 className="page-title">Каталог</h1>
          <p className="page-subtitle">
            Быстрый выбор продуктов и товаров для дома с понятными действиями на одном экране.
          </p>
        </div>
      </section>

      <Card>
        <div className="toolbar toolbar--catalog toolbar--catalog-compact">
          <Input
            placeholder="Поиск по названию товара"
            value={search}
            onChange={(event) => {
              setSearch(event.target.value)
              setPage(0)
            }}
          />

          <Button
            variant="ghost"
            onClick={() => {
              setSearch('')
              setCategoryId(undefined)
              setPage(0)
            }}
          >
            Сбросить
          </Button>
        </div>

        {categoriesQuery.data && categoriesQuery.data.length > 0 ? (
          <div className="chips chips--spaced">
            <button
              type="button"
              data-category-name="Все"
              className={['chip', !categoryId ? 'active' : ''].join(' ')}
              onClick={() => {
                setCategoryId(undefined)
                setPage(0)
              }}
            >
              Все
            </button>
            {categoriesQuery.data.map((category) => (
              <button
                key={category.id}
                type="button"
                data-category-name={category.name}
                className={['chip', categoryId === category.id ? 'active' : ''].join(' ')}
                onClick={() => {
                  setCategoryId(category.id)
                  setPage(0)
                }}
              >
                {category.name}
              </button>
            ))}
          </div>
        ) : null}
      </Card>

      {productsQuery.isLoading ? (
        <div className="product-grid">
          {Array.from({ length: 8 }).map((_, index) => (
            <Card key={index}>
              <div className="product-card">
                <Skeleton height={22} width="65%" />
                <Skeleton height={14} width="40%" />
                <Skeleton height={48} />
                <Skeleton height={18} width="32%" />
                <Skeleton height={44} />
              </div>
            </Card>
          ))}
        </div>
      ) : productsQuery.isError ? (
        <Card>
          <div className="error">{extractErrorMessage(productsQuery.error)}</div>
        </Card>
      ) : products.length === 0 ? (
        <EmptyState
          title="Ничего не найдено"
          description="Попробуйте изменить поисковый запрос или выбрать другую категорию."
        />
      ) : (
        <>
          <div className="product-grid">
            {products.map((product) => {
              const isPending = pendingProductId === product.id

              return (
                <Card key={product.id} className="card--interactive">
                  <article
                    className="product-card"
                    data-product-id={product.id}
                    data-product-name={product.name}
                  >
                    <div className="product-card__meta">
                      <h3 className="product-card__title">{product.name}</h3>
                      <Badge tone={product.availableQuantity > 0 ? 'success' : 'danger'}>
                        {product.availableQuantity > 0 ? `В наличии: ${product.availableQuantity}` : 'Нет в наличии'}
                      </Badge>
                    </div>
                    <div className="muted">{product.categoryName}</div>
                    <div className="product-card__description">{product.description || 'Описание скоро появится.'}</div>
                    <div>
                      <strong>{formatMoney(product.price)}</strong>
                    </div>
                    <div className="product-card__actions">
                      <Button
                        variant="primary"
                        size="md"
                        disabled={!authToken || isPending || product.availableQuantity <= 0}
                        onClick={() => addMutation.mutate({ productId: product.id, product })}
                      >
                        {isPending ? 'Добавляем...' : 'В корзину'}
                      </Button>
                      <ButtonLink variant="secondary" size="md" to={`/products/${product.id}`}>
                        Подробнее
                      </ButtonLink>
                    </div>
                    {!authToken ? <div className="muted muted--compact">Войдите, чтобы добавить товар.</div> : null}
                  </article>
                </Card>
              )
            })}
          </div>

          <Card>
            <div className="cart-row-actions cart-row-actions--between">
              <div className="muted">
                Страница {(productsQuery.data?.page ?? 0) + 1} из {productsQuery.data?.totalPages ?? 1}
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
            {helperMessage ? <div className="muted muted--spaced">{helperMessage}</div> : null}
          </Card>
        </>
      )}

      {categoriesQuery.isLoading ? <Loader compact label="Загружаем категории" /> : null}
      {categoriesQuery.isError ? <div className="error">{extractErrorMessage(categoriesQuery.error)}</div> : null}
    </div>
  )
}
