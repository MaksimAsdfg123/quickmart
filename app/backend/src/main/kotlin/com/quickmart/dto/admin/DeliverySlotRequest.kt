package com.quickmart.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalTime

@Schema(description = "Представляет полезную нагрузку запроса на создание или обновление слота доставки.")
data class DeliverySlotRequest(
    @field:NotNull
    @field:Schema(description = "Указывает дату слота доставки. Формат: `yyyy-MM-dd`.", example = "2026-03-27")
    val slotDate: LocalDate,
    @field:NotNull
    @field:Schema(description = "Указывает время начала слота доставки. Формат: `HH:mm:ss`.", example = "10:00:00")
    val startTime: LocalTime,
    @field:NotNull
    @field:Schema(description = "Указывает время окончания слота доставки. Формат: `HH:mm:ss`.", example = "12:00:00")
    val endTime: LocalTime,
    @field:Min(1)
    @field:Schema(description = "Указывает максимальное количество заказов, которое может быть назначено слоту. Минимальное значение — 1.", example = "30")
    val orderLimit: Int,
    @field:Schema(description = "Указывает, доступен ли слот для назначения при оформлении заказа.", example = "true")
    val active: Boolean = true,
)
