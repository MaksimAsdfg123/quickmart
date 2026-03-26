package com.quickmart.dto.admin

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class DeliverySlotResponse(
    val id: UUID,
    val slotDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val orderLimit: Int,
    val active: Boolean,
)
