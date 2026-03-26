package com.quickmart.controller.admin

import com.quickmart.dto.admin.DeliverySlotRequest
import com.quickmart.dto.admin.DeliverySlotResponse
import com.quickmart.service.DeliverySlotService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/admin/delivery-slots")
class AdminDeliverySlotController(
    private val deliverySlotService: DeliverySlotService,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: DeliverySlotRequest,
    ): ResponseEntity<DeliverySlotResponse> = ResponseEntity.status(HttpStatus.CREATED).body(deliverySlotService.create(request))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: DeliverySlotRequest,
    ): ResponseEntity<DeliverySlotResponse> = ResponseEntity.ok(deliverySlotService.update(id, request))
}
