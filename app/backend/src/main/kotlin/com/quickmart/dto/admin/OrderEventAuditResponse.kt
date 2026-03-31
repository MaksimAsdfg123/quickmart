package com.quickmart.dto.admin

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "Представляет сохраненное Kafka-событие жизненного цикла заказа.")
data class OrderEventAuditResponse(
    @field:Schema(description = "Идентифицирует конкретное событие. Формат: UUID.")
    val eventId: UUID,
    @field:Schema(description = "Определяет тип доменного события.", example = "order.created")
    val eventType: String,
    @field:Schema(description = "Идентифицирует агрегат заказа, к которому относится событие. Формат: UUID.")
    val aggregateId: UUID,
    @field:Schema(description = "Определяет момент возникновения события.", format = "date-time")
    val occurredAt: Instant,
    @field:Schema(description = "Определяет версию payload события.", example = "1")
    val payloadVersion: Int,
    @field:Schema(description = "Содержит сериализованный полезный payload доменного события.")
    val payload: JsonNode,
)
