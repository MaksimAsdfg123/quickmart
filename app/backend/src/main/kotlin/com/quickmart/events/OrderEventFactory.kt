package com.quickmart.events

import com.quickmart.domain.entity.Order
import com.quickmart.domain.enums.OrderStatus
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class OrderEventFactory {
    fun created(order: Order): OrderLifecycleIntegrationEvent = build(order, OrderEventType.CREATED, null)

    fun cancelled(
        order: Order,
        previousStatus: OrderStatus,
    ): OrderLifecycleIntegrationEvent = build(order, OrderEventType.CANCELLED, previousStatus)

    fun statusChanged(
        order: Order,
        previousStatus: OrderStatus,
    ): OrderLifecycleIntegrationEvent = build(order, OrderEventType.STATUS_CHANGED, previousStatus)

    private fun build(
        order: Order,
        eventType: OrderEventType,
        previousStatus: OrderStatus?,
    ): OrderLifecycleIntegrationEvent =
        OrderLifecycleIntegrationEvent(
            eventId = UUID.randomUUID(),
            eventType = eventType.code,
            aggregateId = requireNotNull(order.id) { "Order id must be present before publishing an event" },
            occurredAt = Instant.now(),
            payloadVersion = 1,
            payload =
                OrderEventPayload(
                    orderId = requireNotNull(order.id),
                    userId = requireNotNull(order.user.id),
                    previousStatus = previousStatus,
                    currentStatus = order.status,
                    paymentMethod = order.payment?.method,
                    paymentStatus = order.payment?.status,
                    total = order.total,
                    itemCount = order.items.sumOf { it.quantity },
                    items =
                        order.items.map { item ->
                            OrderEventItemPayload(
                                productId = requireNotNull(item.product.id),
                                productName = item.productName,
                                quantity = item.quantity,
                            )
                        },
                ),
        )
}
