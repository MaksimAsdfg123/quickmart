package com.quickmart.dto.promo

import com.quickmart.domain.enums.PromoType
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Schema(description = "Представляет ресурс промокода.")
data class PromoCodeResponse(
    @field:Schema(description = "Идентифицирует промокод. Формат: UUID.", example = "60000000-0000-0000-0000-000000000002")
    val id: UUID,
    @field:Schema(description = "Содержит идентификатор промокода.", example = "SAVE10")
    val code: String,
    @field:Schema(description = "Содержит тип скидки промокода.", example = "PERCENT")
    val type: PromoType,
    @field:Schema(description = "Содержит значение промокода.", example = "10.00")
    val value: BigDecimal,
    @field:Schema(description = "Содержит минимальную сумму заказа, необходимую для применения.", example = "1000.00")
    val minOrderAmount: BigDecimal,
    @field:Schema(description = "Указывает, активен ли промокод.", example = "true")
    val active: Boolean,
    @field:Schema(description = "Содержит момент начала действия.", example = "2026-03-20T00:00:00")
    val validFrom: LocalDateTime?,
    @field:Schema(description = "Содержит момент окончания действия.", example = "2026-05-31T23:59:59")
    val validTo: LocalDateTime?,
    @field:Schema(description = "Содержит максимальное количество успешных применений.", example = "1000")
    val usageLimit: Int?,
    @field:Schema(description = "Содержит текущее количество успешных применений.", example = "15")
    val usedCount: Int,
)
