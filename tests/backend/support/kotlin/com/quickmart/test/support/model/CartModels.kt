package com.quickmart.test.support.model

import java.math.BigDecimal
import java.util.UUID

data class CartResponseModel(
    val id: UUID,
    val items: List<CartItemModel>,
    val totalItems: Int,
    val subtotal: BigDecimal,
)

data class CartItemModel(
    val id: UUID,
    val productId: UUID,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal,
    val availableQuantity: Int,
)

