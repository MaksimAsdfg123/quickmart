import type { OrderStatus } from '../types'

const moneyFormatter = new Intl.NumberFormat('ru-RU', {
  style: 'currency',
  currency: 'RUB',
  maximumFractionDigits: 2,
})

export function formatMoney(value: string | number): string {
  const numeric = typeof value === 'string' ? Number(value) : value
  if (Number.isNaN(numeric)) {
    return `${value}`
  }
  return moneyFormatter.format(numeric)
}

export function formatDate(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return new Intl.DateTimeFormat('ru-RU', { dateStyle: 'medium' }).format(date)
}

export function formatDateTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return new Intl.DateTimeFormat('ru-RU', { dateStyle: 'medium', timeStyle: 'short' }).format(date)
}

export function orderStatusLabel(status: OrderStatus): string {
  const labels: Record<OrderStatus, string> = {
    CREATED: 'Создан',
    CONFIRMED: 'Подтвержден',
    ASSEMBLING: 'Собирается',
    OUT_FOR_DELIVERY: 'В доставке',
    DELIVERED: 'Доставлен',
    CANCELLED: 'Отменен',
  }

  return labels[status] ?? status
}

export function orderStatusTone(status: OrderStatus): 'info' | 'warning' | 'success' | 'danger' | 'neutral' {
  const tones: Record<OrderStatus, 'info' | 'warning' | 'success' | 'danger' | 'neutral'> = {
    CREATED: 'info',
    CONFIRMED: 'warning',
    ASSEMBLING: 'warning',
    OUT_FOR_DELIVERY: 'info',
    DELIVERED: 'success',
    CANCELLED: 'danger',
  }

  return tones[status] ?? 'neutral'
}

export function paymentStatusLabel(status: 'PENDING' | 'PAID' | 'FAILED'): string {
  const labels: Record<'PENDING' | 'PAID' | 'FAILED', string> = {
    PENDING: 'Ожидает оплаты',
    PAID: 'Оплачен',
    FAILED: 'Неуспешно',
  }

  return labels[status] ?? status
}

export function paymentStatusTone(status: 'PENDING' | 'PAID' | 'FAILED'): 'warning' | 'success' | 'danger' {
  const tones: Record<'PENDING' | 'PAID' | 'FAILED', 'warning' | 'success' | 'danger'> = {
    PENDING: 'warning',
    PAID: 'success',
    FAILED: 'danger',
  }

  return tones[status] ?? 'warning'
}

export function paymentMethodLabel(status: 'CASH' | 'CARD' | 'MOCK_ONLINE'): string {
  const labels: Record<'CASH' | 'CARD' | 'MOCK_ONLINE', string> = {
    CASH: 'Наличными',
    CARD: 'Картой',
    MOCK_ONLINE: 'Онлайн (mock)',
  }

  return labels[status] ?? status
}

export function formatOrderNumber(orderId: string): string {
  const normalized = orderId.replace(/[^a-fA-F0-9]/g, '')
  const seed = normalized.slice(0, 12)
  const numeric = Number.parseInt(seed || '0', 16)
  const fallback = orderId.split('').reduce((sum, char) => sum + char.charCodeAt(0), 0)
  const value = Number.isNaN(numeric) ? fallback : numeric
  return (value % 1_000_000).toString().padStart(6, '0')
}

export function formatOrderSupportCode(orderId: string): string {
  return orderId.slice(0, 8).toUpperCase()
}
