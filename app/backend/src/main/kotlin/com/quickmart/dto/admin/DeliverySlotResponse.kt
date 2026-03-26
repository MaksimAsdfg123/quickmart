package com.quickmart.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Schema(description = "Представляет ресурс слота доставки.")
data class DeliverySlotResponse(
    @field:Schema(description = "Идентифицирует слот доставки. Формат: UUID.", example = "70000000-0000-0000-0000-000000000001")
    val id: UUID,
    @field:Schema(description = "Содержит дату доставки.", example = "2026-03-27")
    val slotDate: LocalDate,
    @field:Schema(description = "Содержит время начала доставки.", example = "10:00:00")
    val startTime: LocalTime,
    @field:Schema(description = "Содержит время окончания доставки.", example = "12:00:00")
    val endTime: LocalTime,
    @field:Schema(description = "Содержит лимит заказов для слота доставки.", example = "30")
    val orderLimit: Int,
    @field:Schema(description = "Указывает, активен ли слот доставки.", example = "true")
    val active: Boolean,
)
