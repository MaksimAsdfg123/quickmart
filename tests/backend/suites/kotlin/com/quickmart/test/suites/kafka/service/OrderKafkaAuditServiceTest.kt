package com.quickmart.test.suites.kafka.service

import com.quickmart.test.shared.kafka.assertion.KafkaAssertions
import com.quickmart.test.shared.kafka.data.KafkaTestDataFactory
import com.quickmart.test.shared.kafka.foundation.BaseKafkaDisabledSuite
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
import java.util.UUID

@Epic("Quickmart Backend Tests")
@Feature("Kafka Integration")
@Owner("qa-automation")
@Tag("kafka")
@DisplayName("Kafka: audit service")
class OrderKafkaAuditServiceTest : BaseKafkaDisabledSuite() {
    @Test
    @Story("Idempotency")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("KAFKA-IDEMP-001 Duplicate processing по одному eventId не создает вторую запись")
    fun shouldIgnoreDuplicateEventIds() {
        val result = kafkaOrderScenario.checkoutCardOrder(prefix = "service-duplicate")
        val createdEvent =
            kafkaOrderScenario.buildManualEvent(
                fixture = result.fixture,
                orderId = result.order.id,
                spec = KafkaTestDataFactory.createdEventSpec(total = result.order.total),
            )

        kafkaOrderScenario.recordDuplicateEvent(createdEvent)

        KafkaAssertions.assertDuplicateEventStoredOnce(
            audits = kafkaOrderScenario.currentAuditTrail(result.order.id),
            expectedEventId = createdEvent.eventId,
        )
    }

    @Test
    @Story("Audit")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("KAFKA-AUDIT-002 getByOrderId корректно маппит сохраненный payload")
    fun shouldReturnChronologicalMappedPayload() {
        val result = kafkaOrderScenario.checkoutCardOrder(prefix = "service-history")
        val laterEvent =
            kafkaOrderScenario.buildManualEvent(
                fixture = result.fixture,
                orderId = result.order.id,
                spec = KafkaTestDataFactory.statusChangedEventSpec(total = result.order.total),
            )
        val earlierEvent =
            kafkaOrderScenario.buildManualEvent(
                fixture = result.fixture,
                orderId = result.order.id,
                spec = KafkaTestDataFactory.createdEventSpec(total = result.order.total),
            )

        kafkaOrderScenario.recordEvent(laterEvent)
        kafkaOrderScenario.recordEvent(earlierEvent)

        val history = kafkaOrderScenario.readAuditHistory(result.order.id)

        KafkaAssertions.assertHistoryResponse(
            history = history,
            expectedOrderId = result.order.id,
            expectedEventTypes = listOf("order.created", "order.status_changed"),
        )
        KafkaAssertions.assertHistoryPayloadMatchesFixture(history, result.fixture)
    }

    @Test
    @Story("Audit")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("KAFKA-AUDIT-003 getByOrderId для missing order дает NotFoundException")
    fun shouldFailForMissingOrder() {
        val throwable = catchThrowable {
            kafkaOrderScenario.readAuditHistory(UUID.randomUUID())
        }

        KafkaAssertions.assertNotFoundException(throwable, "Заказ не найден")
    }
}
