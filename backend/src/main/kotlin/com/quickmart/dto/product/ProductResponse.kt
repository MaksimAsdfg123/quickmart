package com.quickmart.dto.product

import java.math.BigDecimal
import java.util.UUID

data class ProductResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val categoryId: UUID,
    val categoryName: String,
    val imageUrl: String?,
    val active: Boolean,
    val availableQuantity: Int,
)
