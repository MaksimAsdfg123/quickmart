package com.quickmart.test.shared.kafka.assertion

import com.fasterxml.jackson.databind.JsonNode
import com.quickmart.domain.entity.OrderEventAudit
import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.domain.enums.PaymentStatus
import com.quickmart.dto.admin.OrderEventAuditResponse
import com.quickmart.dto.order.OrderResponse
import com.quickmart.exception.BusinessException
import com.quickmart.exception.NotFoundException
import com.quickmart.test.shared.foundation.allureStep
import com.quickmart.test.shared.kafka.data.CheckoutFixture
import org.assertj.core.api.Assertions.assertThat

object KafkaAssertions {
    fun assertCreatedEvent(
        audit: OrderEventAudit,
        payload: JsonNode,
        order: OrderResponse,
        fixture: CheckoutFixture,
        paymentMethod: PaymentMethod,
        paymentStatus: PaymentStatus,
    ) {
        allureStep("Проверить order.created envelope и payload") {
            assertThat(audit.eventId).isNotNull
            assertThat(audit.eventType).isEqualTo("order.created")
            assertThat(audit.orderId).isEqualTo(order.id)
            assertThat(audit.payloadVersion).isEqualTo(1)
            assertThat(audit.occurredAt).isNotNull

            assertThat(payload.path("orderId").asText()).isEqualTo(order.id.toString())
            assertThat(payload.path("userId").asText()).isEqualTo(fixture.user.id!!.toString())
            assertThat(payload.path("previousStatus").isNull).isTrue()
            assertThat(payload.path("currentStatus").asText()).isEqualTo(OrderStatus.CREATED.name)
            assertThat(payload.path("paymentMethod").asText()).isEqualTo(paymentMethod.name)
            assertThat(payload.path("paymentStatus").asText()).isEqualTo(paymentStatus.name)
            assertThat(payload.path("itemCount").asInt()).isEqualTo(fixture.quantity)
            assertThat(payload.path("items").size()).isEqualTo(1)
            assertThat(payload.path("total").decimalValue()).isEqualByComparingTo(order.total)
        }
    }

    fun assertItemPayloadMatchesFixture(
        payload: JsonNode,
        fixture: CheckoutFixture,
    ) {
        allureStep("Проверить товарные позиции в Kafka payload") {
            val firstItem = payload.path("items").get(0)
            assertThat(firstItem.path("productId").asText()).isEqualTo(fixture.product.id!!.toString())
            assertThat(firstItem.path("productName").asText()).isEqualTo(fixture.product.name)
            assertThat(firstItem.path("quantity").asInt()).isEqualTo(fixture.quantity)
            assertThat(payload.path("itemCount").asInt()).isEqualTo(fixture.quantity)
            assertThat(payload.path("items").size()).isEqualTo(1)
        }
    }

    fun assertOrderedAuditTrail(
        audits: List<OrderEventAudit>,
        expectedTypes: List<String>,
    ) {
        allureStep("Проверить хронологический порядок Kafka audit trail") {
            assertThat(audits.map { it.eventType }).containsExactlyElementsOf(expectedTypes)
            assertThat(audits.map { it.occurredAt }).isSorted()
        }
    }

    fun assertStatusChangedPayload(
        payload: JsonNode,
        previousStatus: OrderStatus,
        currentStatus: OrderStatus,
        paymentStatus: PaymentStatus,
    ) {
        allureStep("Проверить payload для order.status_changed") {
            assertThat(payload.path("previousStatus").asText()).isEqualTo(previousStatus.name)
            assertThat(payload.path("currentStatus").asText()).isEqualTo(currentStatus.name)
            assertThat(payload.path("paymentStatus").asText()).isEqualTo(paymentStatus.name)
        }
    }

    fun assertCancelledPayload(
        payload: JsonNode,
        previousStatus: OrderStatus,
        paymentStatus: PaymentStatus,
    ) {
        allureStep("Проверить payload для order.cancelled") {
            assertThat(payload.path("previousStatus").asText()).isEqualTo(previousStatus.name)
            assertThat(payload.path("currentStatus").asText()).isEqualTo(OrderStatus.CANCELLED.name)
            assertThat(payload.path("paymentStatus").asText()).isEqualTo(paymentStatus.name)
        }
    }

