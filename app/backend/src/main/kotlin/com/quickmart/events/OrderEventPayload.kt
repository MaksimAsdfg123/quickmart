package com.quickmart.events

import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.domain.enums.PaymentStatus
import java.math.BigDecimal
import java.util.UUID

data class OrderEventPayload(
    val orderId: UUID,
    val userId: UUID,
    val previousStatus: OrderStatus?,
    val currentStatus: OrderStatus,
    val paymentMethod: PaymentMethod?,
    val paymentStatus: PaymentStatus?,
    val total: BigDecimal,
    val itemCount: Int,
    val items: List<OrderEventItemPayload>,
)
