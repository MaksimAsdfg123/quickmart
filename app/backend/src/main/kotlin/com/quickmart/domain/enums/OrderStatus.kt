package com.quickmart.domain.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Определяет текущее состояние жизненного цикла заказа.",
    example = "CREATED",
)
enum class OrderStatus {
    CREATED,
    CONFIRMED,
    ASSEMBLING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
}
