package com.quickmart.repository

import com.quickmart.domain.entity.Order
import com.quickmart.domain.enums.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface OrderRepository :
    JpaRepository<Order, UUID>,
    OrderRepositoryCustom {
    @EntityGraph(attributePaths = ["items", "items.product", "payment", "deliverySlot", "promoCode", "address"])
    override fun findById(id: UUID): Optional<Order>

    @Query("select o from Order o where o.user.id = :userId")
    fun findAllByUserId(
        @Param("userId") userId: UUID,
        pageable: Pageable,
    ): Page<Order>

    @EntityGraph(attributePaths = ["items", "payment", "deliverySlot"])
    @Query("select o from Order o")
    fun findAllOrders(pageable: Pageable): Page<Order>

    @Query("select count(o) from Order o where o.deliverySlot.id = :slotId and o.status in :statuses")
    fun countByDeliverySlotIdAndStatusIn(
        @Param("slotId") slotId: UUID,
        @Param("statuses") statuses: Collection<OrderStatus>,
    ): Long
}
