package com.quickmart.mapper

import com.quickmart.domain.entity.DeliverySlot
import com.quickmart.dto.admin.DeliverySlotResponse
import org.springframework.stereotype.Component

@Component
class DeliverySlotMapper {
    fun toResponse(slot: DeliverySlot): DeliverySlotResponse =
        DeliverySlotResponse(
            id = slot.id!!,
            slotDate = slot.slotDate,
            startTime = slot.startTime,
            endTime = slot.endTime,
            orderLimit = slot.orderLimit,
            active = slot.active,
        )
}
