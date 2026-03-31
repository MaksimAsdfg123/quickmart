package com.quickmart.repository

import com.quickmart.domain.entity.OrderEventAudit
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrderEventAuditRepository : JpaRepository<OrderEventAudit, UUID> {
    fun existsByEventId(eventId: UUID): Boolean

    fun findAllByOrderIdOrderByOccurredAtAscCreatedAtAsc(orderId: UUID): List<OrderEventAudit>
}
