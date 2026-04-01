package com.quickmart.test.suites.kafka.config

import com.quickmart.domain.enums.OrderStatus
import com.quickmart.test.shared.kafka.assertion.KafkaAssertions
import com.quickmart.test.shared.kafka.foundation.BaseKafkaDisabledSuite
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Owner
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Epic("Quickmart Backend Tests")
@Feature("Kafka Integration")
@Owner("qa-automation")
@Tag("kafka")
@DisplayName("Kafka: disabled mode")
class OrderKafkaDisabledTest : BaseKafkaDisabledSuite() {
    @Test
    @Story("Configuration")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("KAFKA-CONFIG-001 Kafka disabled не ломает checkout/status flow и не создает audit trail")
    fun shouldKeepBusinessFlowWorkingWhenKafkaIsDisabled() {
        val result = kafkaOrderScenario.checkoutCashOrder(prefix = "config-disabled")

        val confirmedOrder = kafkaOrderScenario.confirmOrder(result.order.id)
        val cancelledOrder = kafkaOrderScenario.cancelAsCustomer(result.fixture.user.id!!, result.order.id)

        assertThat(confirmedOrder.status).isEqualTo(OrderStatus.CONFIRMED)
        assertThat(cancelledOrder.status).isEqualTo(OrderStatus.CANCELLED)
        KafkaAssertions.assertAuditTrailEmpty(kafkaOrderScenario.currentAuditTrail(result.order.id))
    }
}
