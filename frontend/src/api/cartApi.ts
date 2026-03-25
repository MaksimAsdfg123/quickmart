import type { Cart } from '../shared/types'
import { apiClient } from './client'

export const cartApi = {
  get: async (): Promise<Cart> => {
    const { data } = await apiClient.get<Cart>('/api/cart')
    return data
  },

  addItem: async (payload: { productId: string; quantity: number }): Promise<Cart> => {
    const { data } = await apiClient.post<Cart>('/api/cart/items', payload)
    return data
  },

  updateItem: async (productId: string, quantity: number): Promise<Cart> => {
    const { data } = await apiClient.put<Cart>(`/api/cart/items/${productId}`, { quantity })
    return data
  },

  removeItem: async (productId: string): Promise<Cart> => {
    const { data } = await apiClient.delete<Cart>(`/api/cart/items/${productId}`)
    return data
  },
}
