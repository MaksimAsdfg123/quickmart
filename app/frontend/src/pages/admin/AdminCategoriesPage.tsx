import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'

import { adminApi, type CategoryPayload } from '../../api/adminApi'
import { useToast } from '../../shared/components/ToastProvider'
import { Badge } from '../../shared/components/ui/Badge'
import { Button } from '../../shared/components/ui/Button'
import { Card } from '../../shared/components/ui/Card'
import { Input } from '../../shared/components/ui/Input'
import { Modal } from '../../shared/components/ui/Modal'
import { extractErrorMessage } from '../../shared/lib/errors'
import type { Category } from '../../shared/types'
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

const emptyCategory: CategoryPayload = {
  name: '',
  description: '',
  active: true,
}

function toCategoryPayload(category: Category): CategoryPayload {
  return {
    name: category.name,
    description: category.description ?? '',
    active: category.active,
  }
}

export function AdminCategoriesPage() {
  const queryClient = useQueryClient()
  const { showToast } = useToast()
  const [drawerMode, setDrawerMode] = useState<DrawerMode>(null)
  const [selectedCategory, setSelectedCategory] = useState<Category | null>(null)
  const [statusFilter, setStatusFilter] = useState<AdminActivityFilter>('ACTIVE')
  const [search, setSearch] = useState('')
  const [pendingRowId, setPendingRowId] = useState<string | null>(null)
  const [deactivationTarget, setDeactivationTarget] = useState<Category | null>(null)
  const [discardModalOpen, setDiscardModalOpen] = useState(false)

  const categoriesQuery = useQuery({
    queryKey: ['admin-categories'],
    queryFn: adminApi.listCategories,
  })

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<CategoryPayload>({
    defaultValues: emptyCategory,
  })

  const syncCategoryInCache = (nextCategory: Category) => {
    queryClient.setQueryData<Category[] | undefined>(['admin-categories'], (current) => {
      if (!current) {
        return current
      }

      const exists = current.some((item) => item.id === nextCategory.id)
      const next = exists
        ? current.map((item) => (item.id === nextCategory.id ? nextCategory : item))
        : [nextCategory, ...current]

      return [...next].sort((left, right) => left.name.localeCompare(right.name, 'ru'))
    })
  }

  const closeDrawerImmediately = () => {
    setDrawerMode(null)
    setSelectedCategory(null)
    setDiscardModalOpen(false)
    reset(emptyCategory)
  }

  const saveMutation = useMutation({
    mutationFn: (payload: CategoryPayload) => {
      if (drawerMode === 'edit' && selectedCategory) {
        return adminApi.updateCategory(selectedCategory.id, payload)
      }

      return adminApi.createCategory(payload)
    },
    onSuccess: (category) => {
      syncCategoryInCache(category)
      queryClient.invalidateQueries({ queryKey: ['admin-categories'] })
      showToast({
        type: 'success',
        title: drawerMode === 'edit' ? 'Категория обновлена' : 'Категория создана',
        description: category.name,
      })
      closeDrawerImmediately()
    },
    onError: (error) => {
      showToast({
        type: 'error',
        title: 'Не удалось сохранить категорию',
        description: extractErrorMessage(error),
      })
    },
  })

  const toggleMutation = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) => adminApi.setCategoryActive(id, active),
    onMutate: ({ id }) => {
      setPendingRowId(id)
    },
    onSuccess: (category) => {
      syncCategoryInCache(category)
      queryClient.invalidateQueries({ queryKey: ['admin-categories'] })
      showToast({
        type: 'success',
        title: category.active ? 'Категория активирована' : 'Категория деактивирована',
        description: category.name,
      })
    },
    onError: (error) => {
      showToast({
        type: 'error',
        title: 'Не удалось изменить статус категории',
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
    setSelectedCategory(null)
    setDrawerMode('create')
    setDiscardModalOpen(false)
    saveMutation.reset()
    reset(emptyCategory)
  }

  const openEditDrawer = (category: Category) => {
    setSelectedCategory(category)
    setDrawerMode('edit')
    setDiscardModalOpen(false)
    saveMutation.reset()
    reset(toCategoryPayload(category))
  }

  const filteredCategories = useMemo(() => {
    const query = search.trim().toLowerCase()

    return (categoriesQuery.data ?? []).filter((category) => {
      if (!matchesActivityFilter(category.active, statusFilter)) {
        return false
      }

      if (!query) {
        return true
      }

      return category.name.toLowerCase().includes(query) || (category.description ?? '').toLowerCase().includes(query)
    })
  }, [categoriesQuery.data, search, statusFilter])

  const drawerError = saveMutation.isError ? extractErrorMessage(saveMutation.error) : null
  const submitLabel = saveMutation.isPending
    ? drawerMode === 'edit'
      ? 'Сохраняем...'
      : 'Создаем...'
    : drawerMode === 'edit'
      ? 'Сохранить'
      : 'Создать категорию'

  const emptyTitle = search || statusFilter !== 'ACTIVE' ? 'Ничего не найдено' : 'Категорий пока нет'
  const emptyDescription =
    search || statusFilter !== 'ACTIVE'
      ? 'Измените фильтр или поисковый запрос.'
      : 'Создайте первую категорию и управляйте ее статусом прямо в таблице.'

  const handleStatusChange = (category: Category) => {
    if (category.active) {
      setDeactivationTarget(category)
      return
    }

    toggleMutation.mutate({ id: category.id, active: true })
  }

  const handleSubmitForm = handleSubmit((values) => {
    const payload: CategoryPayload = {
      name: values.name.trim(),
      description: normalizeOptionalText(values.description),
      active: selectedCategory?.active ?? true,
    }

    saveMutation.mutate(payload)
  })

  return (
    <AdminPageLayout
      title="Категории"
      subtitle="Структура каталога и управление доступностью категорий."
      actions={
        <Button type="button" onClick={openCreateDrawer}>
          Создать категорию
        </Button>
      }
    >
      <AdminFilterToolbar
        search={search}
        searchPlaceholder="Поиск по названию или описанию"
        onSearchChange={setSearch}
        filters={activityFilterOptions}
        activeFilter={statusFilter}
        onFilterChange={setStatusFilter}
        filtersAriaLabel="Фильтр по статусу категорий"
      />

      <AdminPageState
        isLoading={categoriesQuery.isLoading}
        isError={categoriesQuery.isError || !categoriesQuery.data}
        error={categoriesQuery.error}
        isEmpty={filteredCategories.length === 0}
        loadingLabel="Загружаем категории"
        emptyTitle={emptyTitle}
        emptyDescription={emptyDescription}
        emptyAction={
          <Button type="button" onClick={openCreateDrawer}>
            Создать категорию
          </Button>
        }
      >
        <Card>
          <table className="table">
            <thead>
              <tr>
                <th>Категория</th>
                <th>Описание</th>
                <th>Статус</th>
                <th>Действия</th>
              </tr>
            </thead>
            <tbody>
              {filteredCategories.map((category) => {
                const isPending = pendingRowId === category.id

                return (
                  <tr key={category.id}>
                    <td>
                      <strong>{category.name}</strong>
                    </td>
                    <td className="muted">{category.description || 'Без описания'}</td>
                    <td>
                      <Badge tone={category.active ? 'success' : 'neutral'}>
                        {category.active ? 'Активна' : 'Неактивна'}
                      </Badge>
                    </td>
                    <td>
                      <div className="admin-inline-actions">
                        <Button
                          variant="neutral"
                          size="sm"
                          type="button"
                          onClick={() => openEditDrawer(category)}
                          disabled={isPending}
                        >
                          Редактировать
                        </Button>
                        <Button
                          variant={category.active ? 'danger-muted' : 'neutral'}
                          size="sm"
                          type="button"
                          onClick={() => handleStatusChange(category)}
                          disabled={isPending}
                        >
                          {isPending ? 'Обновляем...' : category.active ? 'Деактивировать' : 'Активировать'}
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
        title={drawerMode === 'edit' ? 'Редактировать категорию' : 'Новая категория'}
        description={
          drawerMode === 'edit'
            ? 'Редактируйте категорию без потери текущего списка и фильтра.'
            : 'Создайте категорию и сразу продолжайте работу со списком.'
        }
        error={drawerError}
        onClose={handleDrawerClose}
        footer={
          <>
            <Button variant="neutral" type="button" onClick={handleDrawerClose} disabled={saveMutation.isPending}>
              Отмена
            </Button>
            <Button type="submit" form="category-drawer-form" disabled={saveMutation.isPending}>
              {submitLabel}
            </Button>
          </>
        }
      >
        <form id="category-drawer-form" onSubmit={handleSubmitForm}>
          <div className="form-row">
            <label htmlFor="category-name">Название</label>
            <Input
              id="category-name"
              hasError={Boolean(errors.name)}
              placeholder="Например, Овощи и фрукты"
              {...register('name', { required: 'Введите название категории' })}
            />
            {errors.name ? <div className="error-text">{errors.name.message}</div> : null}
          </div>

          <div className="form-row">
            <label htmlFor="category-description">Описание</label>
            <textarea
              id="category-description"
              className="textarea"
              placeholder="Короткое пояснение для админки и каталога"
              {...register('description')}
            />
          </div>
        </form>
      </AdminEntityDrawer>

      <Modal
        open={deactivationTarget !== null}
        title="Деактивировать категорию?"
        description={
          deactivationTarget
            ? `Категория «${deactivationTarget.name}» пропадет из активного списка, но ее можно будет включить снова.`
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
