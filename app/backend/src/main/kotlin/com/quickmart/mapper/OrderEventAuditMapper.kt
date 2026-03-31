package com.quickmart.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.quickmart.domain.entity.OrderEventAudit
import com.quickmart.dto.admin.OrderEventAuditResponse
import org.springframework.stereotype.Component

@Component
class OrderEventAuditMapper(
    private val objectMapper: ObjectMapper,
) {
    fun toResponse(entity: OrderEventAudit): OrderEventAuditResponse =
        OrderEventAuditResponse(
            eventId = requireNotNull(entity.eventId),
            eventType = entity.eventType,
            aggregateId = requireNotNull(entity.orderId),
            occurredAt = entity.occurredAt,
            payloadVersion = entity.payloadVersion,
            payload = objectMapper.readTree(entity.payloadJson),
        )
}
