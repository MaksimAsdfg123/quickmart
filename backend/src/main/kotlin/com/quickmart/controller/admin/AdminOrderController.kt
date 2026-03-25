package com.quickmart.controller.admin

import com.quickmart.dto.PageResponse
import com.quickmart.dto.order.OrderResponse
import com.quickmart.dto.order.OrderSummaryResponse
import com.quickmart.dto.order.UpdateOrderStatusRequest
import com.quickmart.service.OrderService
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
@RequestMapping("/api/admin/orders")
class AdminOrderController(
    private val orderService: OrderService,
) {
    @GetMapping
    fun all(
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(200) size: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) query: String?,
    ): ResponseEntity<PageResponse<OrderSummaryResponse>> = ResponseEntity.ok(orderService.getAllOrders(page, size, status, query))

    @GetMapping("/{id}")
    fun details(
        @PathVariable id: UUID,
    ): ResponseEntity<OrderResponse> = ResponseEntity.ok(orderService.getOrderDetails(id))

    @PutMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateOrderStatusRequest,
    ): ResponseEntity<OrderResponse> = ResponseEntity.ok(orderService.updateStatus(id, request.status))
}
