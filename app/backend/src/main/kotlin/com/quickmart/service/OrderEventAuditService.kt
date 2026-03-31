package com.quickmart.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.quickmart.domain.entity.OrderEventAudit
import com.quickmart.dto.admin.OrderEventAuditResponse
import com.quickmart.events.OrderLifecycleIntegrationEvent
import com.quickmart.exception.NotFoundException
import com.quickmart.mapper.OrderEventAuditMapper
import com.quickmart.repository.OrderEventAuditRepository
import com.quickmart.repository.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class OrderEventAuditService(
    private val orderRepository: OrderRepository,
    private val orderEventAuditRepository: OrderEventAuditRepository,
    private val orderEventAuditMapper: OrderEventAuditMapper,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getByOrderId(orderId: UUID): List<OrderEventAuditResponse> {
        if (!orderRepository.existsById(orderId)) {
            throw NotFoundException("Заказ не найден")
        }

        return orderEventAuditRepository
            .findAllByOrderIdOrderByOccurredAtAscCreatedAtAsc(orderId)
            .map(orderEventAuditMapper::toResponse)
    }

    @Transactional(noRollbackFor = [DataIntegrityViolationException::class])
    fun record(event: OrderLifecycleIntegrationEvent) {
        if (orderEventAuditRepository.existsByEventId(event.eventId)) {
            logger.info("Skipping duplicate Kafka order event {}", event.eventId)
            return
        }

        try {
            orderEventAuditRepository.saveAndFlush(
                OrderEventAudit().apply {
                    eventId = event.eventId
                    orderId = event.aggregateId
                    eventType = event.eventType
                    payloadVersion = event.payloadVersion
                    occurredAt = event.occurredAt
                    payloadJson = objectMapper.writeValueAsString(event.payload)
                },
            )
        } catch (_: DataIntegrityViolationException) {
            logger.info("Skipping duplicate Kafka order event {}", event.eventId)
        }
    }
}
