package com.quickmart.events

import java.time.Instant
import java.util.UUID

data class OrderLifecycleIntegrationEvent(
    val eventId: UUID,
    val eventType: String,
    val aggregateId: UUID,
    val occurredAt: Instant,
    val payloadVersion: Int,
    val payload: OrderEventPayload,
)
