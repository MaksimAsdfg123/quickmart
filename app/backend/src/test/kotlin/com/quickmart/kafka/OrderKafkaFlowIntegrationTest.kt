package com.quickmart.kafka

import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.domain.enums.PaymentStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    topics = ["quickmart.order-events"],
    brokerProperties = ["listeners=PLAINTEXT://localhost:0", "port=0"],
)
class OrderKafkaFlowIntegrationTest : BaseOrderKafkaIntegrationTest() {
    @Test
    fun `checkout publishes created event with complete payload`() {
        val fixture = createCheckoutFixture("created-payload")
        val order = checkout(fixture, PaymentMethod.CARD)

        val createdEvent = awaitAudits(order.id, 1).single()
        val payload = payloadOf(createdEvent)
        val firstItem = payload.path("items").get(0)

        assertThat(createdEvent.eventId).isNotNull
        assertThat(createdEvent.eventType).isEqualTo("order.created")
        assertThat(createdEvent.orderId).isEqualTo(order.id)
        assertThat(createdEvent.payloadVersion).isEqualTo(1)
        assertThat(createdEvent.occurredAt).isNotNull

        assertThat(payload.path("orderId").asText()).isEqualTo(order.id.toString())
        assertThat(payload.path("userId").asText()).isEqualTo(fixture.user.id!!.toString())
        assertThat(payload.path("previousStatus").isNull).isTrue()
        assertThat(payload.path("currentStatus").asText()).isEqualTo(OrderStatus.CREATED.name)
        assertThat(payload.path("paymentMethod").asText()).isEqualTo(PaymentMethod.CARD.name)
        assertThat(payload.path("paymentStatus").asText()).isEqualTo(PaymentStatus.PAID.name)
        assertThat(payload.path("itemCount").asInt()).isEqualTo(fixture.quantity)
        assertThat(payload.path("items").size()).isEqualTo(1)
        assertThat(firstItem.path("productId").asText()).isEqualTo(fixture.product.id!!.toString())
        assertThat(firstItem.path("productName").asText()).isEqualTo(fixture.product.name)
        assertThat(firstItem.path("quantity").asInt()).isEqualTo(fixture.quantity)
        assertThat(payload.path("total").decimalValue()).isEqualByComparingTo(order.total)
    }

    @Test
    fun `valid status transition publishes status changed and ordered history`() {
        val fixture = createCheckoutFixture("status-change")
        val order = checkout(fixture, PaymentMethod.CARD)

        orderService.updateStatus(order.id, OrderStatus.CONFIRMED)

        val audits = awaitAudits(order.id, 2)
        val statusPayload = payloadOf(audits.last())

        assertThat(audits.map { it.eventType })
            .containsExactly("order.created", "order.status_changed")
        assertThat(statusPayload.path("previousStatus").asText()).isEqualTo(OrderStatus.CREATED.name)
        assertThat(statusPayload.path("currentStatus").asText()).isEqualTo(OrderStatus.CONFIRMED.name)
        assertThat(statusPayload.path("paymentStatus").asText()).isEqualTo(PaymentStatus.PAID.name)
    }

    @Test
    fun `customer cancellation after confirmation publishes cancelled event`() {
        val fixture = createCheckoutFixture("customer-cancel")
        val order = checkout(fixture, PaymentMethod.CARD)

        orderService.updateStatus(order.id, OrderStatus.CONFIRMED)
        orderService.cancelMyOrder(fixture.user.id!!, order.id)

        val audits = awaitAudits(order.id, 3)
        val cancelledPayload = payloadOf(audits.last())

        assertThat(audits.map { it.eventType })
            .containsExactly("order.created", "order.status_changed", "order.cancelled")
        assertThat(cancelledPayload.path("previousStatus").asText()).isEqualTo(OrderStatus.CONFIRMED.name)
        assertThat(cancelledPayload.path("currentStatus").asText()).isEqualTo(OrderStatus.CANCELLED.name)
        assertThat(cancelledPayload.path("paymentStatus").asText()).isEqualTo(PaymentStatus.PAID.name)
    }

    @Test
    fun `admin cancellation via status update publishes cancelled event`() {
        val fixture = createCheckoutFixture("admin-cancel")
        val order = checkout(fixture, PaymentMethod.CASH)

        orderService.updateStatus(order.id, OrderStatus.CONFIRMED)
        orderService.updateStatus(order.id, OrderStatus.CANCELLED)

        val audits = awaitAudits(order.id, 3)
        val cancelledPayload = payloadOf(audits.last())

        assertThat(audits.map { it.eventType })
            .containsExactly("order.created", "order.status_changed", "order.cancelled")
        assertThat(cancelledPayload.path("previousStatus").asText()).isEqualTo(OrderStatus.CONFIRMED.name)
        assertThat(cancelledPayload.path("currentStatus").asText()).isEqualTo(OrderStatus.CANCELLED.name)
        assertThat(cancelledPayload.path("paymentStatus").asText()).isEqualTo(PaymentStatus.FAILED.name)
    }

    @Test
    fun `cash delivery publishes final status event with paid payment status`() {
        val fixture = createCheckoutFixture("cash-delivered")
        val order = checkout(fixture, PaymentMethod.CASH)

        orderService.updateStatus(order.id, OrderStatus.CONFIRMED)
        orderService.updateStatus(order.id, OrderStatus.ASSEMBLING)
        orderService.updateStatus(order.id, OrderStatus.OUT_FOR_DELIVERY)
        orderService.updateStatus(order.id, OrderStatus.DELIVERED)

        val audits = awaitAudits(order.id, 5)
        val deliveredPayload = payloadOf(audits.last())

        assertThat(audits.map { it.eventType })
            .containsExactly(
                "order.created",
                "order.status_changed",
                "order.status_changed",
                "order.status_changed",
                "order.status_changed",
            )
        assertThat(deliveredPayload.path("previousStatus").asText()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY.name)
        assertThat(deliveredPayload.path("currentStatus").asText()).isEqualTo(OrderStatus.DELIVERED.name)
        assertThat(deliveredPayload.path("paymentMethod").asText()).isEqualTo(PaymentMethod.CASH.name)
        assertThat(deliveredPayload.path("paymentStatus").asText()).isEqualTo(PaymentStatus.PAID.name)
    }
}
