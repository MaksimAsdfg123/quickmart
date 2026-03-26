package com.quickmart.dto.admin

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalTime

data class DeliverySlotRequest(
    @field:NotNull
    val slotDate: LocalDate,
    @field:NotNull
    val startTime: LocalTime,
    @field:NotNull
    val endTime: LocalTime,
    @field:Min(1)
    val orderLimit: Int,
    val active: Boolean = true,
)
