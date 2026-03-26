import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'

import { addressApi } from '../api/addressApi'
import { cartApi } from '../api/cartApi'
import { type CheckoutPayload, orderApi } from '../api/orderApi'
import { useToast } from '../shared/components/ToastProvider'
import { Button } from '../shared/components/ui/Button'
import { ButtonLink } from '../shared/components/ui/ButtonLink'
import { Card } from '../shared/components/ui/Card'
import { EmptyState } from '../shared/components/ui/EmptyState'
import { FormShell } from '../shared/components/ui/FormShell'
import { Input } from '../shared/components/ui/Input'
import { Loader } from '../shared/components/ui/Loader'
import { Select } from '../shared/components/ui/Select'
import { extractErrorMessage } from '../shared/lib/errors'
import { formatDate, formatMoney, formatOrderNumber } from '../shared/lib/format'

const BASE_DELIVERY_FEE = 149
const FREE_DELIVERY_THRESHOLD = 1500

export function CheckoutPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { showToast } = useToast()

  const cartQuery = useQuery({ queryKey: ['cart'], queryFn: cartApi.get })
  const addressQuery = useQuery({ queryKey: ['addresses'], queryFn: addressApi.list })
  const slotsQuery = useQuery({ queryKey: ['delivery-slots'], queryFn: orderApi.listDeliverySlots })

  const {
    register,
    watch,
    handleSubmit,
    formState: { errors },
  } = useForm<CheckoutPayload>({
    defaultValues: {
      paymentMethod: 'CARD',
      promoCode: '',
    },
  })

  const checkoutMutation = useMutation({
    mutationFn: orderApi.checkout,
    onSuccess: (order) => {
      queryClient.invalidateQueries({ queryKey: ['cart'] })
      queryClient.invalidateQueries({ queryKey: ['my-orders'] })
      showToast({
        type: 'success',
        title: 'Заказ оформлен',
        description: `Номер заказа: №${formatOrderNumber(order.id)}`,
      })
      navigate(`/checkout/success/${order.id}`)
    },
    onError: (error) => {
      showToast({ type: 'error', title: 'Не удалось оформить заказ', description: extractErrorMessage(error) })
    },
  })

  const addressId = watch('addressId')
  const deliverySlotId = watch('deliverySlotId')
  const promoCode = watch('promoCode')

  if (cartQuery.isLoading || addressQuery.isLoading || slotsQuery.isLoading) {
    return <Loader label="Подготавливаем оформление" />
  }

  if (cartQuery.isError || addressQuery.isError || slotsQuery.isError) {
    return (
      <Card>
        <div className="error">
          {extractErrorMessage(cartQuery.error) ||
            extractErrorMessage(addressQuery.error) ||
            extractErrorMessage(slotsQuery.error)}
        </div>
      </Card>
    )
  }

  if (!cartQuery.data || !addressQuery.data || !slotsQuery.data) {
    return (
      <Card>
        <div className="error">Не удалось загрузить данные для оформления заказа.</div>
      </Card>
    )
  }

  if (cartQuery.data.items.length === 0) {
    return (
      <EmptyState
        title="Корзина пустая"
        description="Добавьте товары в корзину, чтобы перейти к оформлению."
        action={<ButtonLink to="/">В каталог</ButtonLink>}
      />
    )
  }

  if (addressQuery.data.length === 0) {
    return (
      <EmptyState
        title="Добавьте адрес доставки"
        description="Без адреса нельзя оформить заказ."
        action={<ButtonLink to="/profile/addresses?returnTo=/checkout">Перейти к адресам</ButtonLink>}
      />
    )
  }

  if (slotsQuery.data.length === 0) {
    return <EmptyState title="Нет доступных слотов" description="Попробуйте оформить заказ позже." />
  }

  const subtotal = Number(cartQuery.data.subtotal)
  const deliveryFee = subtotal >= FREE_DELIVERY_THRESHOLD ? 0 : BASE_DELIVERY_FEE
  const estimatedTotal = subtotal + deliveryFee
  const canSubmit = Boolean(addressId) && Boolean(deliverySlotId) && !checkoutMutation.isPending

  return (
    <div className="page">
      <section className="page-head">
        <div>
          <h1 className="page-title">Оформление заказа</h1>
          <p className="page-subtitle">Один экран: адрес, слот, промокод, оплата и итог заказа.</p>
        </div>
      </section>

      <div className="checkout-layout">
        <FormShell
          title="Адрес, слот и оплата"
          subtitle="Заполните данные и подтвердите заказ без переключения между шагами."
          width="fluid"
          variant="section"
        >
          <form
            onSubmit={handleSubmit((values) => {
              checkoutMutation.mutate({
                ...values,
                promoCode: values.promoCode?.trim() ? values.promoCode : undefined,
              })
            })}
          >
            <div className="form-row">
              <label htmlFor="addressId">Адрес доставки</label>
              <Select
                id="addressId"
                hasError={Boolean(errors.addressId)}
                {...register('addressId', { required: 'Выберите адрес' })}
              >
                <option value="">Выберите адрес</option>
                {addressQuery.data.map((address) => (
                  <option key={address.id} value={address.id}>
                    {address.label}: {address.city}, {address.street} {address.house}
                  </option>
                ))}
              </Select>
              {errors.addressId ? <div className="error-text">{errors.addressId.message}</div> : null}
            </div>

            <div className="form-row">
              <label htmlFor="deliverySlotId">Временной слот</label>
              <Select
                id="deliverySlotId"
                hasError={Boolean(errors.deliverySlotId)}
                {...register('deliverySlotId', { required: 'Выберите слот доставки' })}
              >
                <option value="">Выберите слот</option>
                {slotsQuery.data.map((slot) => (
                  <option key={slot.id} value={slot.id}>
                    {formatDate(slot.slotDate)} {slot.startTime}-{slot.endTime}
                  </option>
                ))}
              </Select>
              {errors.deliverySlotId ? <div className="error-text">{errors.deliverySlotId.message}</div> : null}
            </div>

            <div className="form-row">
              <label htmlFor="promoCode">Промокод</label>
              <Input
                id="promoCode"
                placeholder="Например, SAVE10"
                {...register('promoCode')}
              />
              <div className="muted muted--compact">Промокод проверяется сервером при подтверждении заказа.</div>
            </div>

            <div className="form-row">
              <label htmlFor="paymentMethod">Способ оплаты</label>
              <Select
                id="paymentMethod"
                {...register('paymentMethod', { required: true })}
              >
                <option value="CARD">Банковская карта</option>
                <option value="CASH">Наличными курьеру</option>
                <option value="MOCK_ONLINE">Онлайн (mock)</option>
              </Select>
            </div>

            {checkoutMutation.isError ? (
              <div className="error">{extractErrorMessage(checkoutMutation.error)}</div>
            ) : null}

            <Button
              type="submit"
              variant="primary"
              size="lg"
              block
              disabled={!canSubmit}
            >
              Подтвердить заказ
            </Button>
          </form>
        </FormShell>

        <Card className="summary-card">
          <div className="section-stack">
            <h3>Итог заказа</h3>
            <div className="order-summary">
              <div className="order-summary__line">
                <span>Товаров</span>
                <strong>{cartQuery.data.totalItems}</strong>
              </div>
              <div className="order-summary__line">
                <span>Подытог</span>
                <strong>{formatMoney(cartQuery.data.subtotal)}</strong>
              </div>
              <div className="order-summary__line">
                <span>Доставка</span>
                <strong>{deliveryFee === 0 ? 'Бесплатно' : formatMoney(deliveryFee)}</strong>
              </div>
              <div className="order-summary__line">
                <span>Промокод</span>
                <strong>{promoCode?.trim() ? promoCode.trim().toUpperCase() : 'Не применен'}</strong>
              </div>
              <div className="order-summary__line order-summary__total">
                <span>Оценка к оплате</span>
                <strong>{formatMoney(estimatedTotal)}</strong>
              </div>
            </div>
          </div>

          <div className="summary-card__hint">
            Финальная сумма учитывает проверку промокода, доступность слота и актуальные остатки на сервере.
          </div>
        </Card>
      </div>
    </div>
  )
}
