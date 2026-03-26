import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'

import { adminApi, type ProductPayload } from '../../api/adminApi'
import { useToast } from '../../shared/components/ToastProvider'
import { Badge } from '../../shared/components/ui/Badge'
import { Button } from '../../shared/components/ui/Button'
import { Card } from '../../shared/components/ui/Card'
import { Input } from '../../shared/components/ui/Input'
import { Modal } from '../../shared/components/ui/Modal'
import { Select } from '../../shared/components/ui/Select'
import { extractErrorMessage } from '../../shared/lib/errors'
import { formatMoney } from '../../shared/lib/format'
import type { Category, PageResponse, Product } from '../../shared/types'
import { AdminEntityDrawer } from './AdminEntityDrawer'
import { AdminFilterToolbar } from './AdminFilterToolbar'
import { AdminPageLayout } from './AdminPageLayout'
import { AdminPageState } from './AdminPageState'
import {
  activityFilterOptions,
  type AdminActivityFilter,
  matchesActivityFilter,
  normalizeOptionalText,
} from './adminShared'

type DrawerMode = 'create' | 'edit' | null

const emptyProduct: ProductPayload = {
  name: '',
  description: '',
  price: 0,
  categoryId: '',
  imageUrl: '',
  active: true,
}

function toProductPayload(product: Product): ProductPayload {
  return {
    name: product.name,
    description: product.description ?? '',
    price: Number(product.price),
    categoryId: product.categoryId,
    imageUrl: product.imageUrl ?? '',
    active: product.active,
  }
}

