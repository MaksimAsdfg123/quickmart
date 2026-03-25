import type { Cart, CartItem, Product } from '../types'

function toNumber(value: string | number): number {
  const numeric = typeof value === 'number' ? value : Number(value)
  return Number.isFinite(numeric) ? numeric : 0
}

function formatAmount(value: number): string {
  return value.toFixed(2)
}

function lineTotal(unitPrice: string, quantity: number): string {
  return formatAmount(toNumber(unitPrice) * quantity)
}

function subtotal(items: CartItem[]): string {
  const total = items.reduce((acc, item) => acc + toNumber(item.lineTotal), 0)
  return formatAmount(total)
}

export function optimisticAddToCart(current: Cart | undefined, product: Product, quantity: number): Cart | undefined {
  if (!current) {
    return current
  }

  if (product.availableQuantity <= 0 || quantity <= 0) {
    return current
  }

  const existing = current.items.find((item) => item.productId === product.id)
  const items = current.items.map((item) => ({ ...item }))

  if (existing) {
    const nextQuantity = Math.min(existing.quantity + quantity, existing.availableQuantity)
    if (nextQuantity === existing.quantity) {
      return current
    }
    const updated = items.map((item) =>
      item.productId === product.id
        ? {
            ...item,
            quantity: nextQuantity,
            lineTotal: lineTotal(item.unitPrice, nextQuantity),
          }
        : item,
    )

    return {
      ...current,
      items: updated,
      totalItems: updated.reduce((acc, item) => acc + item.quantity, 0),
      subtotal: subtotal(updated),
    }
  }

  const newItem: CartItem = {
    id: `optimistic-${product.id}`,
    productId: product.id,
    productName: product.name,
    unitPrice: product.price,
    quantity: Math.min(quantity, product.availableQuantity),
    lineTotal: lineTotal(product.price, Math.min(quantity, product.availableQuantity)),
    availableQuantity: product.availableQuantity,
  }

  const nextItems = [...items, newItem]
  return {
    ...current,
    items: nextItems,
    totalItems: nextItems.reduce((acc, item) => acc + item.quantity, 0),
    subtotal: subtotal(nextItems),
  }
}

export function optimisticUpdateCartItem(
  current: Cart | undefined,
  productId: string,
  quantity: number,
): Cart | undefined {
  if (!current) {
    return current
  }

  const items = current.items
    .filter((item) => (item.productId === productId ? quantity > 0 : true))
    .map((item) =>
      item.productId === productId
        ? {
            ...item,
            quantity,
            lineTotal: lineTotal(item.unitPrice, quantity),
          }
        : { ...item },
    )

  return {
    ...current,
    items,
    totalItems: items.reduce((acc, item) => acc + item.quantity, 0),
    subtotal: subtotal(items),
  }
}

export function optimisticRemoveCartItem(current: Cart | undefined, productId: string): Cart | undefined {
  if (!current) {
    return current
  }

  const items = current.items.filter((item) => item.productId !== productId).map((item) => ({ ...item }))

  return {
    ...current,
    items,
    totalItems: items.reduce((acc, item) => acc + item.quantity, 0),
    subtotal: subtotal(items),
  }
}
