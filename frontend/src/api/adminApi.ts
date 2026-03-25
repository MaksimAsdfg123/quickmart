import type {
  Category,
  InventoryStock,
  Order,
  OrderStatus,
  OrderSummary,
  PageResponse,
  Product,
  PromoCode,
} from '../shared/types'
import { apiClient } from './client'

export interface CategoryPayload {
  name: string
  description?: string | null
  active: boolean
}

export interface ProductPayload {
  name: string
  description?: string | null
  price: number
  categoryId: string
  imageUrl?: string | null
  active: boolean
}

export interface PromoPayload {
  code: string
  type: 'FIXED' | 'PERCENT'
  value: number
  minOrderAmount: number
  active: boolean
  validFrom?: string | null
  validTo?: string | null
  usageLimit?: number | null
}

export const adminApi = {
  listCategories: async (): Promise<Category[]> => {
    const { data } = await apiClient.get<Category[]>('/api/admin/categories')
    return data
  },

  createCategory: async (payload: CategoryPayload): Promise<Category> => {
    const { data } = await apiClient.post<Category>('/api/admin/categories', payload)
    return data
  },

  updateCategory: async (id: string, payload: CategoryPayload): Promise<Category> => {
    const { data } = await apiClient.put<Category>(`/api/admin/categories/${id}`, payload)
    return data
  },

  setCategoryActive: async (id: string, active: boolean): Promise<Category> => {
    const { data } = await apiClient.put<Category>(`/api/admin/categories/${id}/active`, null, { params: { active } })
    return data
  },

  deleteCategory: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/admin/categories/${id}`)
  },

  listProducts: async (params: { page?: number; size?: number }): Promise<PageResponse<Product>> => {
    const { data } = await apiClient.get<PageResponse<Product>>('/api/admin/products', { params })
    return data
  },

  createProduct: async (payload: ProductPayload): Promise<Product> => {
    const { data } = await apiClient.post<Product>('/api/admin/products', payload)
    return data
  },

  updateProduct: async (id: string, payload: ProductPayload): Promise<Product> => {
    const { data } = await apiClient.put<Product>(`/api/admin/products/${id}`, payload)
    return data
  },

  setProductActive: async (id: string, active: boolean): Promise<Product> => {
    const { data } = await apiClient.put<Product>(`/api/admin/products/${id}/active`, null, { params: { active } })
    return data
  },

  deactivateProduct: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/admin/products/${id}`)
  },

  listInventory: async (): Promise<InventoryStock[]> => {
    const { data } = await apiClient.get<InventoryStock[]>('/api/admin/inventory')
    return data
  },

  updateInventory: async (productId: string, availableQuantity: number): Promise<InventoryStock> => {
    const { data } = await apiClient.put<InventoryStock>(`/api/admin/inventory/${productId}`, { availableQuantity })
    return data
  },

  listOrders: async (params: {
    page?: number
    size?: number
    status?: string
    query?: string
  }): Promise<PageResponse<OrderSummary>> => {
    const { data } = await apiClient.get<PageResponse<OrderSummary>>('/api/admin/orders', { params })
    return data
  },

  getOrder: async (id: string): Promise<Order> => {
    const { data } = await apiClient.get<Order>(`/api/admin/orders/${id}`)
    return data
  },

  updateOrderStatus: async (id: string, status: OrderStatus): Promise<Order> => {
    const { data } = await apiClient.put<Order>(`/api/admin/orders/${id}/status`, { status })
    return data
  },

  listPromos: async (): Promise<PromoCode[]> => {
    const { data } = await apiClient.get<PromoCode[]>('/api/admin/promocodes')
    return data
  },

  createPromo: async (payload: PromoPayload): Promise<PromoCode> => {
    const { data } = await apiClient.post<PromoCode>('/api/admin/promocodes', payload)
    return data
  },

  updatePromo: async (id: string, payload: PromoPayload): Promise<PromoCode> => {
    const { data } = await apiClient.put<PromoCode>(`/api/admin/promocodes/${id}`, payload)
    return data
  },

  togglePromo: async (id: string, active: boolean): Promise<PromoCode> => {
    const { data } = await apiClient.put<PromoCode>(`/api/admin/promocodes/${id}/active`, null, { params: { active } })
    return data
  },
}
