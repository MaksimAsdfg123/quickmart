package com.quickmart.test.shared.kafka.data

import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.domain.enums.PaymentStatus
import com.quickmart.events.OrderEventType
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

object KafkaTestDataFactory {
    fun defaultCheckoutOptions(): CheckoutFixtureOptions = CheckoutFixtureOptions()

    fun emptyCartOptions(): CheckoutFixtureOptions = CheckoutFixtureOptions(withCartItems = false)

    fun expensiveCheckoutOptions(): CheckoutFixtureOptions =
        CheckoutFixtureOptions(
            unitPrice = BigDecimal("60000.00"),
            quantity = 1,
        )

    fun createdEventSpec(
        eventId: UUID = UUID.randomUUID(),
        occurredAt: Instant = Instant.parse("2026-03-31T10:00:00Z"),
        paymentMethod: PaymentMethod = PaymentMethod.CARD,
        paymentStatus: PaymentStatus = PaymentStatus.PAID,
        total: BigDecimal,
    ): ManualKafkaEventSpec =
        ManualKafkaEventSpec(
            eventId = eventId,
            eventType = OrderEventType.CREATED,
            occurredAt = occurredAt,
            previousStatus = null,
            currentStatus = OrderStatus.CREATED,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            total = total,
        )

    fun statusChangedEventSpec(
        eventId: UUID = UUID.randomUUID(),
        occurredAt: Instant = Instant.parse("2026-03-31T10:05:00Z"),
        previousStatus: OrderStatus = OrderStatus.CREATED,
        currentStatus: OrderStatus = OrderStatus.CONFIRMED,
        paymentMethod: PaymentMethod = PaymentMethod.CARD,
        paymentStatus: PaymentStatus = PaymentStatus.PAID,
        total: BigDecimal,
    ): ManualKafkaEventSpec =
        ManualKafkaEventSpec(
            eventId = eventId,
            eventType = OrderEventType.STATUS_CHANGED,
            occurredAt = occurredAt,
            previousStatus = previousStatus,
            currentStatus = currentStatus,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            total = total,
        )
}
