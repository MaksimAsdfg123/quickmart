package com.quickmart.dto.cart

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min

@Schema(description = "Представляет полезную нагрузку запроса на изменение количества существующей позиции корзины.")
data class UpdateCartItemRequest(
    @field:Min(0)
    @field:Schema(description = "Указывает целевое количество. Значение `0` удаляет позицию из корзины.", example = "3")
    val quantity: Int,
)
