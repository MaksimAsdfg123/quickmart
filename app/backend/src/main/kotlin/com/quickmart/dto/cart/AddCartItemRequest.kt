package com.quickmart.dto.cart

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(description = "Представляет полезную нагрузку запроса на добавление позиции в корзину.")
data class AddCartItemRequest(
    @field:NotNull
    @field:Schema(description = "Идентифицирует товар, подлежащий добавлению. Формат: UUID.", example = "40000000-0000-0000-0000-000000000001")
    val productId: UUID,
    @field:Min(1)
    @field:Schema(description = "Указывает количество для добавления. Минимальное значение — 1.", example = "2")
    val quantity: Int,
)
