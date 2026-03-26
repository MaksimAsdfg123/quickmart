package com.quickmart.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID

@Schema(description = "Представляет ресурс складского остатка.")
data class InventoryStockResponse(
    @field:Schema(description = "Идентифицирует запись складского остатка. Формат: UUID.", example = "50000000-0000-0000-0000-000000000001")
    val id: UUID,
    @field:Schema(description = "Идентифицирует товар. Формат: UUID.", example = "40000000-0000-0000-0000-000000000001")
    val productId: UUID,
    @field:Schema(description = "Содержит наименование товара.", example = "Bananas")
    val productName: String,
    @field:Schema(description = "Содержит наименование категории.", example = "Vegetables and Fruits")
    val categoryName: String,
    @field:Schema(description = "Содержит цену единицы товара.", example = "129.00")
    val price: BigDecimal,
    @field:Schema(description = "Указывает, активен ли товар.", example = "true")
    val productActive: Boolean,
    @field:Schema(description = "Указывает доступное количество.", example = "50")
    val availableQuantity: Int,
)
