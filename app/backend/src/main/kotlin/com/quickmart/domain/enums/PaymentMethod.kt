package com.quickmart.domain.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Определяет способ оплаты, выбранный для расчета по заказу.",
    example = "CARD",
)
enum class PaymentMethod {
    CASH,
    CARD,
    MOCK_ONLINE,
}
