package com.quickmart.service

import com.quickmart.domain.entity.DeliverySlot
import com.quickmart.domain.enums.OrderStatus
import com.quickmart.dto.admin.DeliverySlotResponse
import com.quickmart.exception.BusinessException
import com.quickmart.exception.NotFoundException
import com.quickmart.mapper.DeliverySlotMapper
import com.quickmart.repository.DeliverySlotRepository
import com.quickmart.repository.OrderRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class DeliverySlotService(
    private val deliverySlotRepository: DeliverySlotRepository,
    private val orderRepository: OrderRepository,
    private val deliverySlotMapper: DeliverySlotMapper,
) {
    private val bookingStatuses = OrderStatus.entries.filter { it != OrderStatus.CANCELLED }

    fun getUpcomingSlots(): List<DeliverySlotResponse> =
        deliverySlotRepository
            .findAllBySlotDateGreaterThanEqualAndActiveTrueOrderBySlotDateAscStartTimeAsc(LocalDate.now())
            .map(deliverySlotMapper::toResponse)

    fun getByIdOrThrow(id: UUID): DeliverySlot =
        deliverySlotRepository.findById(id).orElseThrow { NotFoundException("Слот доставки не найден") }

    fun lockAndValidateForCheckout(slotId: UUID): DeliverySlot {
        val slot =
            deliverySlotRepository.findByIdForUpdate(slotId)
                ?: throw NotFoundException("Слот доставки не найден")

        if (!slot.active) {
            throw BusinessException("Слот доставки недоступен")
        }

        if (slot.slotDate.isBefore(LocalDate.now())) {
            throw BusinessException("Нельзя выбрать слот доставки в прошлом")
        }

        val ordersInSlot = orderRepository.countByDeliverySlotIdAndStatusIn(slot.id!!, bookingStatuses)
        if (ordersInSlot >= slot.orderLimit) {
            throw BusinessException("Лимит заказов для этого слота исчерпан")
        }

        return slot
    }
}
