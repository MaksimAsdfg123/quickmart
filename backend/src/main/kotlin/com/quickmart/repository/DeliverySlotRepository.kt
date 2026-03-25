package com.quickmart.repository

import com.quickmart.domain.entity.DeliverySlot
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.util.UUID

interface DeliverySlotRepository : JpaRepository<DeliverySlot, UUID> {
    fun findAllBySlotDateGreaterThanEqualAndActiveTrueOrderBySlotDateAscStartTimeAsc(date: LocalDate): List<DeliverySlot>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from DeliverySlot s where s.id = :id")
    fun findByIdForUpdate(id: UUID): DeliverySlot?
}
