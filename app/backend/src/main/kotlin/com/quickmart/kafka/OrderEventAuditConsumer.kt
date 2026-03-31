package com.quickmart.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.quickmart.events.OrderLifecycleIntegrationEvent
import com.quickmart.service.OrderEventAuditService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Lazy
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Lazy(false)
@Component
@ConditionalOnProperty(prefix = "app.kafka", name = ["enabled"], havingValue = "true")
class OrderEventAuditConsumer(
    private val objectMapper: ObjectMapper,
    private val orderEventAuditService: OrderEventAuditService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${app.kafka.topic:quickmart.order-events}"],
        groupId = "\${app.kafka.consumer-group:quickmart-order-audit}",
    )
    fun consume(message: String) {
        val event = objectMapper.readValue(message, OrderLifecycleIntegrationEvent::class.java)
        orderEventAuditService.record(event)
        logger.info("Processed Kafka order event {} for order {}", event.eventType, event.aggregateId)
    }
}
