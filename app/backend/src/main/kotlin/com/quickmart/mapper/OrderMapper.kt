package com.quickmart.mapper

import com.quickmart.domain.entity.Order
import com.quickmart.dto.order.OrderItemResponse
import com.quickmart.dto.order.OrderResponse
import com.quickmart.dto.order.OrderSummaryResponse
import org.springframework.stereotype.Component

@Component
class OrderMapper {
    fun toResponse(order: Order): OrderResponse =
        OrderResponse(
            id = order.id!!,
            status = order.status,
            addressSnapshot = order.addressSnapshot,
            deliveryDate = order.deliverySlot.slotDate,
            deliveryStartTime = order.deliverySlot.startTime,
            deliveryEndTime = order.deliverySlot.endTime,
            promoCode = order.promoCode?.code,
            subtotal = order.subtotal,
            discount = order.discount,
            deliveryFee = order.deliveryFee,
            total = order.total,
            items =
                order.items.map {
                    OrderItemResponse(
                        id = it.id!!,
                        productId = it.product.id!!,
                        productName = it.productName,
                        unitPrice = it.unitPrice,
                        quantity = it.quantity,
                        lineTotal = it.lineTotal,
                    )
                },
            paymentMethod = order.payment!!.method,
            paymentStatus = order.payment!!.status,
            createdAt = order.createdAt!!,
        )

    fun toSummary(order: Order): OrderSummaryResponse =
        OrderSummaryResponse(
            id = order.id!!,
            status = order.status,
            total = order.total,
            paymentMethod = order.payment!!.method,
            paymentStatus = order.payment!!.status,
            itemsCount = order.items.sumOf { it.quantity },
            deliveryDate = order.deliverySlot.slotDate,
            deliveryStartTime = order.deliverySlot.startTime,
            deliveryEndTime = order.deliverySlot.endTime,
            createdAt = order.createdAt!!,
        )
}
