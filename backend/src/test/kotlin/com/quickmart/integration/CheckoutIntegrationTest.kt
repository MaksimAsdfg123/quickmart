package com.quickmart.integration

import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.dto.cart.AddCartItemRequest
import com.quickmart.dto.order.CheckoutRequest
import com.quickmart.exception.BusinessException
import com.quickmart.exception.NotFoundException
import com.quickmart.repository.InventoryStockRepository
import com.quickmart.service.CartService
import com.quickmart.service.CheckoutService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class CheckoutIntegrationTest : IntegrationTestBase() {
    @Autowired
    private lateinit var cartService: CartService

    @Autowired
    private lateinit var checkoutService: CheckoutService

    @Autowired
    private lateinit var inventoryStockRepository: InventoryStockRepository

    private val customerId = UUID.fromString("00000000-0000-0000-0000-000000000002")
    private val addressId = UUID.fromString("20000000-0000-0000-0000-000000000001")
    private val foreignAddressId = UUID.fromString("20000000-0000-0000-0000-000000000002")
    private val slotId = UUID.fromString("70000000-0000-0000-0000-000000000001")
    private val productId = UUID.fromString("40000000-0000-0000-0000-000000000001")

    @Test
    fun `checkout should create order, clear cart and decrease stock`() {
        cartService.clear(customerId)

        val beforeStock = inventoryStockRepository.findByProductId(productId)!!.availableQuantity

        cartService.addItem(customerId, AddCartItemRequest(productId, 3))

        val order =
            checkoutService.checkout(
                customerId,
                CheckoutRequest(
                    addressId = addressId,
                    deliverySlotId = slotId,
                    promoCode = "WELCOME100",
                    paymentMethod = PaymentMethod.CARD,
                ),
            )

        val afterStock = inventoryStockRepository.findByProductId(productId)!!.availableQuantity
        val cart = cartService.getCart(customerId)

        assertTrue(order.items.isNotEmpty())
        assertEquals(beforeStock - 3, afterStock)
        assertEquals(0, cart.items.size)
    }

    @Test
    fun `checkout should fail for empty cart`() {
        cartService.clear(customerId)

        assertThrows(BusinessException::class.java) {
            checkoutService.checkout(
                customerId,
                CheckoutRequest(
                    addressId = addressId,
                    deliverySlotId = slotId,
                    promoCode = null,
                    paymentMethod = PaymentMethod.CARD,
                ),
            )
        }
    }

    @Test
    fun `checkout should fail if address does not belong to user`() {
        cartService.clear(customerId)
        cartService.addItem(customerId, AddCartItemRequest(productId, 1))

        assertThrows(NotFoundException::class.java) {
            checkoutService.checkout(
                customerId,
                CheckoutRequest(
                    addressId = foreignAddressId,
                    deliverySlotId = slotId,
                    promoCode = null,
                    paymentMethod = PaymentMethod.CARD,
                ),
            )
        }
    }

    @Test
    fun `checkout should fail for invalid promo and keep stock unchanged`() {
        cartService.clear(customerId)
        cartService.addItem(customerId, AddCartItemRequest(productId, 2))

        val beforeStock = inventoryStockRepository.findByProductId(productId)!!.availableQuantity

        assertThrows(BusinessException::class.java) {
            checkoutService.checkout(
                customerId,
                CheckoutRequest(
                    addressId = addressId,
                    deliverySlotId = slotId,
                    promoCode = "INVALID_PROMO",
                    paymentMethod = PaymentMethod.CARD,
                ),
            )
        }

        val afterStock = inventoryStockRepository.findByProductId(productId)!!.availableQuantity
        val cart = cartService.getCart(customerId)
        assertEquals(beforeStock, afterStock)
        assertEquals(1, cart.items.size)
        assertEquals(2, cart.items.first().quantity)
    }

    @Test
    fun `adding to cart more than stock should fail`() {
        cartService.clear(customerId)
        val available = inventoryStockRepository.findByProductId(productId)!!.availableQuantity

        assertThrows(BusinessException::class.java) {
            cartService.addItem(customerId, AddCartItemRequest(productId, available + 1))
        }
    }
}
