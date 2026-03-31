package com.quickmart.kafka

import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(properties = ["app.kafka.enabled=false"])
@ActiveProfiles("test")
class OrderKafkaDisabledIntegrationTest : BaseOrderKafkaIntegrationTest() {
    @Test
    fun `checkout still works when kafka is disabled`() {
        val fixture = createCheckoutFixture("disabled")
        val order = checkout(fixture, PaymentMethod.CARD)

        assertThat(order.id).isNotNull
        assertThat(currentAudits(order.id)).isEmpty()
    }

    @Test
    fun `status and cancellation flow still work when kafka is disabled`() {
        val fixture = createCheckoutFixture("disabled-follow-up")
        val order = checkout(fixture, PaymentMethod.CASH)

        val confirmedOrder = orderService.updateStatus(order.id, OrderStatus.CONFIRMED)
        val cancelledOrder = orderService.cancelMyOrder(fixture.user.id!!, order.id)

        assertThat(confirmedOrder.status).isEqualTo(OrderStatus.CONFIRMED)
        assertThat(cancelledOrder.status).isEqualTo(OrderStatus.CANCELLED)
        assertThat(currentAudits(order.id)).isEmpty()
    }
}
