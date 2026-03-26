package com.quickmart.controller

import com.quickmart.dto.admin.DeliverySlotResponse
import com.quickmart.service.DeliverySlotService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/delivery-slots")
class DeliverySlotController(
    private val deliverySlotService: DeliverySlotService,
) {
    @GetMapping
    fun getSlots(): ResponseEntity<List<DeliverySlotResponse>> = ResponseEntity.ok(deliverySlotService.getUpcomingSlots())
}
