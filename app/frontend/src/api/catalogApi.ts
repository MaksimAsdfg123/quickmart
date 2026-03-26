import type { Category, PageResponse, Product } from '../shared/types'
import { apiClient } from './client'

export const catalogApi = {
  getCategories: async (): Promise<Category[]> => {
    const { data } = await apiClient.get<Category[]>('/api/categories')
    return data
  },

  getProducts: async (params: {
    categoryId?: string
    q?: string
    page?: number
    size?: number
  }): Promise<PageResponse<Product>> => {
    const { data } = await apiClient.get<PageResponse<Product>>('/api/products', { params })
    return data
  },

  getProduct: async (id: string): Promise<Product> => {
    const { data } = await apiClient.get<Product>(`/api/products/${id}`)
    return data
  },
}
