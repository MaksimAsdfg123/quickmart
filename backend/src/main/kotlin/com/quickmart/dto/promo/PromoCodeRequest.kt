package com.quickmart.dto.promo

import com.quickmart.domain.enums.PromoType
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime

data class PromoCodeRequest(
    @field:NotBlank
    @field:Size(max = 50)
    val code: String,
    @field:NotNull
    val type: PromoType,
    @field:NotNull
    @field:DecimalMin("0.01")
    val value: BigDecimal,
    @field:PositiveOrZero
    val minOrderAmount: BigDecimal = BigDecimal.ZERO,
    val active: Boolean = true,
    val validFrom: LocalDateTime?,
    val validTo: LocalDateTime?,
    @field:Positive
    val usageLimit: Int?,
)
