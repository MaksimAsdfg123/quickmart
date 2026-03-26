package com.quickmart.controller

import com.quickmart.dto.PageResponse
import com.quickmart.dto.order.CheckoutRequest
import com.quickmart.dto.order.OrderResponse
import com.quickmart.dto.order.OrderSummaryResponse
import com.quickmart.security.AuthFacade
import com.quickmart.service.CheckoutService
import com.quickmart.service.OrderService
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val checkoutService: CheckoutService,
    private val orderService: OrderService,
    private val authFacade: AuthFacade,
) {
    @PostMapping("/checkout")
    fun checkout(
        authentication: Authentication,
        @Valid @RequestBody request: CheckoutRequest,
    ): ResponseEntity<OrderResponse> {
        val userId = authFacade.userId(authentication)
        return ResponseEntity.status(HttpStatus.CREATED).body(checkoutService.checkout(userId, request))
    }

    @GetMapping
    fun myOrders(
        authentication: Authentication,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "10") @Min(1) @Max(200) size: Int,
    ): ResponseEntity<PageResponse<OrderSummaryResponse>> {
        val userId = authFacade.userId(authentication)
        return ResponseEntity.ok(orderService.getMyOrders(userId, page, size))
    }

    @GetMapping("/{id}")
    fun myOrderDetails(
        authentication: Authentication,
        @PathVariable id: UUID,
    ): ResponseEntity<OrderResponse> {
        val userId = authFacade.userId(authentication)
        return ResponseEntity.ok(orderService.getMyOrderDetails(userId, id))
    }

    @PostMapping("/{id}/cancel")
    fun cancel(
        authentication: Authentication,
        @PathVariable id: UUID,
    ): ResponseEntity<OrderResponse> {
        val userId = authFacade.userId(authentication)
        return ResponseEntity.ok(orderService.cancelMyOrder(userId, id))
    }
}
