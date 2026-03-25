package com.quickmart.service

import com.quickmart.domain.enums.OrderStatus
import com.quickmart.exception.BusinessException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class OrderStatusTransitionServiceTest {
    private val service = OrderStatusTransitionService()

    @Test
    fun `should allow valid transitions`() {
        assertDoesNotThrow { service.validateTransition(OrderStatus.CREATED, OrderStatus.CONFIRMED) }
        assertDoesNotThrow { service.validateTransition(OrderStatus.CONFIRMED, OrderStatus.ASSEMBLING) }
        assertDoesNotThrow { service.validateTransition(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED) }
    }

    @Test
    fun `should reject invalid transitions`() {
        assertThrows(BusinessException::class.java) {
            service.validateTransition(OrderStatus.CREATED, OrderStatus.DELIVERED)
        }

        assertThrows(BusinessException::class.java) {
            service.validateTransition(OrderStatus.ASSEMBLING, OrderStatus.CANCELLED)
        }
    }

    @Test
    fun `customer cancellation should be allowed only for created and confirmed`() {
        assertDoesNotThrow { service.validateCustomerCancellation(OrderStatus.CREATED) }
        assertDoesNotThrow { service.validateCustomerCancellation(OrderStatus.CONFIRMED) }

        assertThrows(BusinessException::class.java) {
            service.validateCustomerCancellation(OrderStatus.ASSEMBLING)
        }
    }
}
