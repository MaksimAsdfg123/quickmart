package com.quickmart.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "order_event_audit")
class OrderEventAudit : BaseEntity() {
    @Column(name = "event_id", nullable = false, unique = true, updatable = false)
    var eventId: UUID? = null

    @Column(name = "order_id", nullable = false, updatable = false)
    var orderId: UUID? = null

    @Column(name = "event_type", nullable = false, updatable = false)
    lateinit var eventType: String

    @Column(name = "payload_version", nullable = false, updatable = false)
    var payloadVersion: Int = 1

    @Column(name = "occurred_at", nullable = false, updatable = false)
    var occurredAt: Instant = Instant.now()

    @Column(name = "payload_json", nullable = false, updatable = false, columnDefinition = "TEXT")
    lateinit var payloadJson: String
}
