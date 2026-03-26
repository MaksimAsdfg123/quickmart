export type Role = 'CUSTOMER' | 'ADMIN'

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface AuthUser {
  id: string
  email: string
  fullName: string
  role: Role
}

export interface AuthResponse {
  token: string
  user: AuthUser
}

export interface Category {
  id: string
  name: string
  description: string | null
  active: boolean
}

export interface Product {
  id: string
  name: string
  description: string | null
  price: string
  categoryId: string
  categoryName: string
  imageUrl: string | null
  active: boolean
  availableQuantity: number
}

export interface CartItem {
  id: string
  productId: string
  productName: string
  unitPrice: string
  quantity: number
  lineTotal: string
  availableQuantity: number
}

export interface Cart {
  id: string
  items: CartItem[]
  totalItems: number
  subtotal: string
}

export interface Address {
  id: string
  label: string
  city: string
  street: string
  house: string
  apartment: string | null
  entrance: string | null
  floor: string | null
  comment: string | null
  isDefault: boolean
}

export type PaymentMethod = 'CASH' | 'CARD' | 'MOCK_ONLINE'

export interface DeliverySlot {
  id: string
  slotDate: string
  startTime: string
  endTime: string
  orderLimit: number
  active: boolean
}

export type OrderStatus = 'CREATED' | 'CONFIRMED' | 'ASSEMBLING' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED'

export interface OrderItem {
  id: string
  productId: string
  productName: string
  unitPrice: string
  quantity: number
  lineTotal: string
}

export interface Order {
  id: string
  status: OrderStatus
  addressSnapshot: string
  deliveryDate: string
  deliveryStartTime: string
  deliveryEndTime: string
  promoCode: string | null
  subtotal: string
  discount: string
  deliveryFee: string
  total: string
  items: OrderItem[]
  paymentMethod: PaymentMethod
  paymentStatus: 'PENDING' | 'PAID' | 'FAILED'
  createdAt: string
}

export interface OrderSummary {
  id: string
  status: OrderStatus
  total: string
  paymentMethod: PaymentMethod
  paymentStatus: 'PENDING' | 'PAID' | 'FAILED'
  itemsCount: number
  deliveryDate: string
  deliveryStartTime: string
  deliveryEndTime: string
  createdAt: string
}

export type PromoType = 'FIXED' | 'PERCENT'

export interface PromoCode {
  id: string
  code: string
  type: PromoType
  value: string
  minOrderAmount: string
  active: boolean
  validFrom: string | null
  validTo: string | null
  usageLimit: number | null
  usedCount: number
}

export interface InventoryStock {
  id: string
  productId: string
  productName: string
  categoryName: string
  price: string
  productActive: boolean
  availableQuantity: number
}
