package com.quickmart.events

import com.quickmart.domain.entity.Order
import com.quickmart.domain.enums.OrderStatus
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class OrderEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val orderEventFactory: OrderEventFactory,
) {
    fun publishCreated(order: Order) {
        applicationEventPublisher.publishEvent(orderEventFactory.created(order))
    }

    fun publishCancelled(
        order: Order,
        previousStatus: OrderStatus,
    ) {
        applicationEventPublisher.publishEvent(orderEventFactory.cancelled(order, previousStatus))
    }

    fun publishStatusChanged(
        order: Order,
        previousStatus: OrderStatus,
    ) {
        applicationEventPublisher.publishEvent(orderEventFactory.statusChanged(order, previousStatus))
    }
}