export function AdminProductsPage() {
  const queryClient = useQueryClient()
  const { showToast } = useToast()
  const [drawerMode, setDrawerMode] = useState<DrawerMode>(null)
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null)
  const [statusFilter, setStatusFilter] = useState<AdminActivityFilter>('ACTIVE')
  const [search, setSearch] = useState('')
  const [pendingRowId, setPendingRowId] = useState<string | null>(null)
  const [deactivationTarget, setDeactivationTarget] = useState<Product | null>(null)
  const [discardModalOpen, setDiscardModalOpen] = useState(false)

  const productsQuery = useQuery({
    queryKey: ['admin-products'],
    queryFn: () => adminApi.listProducts({ page: 0, size: 50 }),
  })
  const categoriesQuery = useQuery({
    queryKey: ['admin-categories'],
    queryFn: () => adminApi.listCategories(),
  })

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<ProductPayload>({
    defaultValues: emptyProduct,
  })

  const syncProductInCache = (nextProduct: Product) => {
    queryClient.setQueryData<PageResponse<Product> | undefined>(['admin-products'], (current) => {
      if (!current) {
        return current
      }

      const exists = current.content.some((item) => item.id === nextProduct.id)
      const content = exists
        ? current.content.map((item) => (item.id === nextProduct.id ? nextProduct : item))
        : [nextProduct, ...current.content]

      return {
        ...current,
        content,
        totalElements: exists ? current.totalElements : current.totalElements + 1,
      }
    })
  }

  const closeDrawerImmediately = () => {
    setDrawerMode(null)
    setSelectedProduct(null)
    setDiscardModalOpen(false)
    reset(emptyProduct)
  }

  const saveMutation = useMutation({
    mutationFn: (payload: ProductPayload) => {
      if (drawerMode === 'edit' && selectedProduct) {
        return adminApi.updateProduct(selectedProduct.id, payload)
      }

      return adminApi.createProduct(payload)
    },
    onSuccess: (product) => {
      syncProductInCache(product)
      queryClient.invalidateQueries({ queryKey: ['admin-products'] })
      showToast({
        type: 'success',
        title: drawerMode === 'edit' ? 'Товар обновлен' : 'Товар создан',
        description: product.name,
      })
      closeDrawerImmediately()
    },
    onError: (error) => {
      showToast({
        type: 'error',
        title: 'Не удалось сохранить товар',
        description: extractErrorMessage(error),
      })
    },
  })

  const toggleMutation = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) => adminApi.setProductActive(id, active),
    onMutate: ({ id }) => {
      setPendingRowId(id)
    },
    onSuccess: (product) => {
      syncProductInCache(product)
      queryClient.invalidateQueries({ queryKey: ['admin-products'] })
      showToast({
        type: 'success',
        title: product.active ? 'Товар активирован' : 'Товар деактивирован',
        description: product.name,
      })
    },
    onError: (error) => {
      showToast({
        type: 'error',
        title: 'Не удалось изменить статус товара',
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
    setSelectedProduct(null)
    setDrawerMode('create')
    setDiscardModalOpen(false)
    saveMutation.reset()
    reset(emptyProduct)
  }

  const openEditDrawer = (product: Product) => {
    setSelectedProduct(product)
    setDrawerMode('edit')
    setDiscardModalOpen(false)
    saveMutation.reset()
    reset(toProductPayload(product))
  }

  const categories = categoriesQuery.data ?? []
  const hasCategories = categories.length > 0
  const drawerOpen = drawerMode !== null

  const filteredProducts = useMemo(() => {
    const query = search.trim().toLowerCase()

    return (productsQuery.data?.content ?? []).filter((product) => {
      if (!matchesActivityFilter(product.active, statusFilter)) {
        return false
      }

      if (!query) {
        return true
      }

      return (
        product.name.toLowerCase().includes(query) ||
        product.categoryName.toLowerCase().includes(query) ||
        (product.description ?? '').toLowerCase().includes(query)
      )
    })
  }, [productsQuery.data, search, statusFilter])

  const drawerError = saveMutation.isError
    ? extractErrorMessage(saveMutation.error)
    : categoriesQuery.isError
      ? extractErrorMessage(categoriesQuery.error)
      : null

  const submitDisabled =
    saveMutation.isPending || categoriesQuery.isLoading || categoriesQuery.isError || !hasCategories

  const submitLabel = saveMutation.isPending
    ? drawerMode === 'edit'
      ? 'Сохраняем...'
      : 'Создаем...'
    : drawerMode === 'edit'
      ? 'Сохранить'
      : 'Создать товар'

  const emptyTitle = search || statusFilter !== 'ACTIVE' ? 'Ничего не найдено' : 'Товаров пока нет'
  const emptyDescription =
    search || statusFilter !== 'ACTIVE'
      ? 'Измените фильтр или поисковый запрос.'
      : 'Создайте первый товар и затем управляйте его статусом без ухода со страницы.'

  const handleStatusChange = (product: Product) => {
    if (product.active) {
      setDeactivationTarget(product)
      return
    }

    toggleMutation.mutate({ id: product.id, active: true })
  }

  const handleSubmitForm = handleSubmit((values) => {
    const payload: ProductPayload = {
      name: values.name.trim(),
      description: normalizeOptionalText(values.description),
      price: values.price,
      categoryId: values.categoryId,
      imageUrl: normalizeOptionalText(values.imageUrl),
      active: selectedProduct?.active ?? true,
    }

    saveMutation.mutate(payload)
  })

  return (
    <AdminPageLayout
      title="Товары"
      subtitle="Быстрое управление карточками и статусами каталога."
      actions={
        <Button type="button" onClick={openCreateDrawer}>
          Создать товар
        </Button>
      }
    >
      <AdminFilterToolbar
        search={search}
        searchPlaceholder="Поиск по названию, категории или описанию"
        onSearchChange={setSearch}
        filters={activityFilterOptions}
        activeFilter={statusFilter}
        onFilterChange={setStatusFilter}
        filtersAriaLabel="Фильтр по статусу товаров"
      />

      <AdminPageState
        isLoading={productsQuery.isLoading}
        isError={productsQuery.isError || !productsQuery.data}
        error={productsQuery.error}
        isEmpty={filteredProducts.length === 0}
        loadingLabel="Загружаем товары"
        emptyTitle={emptyTitle}
        emptyDescription={emptyDescription}
        emptyAction={
          <Button type="button" onClick={openCreateDrawer}>
            Создать товар
          </Button>
        }
      >
        <Card>
          <table className="table">
            <thead>
              <tr>
                <th>Товар</th>
                <th>Категория</th>
                <th>Цена</th>
                <th>Статус</th>
                <th>Действия</th>
              </tr>
            </thead>
            <tbody>
              {filteredProducts.map((product) => {
                const isPending = pendingRowId === product.id

                return (
                  <tr key={product.id}>
                    <td>
                      <strong>{product.name}</strong>
                      <div className="muted muted--compact">{product.description || 'Без описания'}</div>
                    </td>
                    <td>{product.categoryName}</td>
                    <td>{formatMoney(product.price)}</td>
                    <td>
                      <Badge tone={product.active ? 'success' : 'neutral'}>
                        {product.active ? 'Активен' : 'Неактивен'}
                      </Badge>
                    </td>
                    <td>
                      <div className="admin-inline-actions">
                        <Button
                          variant="neutral"
                          size="sm"
                          type="button"
                          onClick={() => openEditDrawer(product)}
                          disabled={isPending}
                        >
                          Редактировать
                        </Button>
                        <Button
                          variant={product.active ? 'danger-muted' : 'neutral'}
                          size="sm"
                          type="button"
                          onClick={() => handleStatusChange(product)}
                          disabled={isPending}
                        >
                          {isPending ? 'Обновляем...' : product.active ? 'Деактивировать' : 'Активировать'}
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
        open={drawerOpen}
        title={drawerMode === 'edit' ? 'Редактировать товар' : 'Новый товар'}
        description={
          drawerMode === 'edit'
            ? 'Изменения сохраняются без потери фильтра и позиции в списке.'
            : 'Создайте карточку товара и сразу вернитесь к каталогу администрирования.'
        }
        error={drawerError}
        onClose={handleDrawerClose}
        footer={
          <>
            <Button variant="neutral" type="button" onClick={handleDrawerClose} disabled={saveMutation.isPending}>
              Отмена
            </Button>
            <Button type="submit" form="product-drawer-form" disabled={submitDisabled}>
              {submitLabel}
            </Button>
          </>
        }
      >
        <form id="product-drawer-form" onSubmit={handleSubmitForm}>
          <div className="form-row">
            <label htmlFor="product-name">Название</label>
            <Input
              id="product-name"
              hasError={Boolean(errors.name)}
              placeholder="Например, Молоко 2.5%"
              {...register('name', { required: 'Введите название товара' })}
            />
            {errors.name ? <div className="error-text">{errors.name.message}</div> : null}
          </div>

          <div className="form-row">
            <label htmlFor="product-description">Описание</label>
            <textarea
              id="product-description"
              className="textarea"
              placeholder="Короткое описание для карточки товара"
              {...register('description')}
            />
          </div>

          <div className="form-row">
            <label htmlFor="product-price">Цена</label>
            <Input
              id="product-price"
              hasError={Boolean(errors.price)}
              type="number"
              step="0.01"
              min={0.01}
              placeholder="0.00"
              {...register('price', {
                valueAsNumber: true,
                required: 'Укажите цену товара',
                min: { value: 0.01, message: 'Цена должна быть больше 0' },
              })}
            />
            {errors.price ? <div className="error-text">{errors.price.message}</div> : null}
          </div>

          <div className="form-row">
            <label htmlFor="product-category">Категория</label>
            <Select
              id="product-category"
              hasError={Boolean(errors.categoryId)}
              disabled={categoriesQuery.isLoading || categoriesQuery.isError || !hasCategories}
              {...register('categoryId', { required: 'Выберите категорию' })}
            >
              <option value="">Выберите категорию</option>
              {categories.map((category: Category) => (
                <option key={category.id} value={category.id}>
                  {category.active ? category.name : `${category.name} • неактивна`}
                </option>
              ))}
            </Select>
            {errors.categoryId ? <div className="error-text">{errors.categoryId.message}</div> : null}
            {!categoriesQuery.isLoading && !categoriesQuery.isError && !hasCategories ? (
              <div className="muted muted--compact">Сначала создайте хотя бы одну категорию.</div>
            ) : null}
          </div>

          <div className="form-row">
            <label htmlFor="product-imageUrl">URL изображения</label>
            <Input id="product-imageUrl" placeholder="https://example.com/image.jpg" {...register('imageUrl')} />
          </div>
        </form>
      </AdminEntityDrawer>

      <Modal
        open={deactivationTarget !== null}
        title="Деактивировать товар?"
        description={
          deactivationTarget
            ? `Товар «${deactivationTarget.name}» пропадет из активного каталога, но останется доступен для повторной активации.`
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
