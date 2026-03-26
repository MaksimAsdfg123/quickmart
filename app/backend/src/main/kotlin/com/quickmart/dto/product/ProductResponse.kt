package com.quickmart.dto.product

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID

@Schema(description = "Представляет ресурс товара.")
data class ProductResponse(
    @field:Schema(description = "Идентифицирует товар. Формат: UUID.", example = "40000000-0000-0000-0000-000000000001")
    val id: UUID,
    @field:Schema(description = "Содержит наименование товара.", example = "Bananas")
    val name: String,
    @field:Schema(description = "Содержит описание товара.", example = "Fresh bananas, 1 kg")
    val description: String?,
    @field:Schema(description = "Содержит цену единицы товара.", example = "129.00")
    val price: BigDecimal,
    @field:Schema(description = "Идентифицирует назначенную категорию. Формат: UUID.", example = "30000000-0000-0000-0000-000000000001")
    val categoryId: UUID,
    @field:Schema(description = "Содержит наименование назначенной категории.", example = "Vegetables and Fruits")
    val categoryName: String,
    @field:Schema(description = "Содержит URL изображения товара.", example = "https://cdn.example.com/products/bananas.png")
    val imageUrl: String?,
    @field:Schema(description = "Указывает, активен ли товар.", example = "true")
    val active: Boolean,
    @field:Schema(description = "Указывает текущее доступное количество товара на складе.", example = "50")
    val availableQuantity: Int,
)
