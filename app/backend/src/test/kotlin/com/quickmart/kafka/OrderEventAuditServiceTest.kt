package com.quickmart.kafka

import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.domain.enums.PaymentStatus
import com.quickmart.events.OrderEventItemPayload
import com.quickmart.events.OrderEventPayload
import com.quickmart.events.OrderEventType
import com.quickmart.events.OrderLifecycleIntegrationEvent
import com.quickmart.exception.NotFoundException
import com.quickmart.service.OrderEventAuditService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.util.UUID

@SpringBootTest(properties = ["app.kafka.enabled=false"])
@ActiveProfiles("test")
class OrderEventAuditServiceTest : BaseOrderKafkaIntegrationTest() {
    @Autowired
    private lateinit var orderEventAuditService: OrderEventAuditService

    @Test
    fun `record ignores duplicate event ids`() {
        val fixture = createCheckoutFixture("duplicate-event")
        val order = checkout(fixture, PaymentMethod.CARD)
        val event =
            manualEvent(
                fixture = fixture,
                orderId = order.id,
                eventId = UUID.randomUUID(),
                eventType = OrderEventType.CREATED,
                occurredAt = Instant.parse("2026-03-31T10:00:00Z"),
                previousStatus = null,
                currentStatus = OrderStatus.CREATED,
                paymentMethod = PaymentMethod.CARD,
                paymentStatus = PaymentStatus.PAID,
                total = order.total,
            )

        orderEventAuditService.record(event)
        orderEventAuditService.record(event)

        val audits = currentAudits(order.id)
        assertThat(audits).hasSize(1)
        assertThat(audits.single().eventId).isEqualTo(event.eventId)
    }

    @Test
    fun `get by order id returns chronological mapped payload`() {
        val fixture = createCheckoutFixture("audit-history")
        val order = checkout(fixture, PaymentMethod.CARD)
        val laterEvent =
            manualEvent(
                fixture = fixture,
                orderId = order.id,
                eventId = UUID.randomUUID(),
                eventType = OrderEventType.STATUS_CHANGED,
                occurredAt = Instant.parse("2026-03-31T10:05:00Z"),
                previousStatus = OrderStatus.CREATED,
                currentStatus = OrderStatus.CONFIRMED,
                paymentMethod = PaymentMethod.CARD,
                paymentStatus = PaymentStatus.PAID,
                total = order.total,
            )
        val earlierEvent =
            manualEvent(
                fixture = fixture,
                orderId = order.id,
                eventId = UUID.randomUUID(),
                eventType = OrderEventType.CREATED,
                occurredAt = Instant.parse("2026-03-31T10:00:00Z"),
                previousStatus = null,
                currentStatus = OrderStatus.CREATED,
                paymentMethod = PaymentMethod.CARD,
                paymentStatus = PaymentStatus.PAID,
                total = order.total,
            )

        orderEventAuditService.record(laterEvent)
        orderEventAuditService.record(earlierEvent)

        val history = orderEventAuditService.getByOrderId(order.id)

        assertThat(history.map { it.eventType })
            .containsExactly("order.created", "order.status_changed")
        assertThat(history.first().aggregateId).isEqualTo(order.id)
        assertThat(history.first().payloadVersion).isEqualTo(1)
        assertThat(history.first().payload.path("currentStatus").asText()).isEqualTo(OrderStatus.CREATED.name)
        assertThat(history.first().payload.path("items").get(0).path("productId").asText())
            .isEqualTo(fixture.product.id!!.toString())
        assertThat(history.last().payload.path("previousStatus").asText()).isEqualTo(OrderStatus.CREATED.name)
        assertThat(history.last().payload.path("currentStatus").asText()).isEqualTo(OrderStatus.CONFIRMED.name)
        assertThat(history.last().payload.path("itemCount").asInt()).isEqualTo(fixture.quantity)
    }

    @Test
    fun `get by order id throws when order is missing`() {
        assertThatThrownBy { orderEventAuditService.getByOrderId(UUID.randomUUID()) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Заказ не найден")
    }

    private fun manualEvent(
        fixture: CheckoutFixture,
        orderId: UUID,
        eventId: UUID,
        eventType: OrderEventType,
        occurredAt: Instant,
        previousStatus: OrderStatus?,
        currentStatus: OrderStatus,
        paymentMethod: PaymentMethod,
        paymentStatus: PaymentStatus,
        total: java.math.BigDecimal,
    ): OrderLifecycleIntegrationEvent =
        OrderLifecycleIntegrationEvent(
            eventId = eventId,
            eventType = eventType.code,
            aggregateId = orderId,
            occurredAt = occurredAt,
            payloadVersion = 1,
            payload =
                OrderEventPayload(
                    orderId = orderId,
                    userId = fixture.user.id!!,
                    previousStatus = previousStatus,
                    currentStatus = currentStatus,
                    paymentMethod = paymentMethod,
                    paymentStatus = paymentStatus,
                    total = total,
                    itemCount = fixture.quantity,
                    items =
                        listOf(
                            OrderEventItemPayload(
                                productId = fixture.product.id!!,
                                productName = fixture.product.name,
                                quantity = fixture.quantity,
                            ),
                        ),
                ),
        )
}
