package com.quickmart.test.suites.kafka.failure

import com.quickmart.domain.enums.OrderStatus
import com.quickmart.test.shared.kafka.assertion.KafkaAssertions
import com.quickmart.test.shared.kafka.data.KafkaTestDataFactory
import com.quickmart.test.shared.kafka.foundation.BaseKafkaEnabledSuite
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Owner
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Epic("Quickmart Backend Tests")
@Feature("Kafka Integration")
@Owner("qa-automation")
@Tag("kafka")
@DisplayName("Kafka: rollback and negative scenarios")
class OrderKafkaFailureTest : BaseKafkaEnabledSuite() {
    @Test
    @Story("Transactional")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("KAFKA-TX-001 Empty cart не публикует Kafka event")
    fun shouldNotPublishEventForEmptyCartCheckout() {
        val orderCountBefore = kafkaOrderScenario.orderCount()
        val auditCountBefore = kafkaOrderScenario.auditCount()

        val throwable = catchThrowable {
            kafkaOrderScenario.checkoutCardOrder(
                prefix = "failure-empty-cart-checkout",
                options = KafkaTestDataFactory.emptyCartOptions(),
            )
        }

        KafkaAssertions.assertBusinessException(throwable, "Корзина пуста")
        KafkaAssertions.assertCountsUnchanged(
            orderCountBefore = orderCountBefore,
            orderCountAfter = kafkaOrderScenario.orderCount(),
            auditCountBefore = auditCountBefore,
            auditCountAfter = kafkaOrderScenario.auditCount(),
        )
    }

    @Test
    @Story("Transactional")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("KAFKA-TX-002 Failed MOCK_ONLINE payment не публикует событие")
    fun shouldNotPublishEventForFailedMockOnlinePayment() {
        val orderCountBefore = kafkaOrderScenario.orderCount()
        val auditCountBefore = kafkaOrderScenario.auditCount()

        val throwable = catchThrowable {
            kafkaOrderScenario.checkoutExpensiveMockOnlineOrder(prefix = "failure-online-payment")
        }

        KafkaAssertions.assertBusinessException(throwable, "Оплата не прошла")
        KafkaAssertions.assertCountsUnchanged(
            orderCountBefore = orderCountBefore,
            orderCountAfter = kafkaOrderScenario.orderCount(),
            auditCountBefore = auditCountBefore,
            auditCountAfter = kafkaOrderScenario.auditCount(),
        )
    }

    @Test
    @Story("Transactional")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("KAFKA-TX-003 Invalid status transition не публикует follow-up event")
    fun shouldNotPublishFollowUpEventForInvalidTransition() {
        val result = kafkaOrderScenario.checkoutCardOrder(prefix = "failure-invalid-transition")

        kafkaOrderScenario.awaitAuditTrail(result.order.id, 1)

        val throwable = catchThrowable {
            kafkaOrderScenario.updateStatus(result.order.id, OrderStatus.DELIVERED)
        }

        KafkaAssertions.assertBusinessException(throwable, "Недопустимый переход статуса заказа")
        KafkaAssertions.assertAuditTrailHasOnlyCreatedEvent(kafkaOrderScenario.currentAuditTrail(result.order.id))
    }
}
