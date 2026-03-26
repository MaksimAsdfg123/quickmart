package com.quickmart.domain.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Определяет статус обработки оплаты, связанный с заказом.",
    example = "PAID",
)
enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
}
