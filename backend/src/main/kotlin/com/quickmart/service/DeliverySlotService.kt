package com.quickmart.service

import com.quickmart.domain.entity.DeliverySlot
import com.quickmart.domain.enums.OrderStatus
import com.quickmart.dto.admin.DeliverySlotRequest
import com.quickmart.dto.admin.DeliverySlotResponse
import com.quickmart.exception.BusinessException
import com.quickmart.exception.NotFoundException
import com.quickmart.mapper.DeliverySlotMapper
import com.quickmart.repository.DeliverySlotRepository
import com.quickmart.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

    @Transactional
    fun create(request: DeliverySlotRequest): DeliverySlotResponse {
        val slot =
            DeliverySlot().apply {
                slotDate = request.slotDate
                startTime = request.startTime
                endTime = request.endTime
                orderLimit = request.orderLimit
                active = request.active
            }

        validateForAdmin(slot)
        return deliverySlotMapper.toResponse(deliverySlotRepository.save(slot))
    }

    @Transactional
    fun update(
        id: UUID,
        request: DeliverySlotRequest,
    ): DeliverySlotResponse {
        val slot = getByIdOrThrow(id)
        slot.slotDate = request.slotDate
        slot.startTime = request.startTime
        slot.endTime = request.endTime
        slot.orderLimit = request.orderLimit
        slot.active = request.active

        validateForAdmin(slot)
        return deliverySlotMapper.toResponse(deliverySlotRepository.save(slot))
    }

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

    private fun validateForAdmin(slot: DeliverySlot) {
        if (!slot.startTime.isBefore(slot.endTime)) {
            throw BusinessException("Время окончания слота должно быть позже времени начала")
        }

        if (slot.slotDate.isBefore(LocalDate.now())) {
            throw BusinessException("Нельзя создать слот доставки в прошлом")
        }
    }
}
