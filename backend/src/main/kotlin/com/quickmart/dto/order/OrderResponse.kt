package com.quickmart.dto.order

import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.domain.enums.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val status: OrderStatus,
    val addressSnapshot: String,
    val deliveryDate: LocalDate,
    val deliveryStartTime: LocalTime,
    val deliveryEndTime: LocalTime,
    val promoCode: String?,
    val subtotal: BigDecimal,
    val discount: BigDecimal,
    val deliveryFee: BigDecimal,
    val total: BigDecimal,
    val items: List<OrderItemResponse>,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val createdAt: LocalDateTime,
)

data class OrderItemResponse(
    val id: UUID,
    val productId: UUID,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal,
)

data class OrderSummaryResponse(
    val id: UUID,
    val status: OrderStatus,
    val total: BigDecimal,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val itemsCount: Int,
    val deliveryDate: LocalDate,
    val deliveryStartTime: LocalTime,
    val deliveryEndTime: LocalTime,
    val createdAt: LocalDateTime,
)
