import type { Address } from '../shared/types'
import { apiClient } from './client'

export interface AddressPayload {
  label: string
  city: string
  street: string
  house: string
  apartment?: string | null
  entrance?: string | null
  floor?: string | null
  comment?: string | null
  isDefault: boolean
}

function normalizeRequired(value: string): string {
  return value.trim()
}

function normalizeOptional(value?: string | null): string | null {
  const normalized = value?.trim()
  return normalized ? normalized : null
}

function normalizePayload(payload: AddressPayload): AddressPayload {
  return {
    label: normalizeRequired(payload.label),
    city: normalizeRequired(payload.city),
    street: normalizeRequired(payload.street),
    house: normalizeRequired(payload.house),
    apartment: normalizeOptional(payload.apartment),
    entrance: normalizeOptional(payload.entrance),
    floor: normalizeOptional(payload.floor),
    comment: normalizeOptional(payload.comment),
    isDefault: Boolean(payload.isDefault),
  }
}

export const addressApi = {
  list: async (): Promise<Address[]> => {
    const { data } = await apiClient.get<Address[]>('/api/addresses')
    return data
  },

  create: async (payload: AddressPayload): Promise<Address> => {
    const { data } = await apiClient.post<Address>('/api/addresses', normalizePayload(payload))
    return data
  },

  update: async (id: string, payload: AddressPayload): Promise<Address> => {
    const { data } = await apiClient.put<Address>(`/api/addresses/${id}`, normalizePayload(payload))
    return data
  },

  remove: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/addresses/${id}`)
  },
}
