package com.quickmart.dto.admin

import java.math.BigDecimal
import java.util.UUID

data class InventoryStockResponse(
    val id: UUID,
    val productId: UUID,
    val productName: String,
    val categoryName: String,
    val price: BigDecimal,
    val productActive: Boolean,
    val availableQuantity: Int,
)
