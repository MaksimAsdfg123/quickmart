package com.quickmart.domain.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Определяет метод расчета скидки по промокоду.",
    example = "FIXED",
)
enum class PromoType {
    FIXED,
    PERCENT,
}
