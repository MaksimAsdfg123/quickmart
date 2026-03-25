package com.quickmart.repository

import com.quickmart.domain.entity.Order
import com.quickmart.domain.enums.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface OrderRepositoryCustom {
    fun searchForAdmin(
        statuses: Collection<OrderStatus>?,
        query: String?,
        pageable: Pageable,
    ): Page<Order>
}
