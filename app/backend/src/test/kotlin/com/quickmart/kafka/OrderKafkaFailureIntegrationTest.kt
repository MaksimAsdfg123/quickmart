package com.quickmart.kafka

import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.exception.BusinessException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
class OrderKafkaFailureIntegrationTest : BaseOrderKafkaIntegrationTest() {
    @Test
    fun `empty cart checkout does not publish kafka event`() {
        val fixture = createCheckoutFixture("empty-cart", CheckoutFixtureOptions(withCartItems = false))
        val auditCountBefore = orderEventAuditRepository.count()
        val orderCountBefore = orderRepository.count()

        assertThatThrownBy { checkout(fixture, PaymentMethod.CARD) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage("Корзина пуста")

        assertThat(orderRepository.count()).isEqualTo(orderCountBefore)
        assertThat(orderEventAuditRepository.count()).isEqualTo(auditCountBefore)
    }

    @Test
    fun `failed online payment rolls back and does not publish kafka event`() {
        val fixture = createExpensiveCheckoutFixture("failed-online")
        val auditCountBefore = orderEventAuditRepository.count()
        val orderCountBefore = orderRepository.count()

        assertThatThrownBy { checkout(fixture, PaymentMethod.MOCK_ONLINE) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage("Оплата не прошла")

        assertThat(orderRepository.count()).isEqualTo(orderCountBefore)
        assertThat(orderEventAuditRepository.count()).isEqualTo(auditCountBefore)
    }

    @Test
    fun `invalid status transition does not publish follow up event`() {
        val fixture = createCheckoutFixture("invalid-transition")
        val order = checkout(fixture, PaymentMethod.CARD)
        awaitAudits(order.id, 1)

        assertThatThrownBy { orderService.updateStatus(order.id, OrderStatus.DELIVERED) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("Недопустимый переход статуса заказа")

        val audits = currentAudits(order.id)
        assertThat(audits).hasSize(1)
        assertThat(audits.single().eventType).isEqualTo("order.created")
    }
}