    fun assertDeliveredCashPayload(payload: JsonNode) {
        allureStep("Проверить финальный payload для cash delivery") {
            assertThat(payload.path("previousStatus").asText()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY.name)
            assertThat(payload.path("currentStatus").asText()).isEqualTo(OrderStatus.DELIVERED.name)
            assertThat(payload.path("paymentMethod").asText()).isEqualTo(PaymentMethod.CASH.name)
            assertThat(payload.path("paymentStatus").asText()).isEqualTo(PaymentStatus.PAID.name)
        }
    }

    fun assertBusinessException(
        throwable: Throwable?,
        expectedMessage: String,
    ) {
        allureStep("Проверить бизнес-ошибку $expectedMessage") {
            assertThat(throwable).isInstanceOf(BusinessException::class.java)
            assertThat(throwable).hasMessageContaining(expectedMessage)
        }
    }

    fun assertCountsUnchanged(
        orderCountBefore: Long,
        orderCountAfter: Long,
        auditCountBefore: Long,
        auditCountAfter: Long,
    ) {
        allureStep("Проверить отсутствие post-commit side effects") {
            assertThat(orderCountAfter).isEqualTo(orderCountBefore)
            assertThat(auditCountAfter).isEqualTo(auditCountBefore)
        }
    }

    fun assertAuditTrailHasOnlyCreatedEvent(audits: List<OrderEventAudit>) {
        allureStep("Проверить отсутствие follow-up Kafka event") {
            assertThat(audits).hasSize(1)
            assertThat(audits.single().eventType).isEqualTo("order.created")
        }
    }

    fun assertDuplicateEventStoredOnce(
        audits: List<OrderEventAudit>,
        expectedEventId: java.util.UUID,
    ) {
        allureStep("Проверить идемпотентность audit storage по eventId") {
            assertThat(audits).hasSize(1)
            assertThat(audits.single().eventId).isEqualTo(expectedEventId)
        }
    }

    fun assertHistoryResponse(
        history: List<OrderEventAuditResponse>,
        expectedOrderId: java.util.UUID,
        expectedEventTypes: List<String>,
    ) {
        allureStep("Проверить read model истории Kafka-событий") {
            assertThat(history.map { it.eventType }).containsExactlyElementsOf(expectedEventTypes)
            assertThat(history.map { it.occurredAt }).isSorted()
            assertThat(history.all { it.aggregateId == expectedOrderId }).isTrue()
            assertThat(history.map { it.payloadVersion }.distinct()).containsExactly(1)
        }
    }

    fun assertHistoryPayloadMatchesFixture(
        history: List<OrderEventAuditResponse>,
        fixture: CheckoutFixture,
    ) {
        allureStep("Проверить десериализованный payload из read model") {
            val createdPayload = history.first().payload
            val changedPayload = history.last().payload

            assertThat(createdPayload.path("currentStatus").asText()).isEqualTo(OrderStatus.CREATED.name)
            assertThat(createdPayload.path("items").get(0).path("productId").asText())
                .isEqualTo(fixture.product.id!!.toString())
            assertThat(createdPayload.path("itemCount").asInt()).isEqualTo(fixture.quantity)

            assertThat(changedPayload.path("previousStatus").asText()).isEqualTo(OrderStatus.CREATED.name)
            assertThat(changedPayload.path("currentStatus").asText()).isEqualTo(OrderStatus.CONFIRMED.name)
        }
    }

    fun assertNotFoundException(
        throwable: Throwable?,
        expectedMessage: String,
    ) {
        allureStep("Проверить NotFoundException для отсутствующего заказа") {
            assertThat(throwable).isInstanceOf(NotFoundException::class.java)
            assertThat(throwable).hasMessageContaining(expectedMessage)
        }
    }

    fun assertAuditTrailEmpty(audits: List<OrderEventAudit>) {
        allureStep("Проверить отсутствие audit trail при выключенной Kafka") {
            assertThat(audits).isEmpty()
        }
    }
}
