import type { DeliverySlot, Order, OrderSummary, PageResponse, PaymentMethod } from '../shared/types'
import { apiClient } from './client'

export interface CheckoutPayload {
  addressId: string
  deliverySlotId: string
  promoCode?: string
  paymentMethod: PaymentMethod
}

export const orderApi = {
  listDeliverySlots: async (): Promise<DeliverySlot[]> => {
    const { data } = await apiClient.get<DeliverySlot[]>('/api/delivery-slots')
    return data
  },

  checkout: async (payload: CheckoutPayload): Promise<Order> => {
    const { data } = await apiClient.post<Order>('/api/orders/checkout', payload)
    return data
  },

  myOrders: async (params: { page?: number; size?: number }): Promise<PageResponse<OrderSummary>> => {
    const { data } = await apiClient.get<PageResponse<OrderSummary>>('/api/orders', { params })
    return data
  },

  myOrder: async (id: string): Promise<Order> => {
    const { data } = await apiClient.get<Order>(`/api/orders/${id}`)
    return data
  },

  cancelMyOrder: async (id: string): Promise<Order> => {
    const { data } = await apiClient.post<Order>(`/api/orders/${id}/cancel`)
    return data
  },
}
