package com.quickmart.test.shared.kafka.data

import com.quickmart.domain.entity.Address
import com.quickmart.domain.entity.DeliverySlot
import com.quickmart.domain.entity.Product
import com.quickmart.domain.entity.User
import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.domain.enums.PaymentStatus
import com.quickmart.dto.order.OrderResponse
import com.quickmart.events.OrderEventType
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CheckoutFixtureOptions(
    val unitPrice: BigDecimal = BigDecimal("499.00"),
    val quantity: Int = 2,
    val withCartItems: Boolean = true,
)

data class CheckoutFixture(
    val user: User,
    val address: Address,
    val deliverySlot: DeliverySlot,
    val product: Product,
    val quantity: Int,
    val unitPrice: BigDecimal,
)

data class CheckoutScenarioResult(
    val fixture: CheckoutFixture,
    val order: OrderResponse,
)

data class ManualKafkaEventSpec(
    val eventId: UUID,
    val eventType: OrderEventType,
    val occurredAt: Instant,
    val previousStatus: OrderStatus?,
    val currentStatus: OrderStatus,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val total: BigDecimal,
)
