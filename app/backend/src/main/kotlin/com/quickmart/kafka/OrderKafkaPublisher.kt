package com.quickmart.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.quickmart.config.AppKafkaProperties
import com.quickmart.events.OrderLifecycleIntegrationEvent
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = ["enabled"], havingValue = "true")
class OrderKafkaPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val kafkaProperties: AppKafkaProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun publish(event: OrderLifecycleIntegrationEvent) {
        val payload = objectMapper.writeValueAsString(event)
        kafkaTemplate
            .send(kafkaProperties.topic, event.aggregateId.toString(), payload)
            .whenComplete { _, exception ->
                if (exception == null) {
                    logger.info("Published Kafka order event {} for order {}", event.eventType, event.aggregateId)
                } else {
                    logger.error("Failed to publish Kafka order event {} for order {}", event.eventType, event.aggregateId, exception)
                }
            }
    }
}
