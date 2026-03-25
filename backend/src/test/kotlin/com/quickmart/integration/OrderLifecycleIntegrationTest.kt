package com.quickmart.integration

import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.dto.cart.AddCartItemRequest
import com.quickmart.dto.order.CheckoutRequest
import com.quickmart.exception.BusinessException
import com.quickmart.repository.InventoryStockRepository
import com.quickmart.service.CartService
import com.quickmart.service.CheckoutService
import com.quickmart.service.OrderService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class OrderLifecycleIntegrationTest : IntegrationTestBase() {
    @Autowired
    private lateinit var cartService: CartService

    @Autowired
    private lateinit var checkoutService: CheckoutService

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var inventoryStockRepository: InventoryStockRepository

    private val customerId = UUID.fromString("00000000-0000-0000-0000-000000000002")
    private val addressId = UUID.fromString("20000000-0000-0000-0000-000000000001")
    private val slotId = UUID.fromString("70000000-0000-0000-0000-000000000002")
    private val productId = UUID.fromString("40000000-0000-0000-0000-000000000002")

    @Test
    fun `customer can cancel created order and inventory returns`() {
        cartService.clear(customerId)
        val before = inventoryStockRepository.findByProductId(productId)!!.availableQuantity
        cartService.addItem(customerId, AddCartItemRequest(productId, 2))

        val createdOrder =
            checkoutService.checkout(
                customerId,
                CheckoutRequest(addressId, slotId, null, PaymentMethod.CASH),
            )

        val afterCheckout = inventoryStockRepository.findByProductId(productId)!!.availableQuantity
        assertEquals(before - 2, afterCheckout)

        val cancelled = orderService.cancelMyOrder(customerId, createdOrder.id)
        assertEquals(OrderStatus.CANCELLED, cancelled.status)

        val afterCancel = inventoryStockRepository.findByProductId(productId)!!.availableQuantity
        assertEquals(before, afterCancel)
    }

    @Test
    fun `customer cannot cancel after assembling`() {
        cartService.clear(customerId)
        cartService.addItem(customerId, AddCartItemRequest(productId, 1))

        val createdOrder =
            checkoutService.checkout(
                customerId,
                CheckoutRequest(addressId, slotId, null, PaymentMethod.CARD),
            )

        orderService.updateStatus(createdOrder.id, OrderStatus.CONFIRMED)
        orderService.updateStatus(createdOrder.id, OrderStatus.ASSEMBLING)

        assertThrows(BusinessException::class.java) {
            orderService.cancelMyOrder(customerId, createdOrder.id)
        }
    }

    @Test
    fun `invalid lifecycle transition should be rejected`() {
        cartService.clear(customerId)
        cartService.addItem(customerId, AddCartItemRequest(productId, 1))

        val createdOrder =
            checkoutService.checkout(
                customerId,
                CheckoutRequest(addressId, slotId, null, PaymentMethod.CARD),
            )

        assertThrows(BusinessException::class.java) {
            orderService.updateStatus(createdOrder.id, OrderStatus.DELIVERED)
        }
    }

    @Test
    fun `admin can load order summaries with payment and item counters`() {
        cartService.clear(customerId)
        cartService.addItem(customerId, AddCartItemRequest(productId, 2))

        checkoutService.checkout(
            customerId,
            CheckoutRequest(addressId, slotId, null, PaymentMethod.CARD),
        )

        val page = orderService.getAllOrders(page = 0, size = 10, status = "ACTIVE", query = null)

        assertFalse(page.content.isEmpty())
        val summary = page.content.first()
        assertEquals(PaymentMethod.CARD, summary.paymentMethod)
        assertEquals(2, summary.itemsCount)
    }
}
