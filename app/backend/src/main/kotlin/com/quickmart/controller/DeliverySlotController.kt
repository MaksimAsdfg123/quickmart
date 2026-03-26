package com.quickmart.controller

import com.quickmart.config.INTERNAL_SERVER_ERROR_RESPONSE_REF
import com.quickmart.config.TAG_DELIVERY_SLOTS
import com.quickmart.dto.admin.DeliverySlotResponse
import com.quickmart.service.DeliverySlotService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/delivery-slots")
@Tag(name = TAG_DELIVERY_SLOTS, description = "Операции публичного списка слотов доставки")
class DeliverySlotController(
    private val deliverySlotService: DeliverySlotService,
) {
    @GetMapping
    @Operation(
        summary = "Получить список слотов доставки",
        description = "Назначение: Возвращает коллекцию активных слотов доставки, дата которых не меньше текущей даты.\nАвторизация: Не требуется.\nОграничения: Возвращаются только текущие или будущие активные слоты.\nРезультат: Возвращает коллекцию слотов доставки, упорядоченную по дате и времени начала.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Коллекция слотов доставки возвращена."),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun getSlots(): ResponseEntity<List<DeliverySlotResponse>> = ResponseEntity.ok(deliverySlotService.getUpcomingSlots())
}
