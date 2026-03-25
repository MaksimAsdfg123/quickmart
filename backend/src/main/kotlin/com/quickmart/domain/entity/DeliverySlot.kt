package com.quickmart.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "delivery_slots")
class DeliverySlot : BaseEntity() {
    @Column(name = "slot_date", nullable = false)
    var slotDate: LocalDate = LocalDate.now()

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime = LocalTime.of(9, 0)

    @Column(name = "end_time", nullable = false)
    var endTime: LocalTime = LocalTime.of(10, 0)

    @Column(name = "order_limit", nullable = false)
    var orderLimit: Int = 0

    @Column(name = "active", nullable = false)
    var active: Boolean = true
}
