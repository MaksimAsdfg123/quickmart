package com.quickmart.dto.promo

import com.quickmart.domain.enums.PromoType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime

@Schema(description = "Представляет полезную нагрузку запроса на создание или обновление промокода.")
data class PromoCodeRequest(
    @field:NotBlank
    @field:Size(max = 50)
    @field:Schema(description = "Указывает идентификатор промокода. Ограничения: максимальная длина — 50. Сохраняется в верхнем регистре.", example = "SAVE10")
    val code: String,
    @field:NotNull
    @field:Schema(description = "Указывает тип скидки.", example = "PERCENT")
    val type: PromoType,
    @field:NotNull
    @field:DecimalMin("0.01")
    @field:Schema(description = "Указывает значение скидки. Для `FIXED` значение представляет абсолютную денежную скидку. Для `PERCENT` значение представляет процент.", example = "10.00")
    val value: BigDecimal,
    @field:PositiveOrZero
    @field:Schema(description = "Указывает минимальную сумму заказа, необходимую для применения промокода.", example = "1000.00")
    val minOrderAmount: BigDecimal = BigDecimal.ZERO,
    @field:Schema(description = "Указывает, активен ли промокод.", example = "true")
    val active: Boolean = true,
    @field:Schema(description = "Указывает момент начала действия. Формат: дата и время ISO 8601.", example = "2026-03-20T00:00:00")
    val validFrom: LocalDateTime?,
    @field:Schema(description = "Указывает момент окончания действия. Формат: дата и время ISO 8601.", example = "2026-05-31T23:59:59")
    val validTo: LocalDateTime?,
    @field:Positive
    @field:Schema(description = "Указывает максимальное количество успешных применений. Не передавайте поле для настройки промокода без ограничения использования.", example = "1000")
    val usageLimit: Int?,
)
