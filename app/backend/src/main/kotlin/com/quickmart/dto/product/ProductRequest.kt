package com.quickmart.dto.product

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.UUID

@Schema(description = "Представляет полезную нагрузку запроса на создание или обновление товара.")
data class ProductRequest(
    @field:NotBlank
    @field:Size(max = 200)
    @field:Schema(description = "Указывает наименование товара. Ограничения: максимальная длина — 200.", example = "Bananas")
    val name: String,
    @field:Size(max = 2000)
    @field:Schema(description = "Указывает описание товара. Ограничения: максимальная длина — 2000.", example = "Fresh bananas, 1 kg")
    val description: String?,
    @field:NotNull
    @field:DecimalMin("0.01")
    @field:Schema(description = "Указывает цену единицы товара. Формат: десятичное число с масштабом, определяемым слоем хранения. Минимальное значение — 0.01.", example = "129.00")
    val price: BigDecimal,
    @field:NotNull
    @field:Schema(description = "Идентифицирует категорию, назначенную товару. Формат: UUID.", example = "30000000-0000-0000-0000-000000000001")
    val categoryId: UUID,
    @field:Size(max = 500)
    @field:Schema(description = "Указывает URL изображения товара. Ограничения: максимальная длина — 500.", example = "https://cdn.example.com/products/bananas.png")
    val imageUrl: String?,
    @field:Schema(description = "Указывает, активен ли товар.", example = "true")
    val active: Boolean = true,
)
