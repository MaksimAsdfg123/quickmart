package com.quickmart.test.suites.kafka.flow

import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.domain.enums.PaymentStatus
import com.quickmart.test.shared.kafka.assertion.KafkaAssertions
import com.quickmart.test.shared.kafka.foundation.BaseKafkaEnabledSuite
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Owner
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Epic("Quickmart Backend Tests")
@Feature("Kafka Integration")
@Owner("qa-automation")
@Tag("kafka")
@DisplayName("Kafka: order flow")
class OrderKafkaFlowTest : BaseKafkaEnabledSuite() {
    @Test
    @Story("Flow")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("KAFKA-FLOW-001 Checkout создает order.created и audit entry")
    fun shouldPublishCreatedEventOnCheckout() {
        val result = kafkaOrderScenario.checkoutCardOrder(prefix = "flow-created")

        val createdEvent = kafkaOrderScenario.awaitAuditTrail(result.order.id, 1).single()
        val payload = kafkaOrderScenario.payloadOf(createdEvent)

        KafkaAssertions.assertCreatedEvent(
            audit = createdEvent,
            payload = payload,
            order = result.order,
            fixture = result.fixture,
            paymentMethod = PaymentMethod.CARD,
            paymentStatus = PaymentStatus.PAID,
        )
        KafkaAssertions.assertItemPayloadMatchesFixture(payload, result.fixture)
    }

    @Test
    @Story("Payload")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("KAFKA-PAYLOAD-001 order.created содержит полный envelope")
    fun shouldExposeCompleteEnvelopeForCreatedEvent() {
        val result = kafkaOrderScenario.checkoutCardOrder(prefix = "payload-envelope")

        val createdEvent = kafkaOrderScenario.awaitAuditTrail(result.order.id, 1).single()
        val payload = kafkaOrderScenario.payloadOf(createdEvent)

        KafkaAssertions.assertCreatedEvent(
            audit = createdEvent,
            payload = payload,
            order = result.order,
            fixture = result.fixture,
            paymentMethod = PaymentMethod.CARD,
            paymentStatus = PaymentStatus.PAID,
        )
    }

    @Test
    @Story("Payload")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("KAFKA-PAYLOAD-002 payload order.created согласован с order snapshot")
    fun shouldExposeConsistentPayloadForCreatedEvent() {
        val result = kafkaOrderScenario.checkoutCardOrder(prefix = "payload-items")

        val createdEvent = kafkaOrderScenario.awaitAuditTrail(result.order.id, 1).single()
        val payload = kafkaOrderScenario.payloadOf(createdEvent)

        KafkaAssertions.assertItemPayloadMatchesFixture(payload, result.fixture)
        KafkaAssertions.assertCreatedEvent(
            audit = createdEvent,
            payload = payload,
            order = result.order,
            fixture = result.fixture,
            paymentMethod = PaymentMethod.CARD,
            paymentStatus = PaymentStatus.PAID,
        )
    }

    @Test
    @Story("Flow")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("KAFKA-FLOW-002 CREATED -> CONFIRMED создает order.status_changed")
    fun shouldPublishStatusChangedForValidTransition() {
        val result = kafkaOrderScenario.checkoutCardOrder(prefix = "flow-confirmed")

        kafkaOrderScenario.confirmOrder(result.order.id)

        val audits = kafkaOrderScenario.awaitAuditTrail(result.order.id, 2)
        val statusPayload = kafkaOrderScenario.payloadOf(audits.last())

        KafkaAssertions.assertOrderedAuditTrail(
            audits = audits,
            expectedTypes = listOf("order.created", "order.status_changed"),
        )
        KafkaAssertions.assertStatusChangedPayload(
            payload = statusPayload,
            previousStatus = OrderStatus.CREATED,
            currentStatus = OrderStatus.CONFIRMED,
            paymentStatus = PaymentStatus.PAID,
        )
    }

    @Test
    @Story("Flow")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("KAFKA-FLOW-003 Customer cancel после valid transition создает order.cancelled")
    fun shouldPublishCancelledEventForCustomerCancellation() {
        val result = kafkaOrderScenario.checkoutCardOrder(prefix = "flow-customer-cancel")

        kafkaOrderScenario.confirmOrder(result.order.id)
        kafkaOrderScenario.cancelAsCustomer(result.fixture.user.id!!, result.order.id)

        val audits = kafkaOrderScenario.awaitAuditTrail(result.order.id, 3)
        val cancelledPayload = kafkaOrderScenario.payloadOf(audits.last())

        KafkaAssertions.assertOrderedAuditTrail(
            audits = audits,
            expectedTypes = listOf("order.created", "order.status_changed", "order.cancelled"),
        )
        KafkaAssertions.assertCancelledPayload(
            payload = cancelledPayload,
            previousStatus = OrderStatus.CONFIRMED,
            paymentStatus = PaymentStatus.PAID,
        )
    }

    @Test
    @Story("Flow")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("KAFKA-FLOW-004 Admin cancel через updateStatus(CANCELLED) создает order.cancelled")
    fun shouldPublishCancelledEventForAdminCancellation() {
        val result = kafkaOrderScenario.checkoutCashOrder(prefix = "flow-admin-cancel")

        kafkaOrderScenario.confirmOrder(result.order.id)
        kafkaOrderScenario.cancelAsAdmin(result.order.id)

        val audits = kafkaOrderScenario.awaitAuditTrail(result.order.id, 3)
        val cancelledPayload = kafkaOrderScenario.payloadOf(audits.last())

        KafkaAssertions.assertOrderedAuditTrail(
            audits = audits,
            expectedTypes = listOf("order.created", "order.status_changed", "order.cancelled"),
        )
        KafkaAssertions.assertCancelledPayload(
            payload = cancelledPayload,
            previousStatus = OrderStatus.CONFIRMED,
            paymentStatus = PaymentStatus.FAILED,
        )
    }

    @Test
    @Story("Flow")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("KAFKA-FLOW-005 CASH order при DELIVERED публикует paymentStatus=PAID")
    fun shouldPublishPaidStatusWhenCashOrderIsDelivered() {
        val result = kafkaOrderScenario.checkoutCashOrder(prefix = "flow-cash-delivered")

        kafkaOrderScenario.deliverCashOrder(result.order.id)

        val audits = kafkaOrderScenario.awaitAuditTrail(result.order.id, 5)
        val deliveredPayload = kafkaOrderScenario.payloadOf(audits.last())

        KafkaAssertions.assertOrderedAuditTrail(
            audits = audits,
            expectedTypes =
                listOf(
                    "order.created",
                    "order.status_changed",
                    "order.status_changed",
                    "order.status_changed",
                    "order.status_changed",
                ),
        )
        KafkaAssertions.assertDeliveredCashPayload(deliveredPayload)
    }

    @Test
    @Story("Audit")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("KAFKA-AUDIT-001 История audit trail упорядочена по времени и событийному flow")
    fun shouldReturnOrderedAuditTrail() {
        val result = kafkaOrderScenario.checkoutCashOrder(prefix = "flow-audit-order")

        kafkaOrderScenario.deliverCashOrder(result.order.id)

        val audits = kafkaOrderScenario.awaitAuditTrail(result.order.id, 5)

        KafkaAssertions.assertOrderedAuditTrail(
            audits = audits,
            expectedTypes =
                listOf(
                    "order.created",
                    "order.status_changed",
                    "order.status_changed",
                    "order.status_changed",
                    "order.status_changed",
                ),
        )
    }
}
