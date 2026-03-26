package com.quickmart.service

import com.quickmart.domain.entity.Order
import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.domain.enums.PaymentStatus
import com.quickmart.dto.PageResponse
import com.quickmart.dto.order.OrderResponse
import com.quickmart.dto.order.OrderSummaryResponse
import com.quickmart.exception.BusinessException
import com.quickmart.exception.NotFoundException
import com.quickmart.mapper.OrderMapper
import com.quickmart.repository.OrderRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderMapper: OrderMapper,
    private val orderStatusTransitionService: OrderStatusTransitionService,
    private val inventoryService: InventoryService,
) {
    @Transactional(readOnly = true)
    fun getMyOrders(
        userId: UUID,
        page: Int,
        size: Int,
    ): PageResponse<OrderSummaryResponse> {
        val normalizedPage = page.coerceAtLeast(0)
        val normalizedSize = size.coerceIn(1, 200)
        val pageable = PageRequest.of(normalizedPage, normalizedSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val orders = orderRepository.findAllByUserId(userId, pageable)
        return PageResponse.from(orders, orderMapper::toSummary)
    }

    @Transactional(readOnly = true)
    fun getMyOrderDetails(
        userId: UUID,
        orderId: UUID,
    ): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow { NotFoundException("Заказ не найден") }
        if (order.user.id != userId) {
            throw NotFoundException("Заказ не найден")
        }
        return orderMapper.toResponse(order)
    }

    @Transactional
    fun cancelMyOrder(
        userId: UUID,
        orderId: UUID,
    ): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow { NotFoundException("Заказ не найден") }
        if (order.user.id != userId) {
            throw NotFoundException("Заказ не найден")
        }

        orderStatusTransitionService.validateCustomerCancellation(order.status)
        cancelOrder(order)

        return orderMapper.toResponse(orderRepository.save(order))
    }

    @Transactional(readOnly = true)
    fun getAllOrders(
        page: Int,
        size: Int,
        status: String?,
        query: String?,
    ): PageResponse<OrderSummaryResponse> {
        val normalizedPage = page.coerceAtLeast(0)
        val normalizedSize = size.coerceIn(1, 200)
        val pageable = PageRequest.of(normalizedPage, normalizedSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val searchQuery = query?.trim()?.takeIf { it.isNotBlank() }
        val orders = orderRepository.searchForAdmin(resolveStatuses(status), searchQuery, pageable)
        return PageResponse.from(orders, orderMapper::toSummary)
    }

    @Transactional(readOnly = true)
    fun getOrderDetails(orderId: UUID): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow { NotFoundException("Заказ не найден") }
        return orderMapper.toResponse(order)
    }

    @Transactional
    fun updateStatus(
        orderId: UUID,
        targetStatus: OrderStatus,
    ): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow { NotFoundException("Заказ не найден") }

        orderStatusTransitionService.validateTransition(order.status, targetStatus)

        if (targetStatus == OrderStatus.CANCELLED) {
            cancelOrder(order)
        } else {
            order.status = targetStatus
            if (targetStatus == OrderStatus.DELIVERED && order.payment?.method == PaymentMethod.CASH) {
                order.payment?.status = PaymentStatus.PAID
            }
        }

        return orderMapper.toResponse(orderRepository.save(order))
    }

    private fun cancelOrder(order: Order) {
        if (order.status == OrderStatus.CANCELLED) {
            return
        }

        order.items.forEach { item ->
            inventoryService.increaseStockWithLock(item.product.id!!, item.quantity)
        }

        if (order.payment?.status == PaymentStatus.PENDING) {
            order.payment?.status = PaymentStatus.FAILED
        }

        order.status = OrderStatus.CANCELLED
    }

    private fun resolveStatuses(status: String?): Collection<OrderStatus>? {
        val normalized = status?.trim()?.uppercase()?.takeIf { it.isNotBlank() } ?: return null
        return when (normalized) {
            "ACTIVE" ->
                listOf(
                    OrderStatus.CREATED,
                    OrderStatus.CONFIRMED,
                    OrderStatus.ASSEMBLING,
                    OrderStatus.OUT_FOR_DELIVERY,
                )

            else ->
                listOf(
                    runCatching { OrderStatus.valueOf(normalized) }
                        .getOrElse { throw BusinessException("Неизвестный статус заказа") },
                )
        }
    }
}
