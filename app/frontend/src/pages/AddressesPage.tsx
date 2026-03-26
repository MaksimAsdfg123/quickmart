import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate, useSearchParams } from 'react-router-dom'

import { addressApi, type AddressPayload } from '../api/addressApi'
import { useToast } from '../shared/components/ToastProvider'
import { Badge } from '../shared/components/ui/Badge'
import { Button } from '../shared/components/ui/Button'
import { Card } from '../shared/components/ui/Card'
import { EmptyState } from '../shared/components/ui/EmptyState'
import { FormShell } from '../shared/components/ui/FormShell'
import { Input } from '../shared/components/ui/Input'
import { Loader } from '../shared/components/ui/Loader'
import { Modal } from '../shared/components/ui/Modal'
import { extractErrorMessage } from '../shared/lib/errors'
import type { Address } from '../shared/types'

const defaultAddressValues: AddressPayload = {
  label: 'Дом',
  city: 'Екатеринбург',
  street: '',
  house: '',
  apartment: '',
  entrance: '',
  floor: '',
  comment: '',
  isDefault: false,
}

const addressFieldNames: Array<keyof AddressPayload> = [
  'label',
  'city',
  'street',
  'house',
  'apartment',
  'entrance',
  'floor',
  'comment',
  'isDefault',
]

function toFormValues(address: Address): AddressPayload {
  return {
    label: address.label,
    city: address.city,
    street: address.street,
    house: address.house,
    apartment: address.apartment ?? '',
    entrance: address.entrance ?? '',
    floor: address.floor ?? '',
    comment: address.comment ?? '',
    isDefault: address.isDefault,
  }
}

