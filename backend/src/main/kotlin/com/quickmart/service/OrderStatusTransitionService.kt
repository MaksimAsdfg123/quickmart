package com.quickmart.service

import com.quickmart.domain.enums.OrderStatus
import com.quickmart.exception.BusinessException
import org.springframework.stereotype.Service

@Service
class OrderStatusTransitionService {
    private val allowedTransitions: Map<OrderStatus, Set<OrderStatus>> =
        mapOf(
            OrderStatus.CREATED to setOf(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED to setOf(OrderStatus.ASSEMBLING, OrderStatus.CANCELLED),
            OrderStatus.ASSEMBLING to setOf(OrderStatus.OUT_FOR_DELIVERY),
            OrderStatus.OUT_FOR_DELIVERY to setOf(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED to emptySet(),
            OrderStatus.CANCELLED to emptySet(),
        )

    fun validateTransition(
        current: OrderStatus,
        target: OrderStatus,
    ) {
        if (current == target) {
            return
        }

        val allowed = allowedTransitions[current].orEmpty()
        if (target !in allowed) {
            throw BusinessException("Недопустимый переход статуса заказа: $current -> $target")
        }
    }

    fun validateCustomerCancellation(current: OrderStatus) {
        if (current != OrderStatus.CREATED && current != OrderStatus.CONFIRMED) {
            throw BusinessException("Заказ нельзя отменить в статусе $current")
        }
    }
}
