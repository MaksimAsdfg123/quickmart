package com.quickmart.dto.cart

import java.math.BigDecimal
import java.util.UUID

data class CartResponse(
    val id: UUID,
    val items: List<CartItemResponse>,
    val totalItems: Int,
    val subtotal: BigDecimal,
)

data class CartItemResponse(
    val id: UUID,
    val productId: UUID,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal,
    val availableQuantity: Int,
)