export function AddressesPage() {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const returnTo = searchParams.get('returnTo')
  const { showToast } = useToast()
  const [deletingId, setDeletingId] = useState<string | null>(null)
  const [editingId, setEditingId] = useState<string | null>(null)

  const addressesQuery = useQuery({ queryKey: ['addresses'], queryFn: addressApi.list })

  const {
    register,
    handleSubmit,
    reset,
    setError,
    clearErrors,
    formState: { errors },
  } = useForm<AddressPayload>({
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    defaultValues: defaultAddressValues,
  })

  const saveMutation = useMutation({
    mutationFn: async ({ payload, addressId }: { payload: AddressPayload; addressId: string | null }) =>
      addressId ? addressApi.update(addressId, payload) : addressApi.create(payload),
    onSuccess: (_, variables) => {
      const isEdit = Boolean(variables.addressId)
      queryClient.invalidateQueries({ queryKey: ['addresses'] })
      reset(defaultAddressValues)
      setEditingId(null)

      if (returnTo && !isEdit) {
        showToast({
          type: 'success',
          title: 'Адрес сохранен',
          description: 'Возвращаемся к оформлению заказа',
        })
        navigate(returnTo)
        return
      }

      showToast({ type: 'success', title: isEdit ? 'Адрес обновлен' : 'Адрес сохранен' })
    },
    onError: (error) => {
      if (axios.isAxiosError(error)) {
        const fieldErrors = (error.response?.data as { fieldErrors?: Record<string, string> } | undefined)?.fieldErrors

        if (fieldErrors) {
          Object.entries(fieldErrors).forEach(([fieldName, message]) => {
            if ((addressFieldNames as string[]).includes(fieldName)) {
              setError(fieldName as keyof AddressPayload, { type: 'server', message })
            }
          })
        }
      }

      showToast({ type: 'error', title: 'Не удалось сохранить адрес', description: extractErrorMessage(error) })
    },
  })

  const deleteMutation = useMutation({
    mutationFn: addressApi.remove,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['addresses'] })

      if (deletingId && deletingId === editingId) {
        setEditingId(null)
        reset(defaultAddressValues)
      }

      setDeletingId(null)
      showToast({ type: 'success', title: 'Адрес удален' })
    },
    onError: (error) => {
      showToast({ type: 'error', title: 'Не удалось удалить адрес', description: extractErrorMessage(error) })
    },
  })

  const deletingAddress = addressesQuery.data?.find((address) => address.id === deletingId)
  const emptyAddressesDescription = 'Добавьте первый адрес в форме справа.'

  return (
    <div className="page">
      <section className="page-head">
        <div>
          <h1 className="page-title">Адреса доставки</h1>
          <p className="page-subtitle">Сохраните несколько адресов для быстрого оформления заказа.</p>
        </div>
        {returnTo ? (
          <Button variant="secondary" onClick={() => navigate(returnTo)}>
            Вернуться к оформлению
          </Button>
        ) : null}
      </section>

      <div className="details-layout">
        <Card className="details-stack">
          <div className="title-row">
            <h3>Мои адреса</h3>
          </div>
          {addressesQuery.isLoading ? (
            <Loader label="Загружаем адреса" compact />
          ) : addressesQuery.isError ? (
            <div className="error">{extractErrorMessage(addressesQuery.error)}</div>
          ) : (addressesQuery.data?.length ?? 0) === 0 ? (
            <EmptyState title="Нет сохраненных адресов" description={emptyAddressesDescription} />
          ) : (
            <div className="pane-list">
              {addressesQuery.data?.map((address) => (
                <Card key={address.id}>
                  <div className="order-summary">
                    <div className="order-summary__line">
                      <strong>{address.label}</strong>
                      {address.isDefault ? <Badge tone="success">По умолчанию</Badge> : null}
                    </div>
                    <div>
                      {address.city}, {address.street} {address.house}
                    </div>
                    <div className="admin-inline-actions">
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => {
                          setEditingId(address.id)
                          clearErrors()
                          reset(toFormValues(address))
                        }}
                      >
                        Редактировать
                      </Button>
                      <Button variant="danger" size="sm" onClick={() => setDeletingId(address.id)}>
                        Удалить
                      </Button>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          )}
        </Card>

        <FormShell
          title={editingId ? 'Редактирование адреса' : 'Новый адрес'}
          subtitle={
            editingId ? 'Сохраните несколько адресов для быстрого оформления заказа.' : emptyAddressesDescription
          }
          width="fluid"
          variant="section"
        >
          <form
            onSubmit={handleSubmit((values) => {
              clearErrors()
              saveMutation.mutate({ payload: values, addressId: editingId })
            })}
          >
            <div className="form-row">
              <label htmlFor="label">Название</label>
              <Input
                id="label"
                hasError={Boolean(errors.label)}
                placeholder="Дом, офис, дача"
                maxLength={100}
                {...register('label', {
                  required: 'Укажите название адреса',
                  validate: (value) => value.trim().length > 0 || 'Укажите название адреса',
                  maxLength: { value: 100, message: 'Максимум 100 символов' },
                })}
              />
              {errors.label ? <div className="error-text">{errors.label.message}</div> : null}
            </div>

            <div className="form-row">
              <label htmlFor="city">Город</label>
              <Input
                id="city"
                hasError={Boolean(errors.city)}
                placeholder="Екатеринбург"
                maxLength={100}
                {...register('city', {
                  required: 'Укажите город',
                  validate: (value) => value.trim().length > 0 || 'Укажите город',
                  maxLength: { value: 100, message: 'Максимум 100 символов' },
                })}
              />
              {errors.city ? <div className="error-text">{errors.city.message}</div> : null}
            </div>

            <div className="form-row">
              <label htmlFor="street">Улица</label>
              <Input
                id="street"
                hasError={Boolean(errors.street)}
                placeholder="Ленина"
                maxLength={150}
                {...register('street', {
                  required: 'Укажите улицу',
                  validate: (value) => value.trim().length > 0 || 'Укажите улицу',
                  maxLength: { value: 150, message: 'Максимум 150 символов' },
                })}
              />
              {errors.street ? <div className="error-text">{errors.street.message}</div> : null}
            </div>

            <div className="form-row">
              <label htmlFor="house">Дом</label>
              <Input
                id="house"
                hasError={Boolean(errors.house)}
                placeholder="10"
                maxLength={30}
                {...register('house', {
                  required: 'Укажите номер дома',
                  validate: (value) => value.trim().length > 0 || 'Укажите номер дома',
                  maxLength: { value: 30, message: 'Максимум 30 символов' },
                })}
              />
              {errors.house ? <div className="error-text">{errors.house.message}</div> : null}
            </div>

            <div className="form-row">
              <label htmlFor="apartment">Квартира (необязательно)</label>
              <Input
                id="apartment"
                placeholder="25"
                maxLength={30}
                {...register('apartment', { maxLength: { value: 30, message: 'Максимум 30 символов' } })}
              />
              {errors.apartment ? <div className="error-text">{errors.apartment.message}</div> : null}
            </div>

            <div className="form-row">
              <label htmlFor="entrance">Подъезд (необязательно)</label>
              <Input
                id="entrance"
                placeholder="2"
                maxLength={30}
                {...register('entrance', { maxLength: { value: 30, message: 'Максимум 30 символов' } })}
              />
              {errors.entrance ? <div className="error-text">{errors.entrance.message}</div> : null}
            </div>

            <div className="form-row">
              <label htmlFor="floor">Этаж (необязательно)</label>
              <Input
                id="floor"
                placeholder="7"
                maxLength={30}
                {...register('floor', { maxLength: { value: 30, message: 'Максимум 30 символов' } })}
              />
              {errors.floor ? <div className="error-text">{errors.floor.message}</div> : null}
            </div>

            <div className="form-row">
              <label htmlFor="comment">Комментарий (необязательно)</label>
              <Input
                id="comment"
                placeholder="Домофон не работает"
                maxLength={300}
                {...register('comment', { maxLength: { value: 300, message: 'Максимум 300 символов' } })}
              />
              {errors.comment ? <div className="error-text">{errors.comment.message}</div> : null}
            </div>

            <label>
              <input type="checkbox" {...register('isDefault')} /> Сделать адресом по умолчанию
            </label>

            {saveMutation.isError ? <div className="error">{extractErrorMessage(saveMutation.error)}</div> : null}

            <div className="form-actions">
              <Button type="submit" variant="primary" size="lg" block disabled={saveMutation.isPending}>
                {saveMutation.isPending ? 'Сохраняем...' : editingId ? 'Сохранить изменения' : 'Сохранить адрес'}
              </Button>

              {editingId ? (
                <Button
                  type="button"
                  variant="ghost"
                  size="lg"
                  block
                  onClick={() => {
                    setEditingId(null)
                    clearErrors()
                    reset(defaultAddressValues)
                  }}
                >
                  Отмена
                </Button>
              ) : null}
            </div>
          </form>
        </FormShell>
      </div>

      <Modal
        open={Boolean(deletingAddress)}
        title="Удалить адрес?"
        description={deletingAddress ? `Адрес «${deletingAddress.label}» будет удален.` : undefined}
        confirmLabel="Удалить"
        confirmTone="danger"
        pending={deleteMutation.isPending}
        onClose={() => setDeletingId(null)}
        onConfirm={() => {
          if (deletingAddress) {
            deleteMutation.mutate(deletingAddress.id)
          }
        }}
      />
    </div>
  )
}
