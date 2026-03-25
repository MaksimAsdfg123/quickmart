package com.quickmart.dto.promo

import com.quickmart.domain.enums.PromoType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class PromoCodeResponse(
    val id: UUID,
    val code: String,
    val type: PromoType,
    val value: BigDecimal,
    val minOrderAmount: BigDecimal,
    val active: Boolean,
    val validFrom: LocalDateTime?,
    val validTo: LocalDateTime?,
    val usageLimit: Int?,
    val usedCount: Int,
)
