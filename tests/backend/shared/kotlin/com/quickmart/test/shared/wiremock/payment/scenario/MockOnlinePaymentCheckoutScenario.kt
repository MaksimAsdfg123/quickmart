package com.quickmart.test.shared.wiremock.payment.scenario

import com.quickmart.dto.order.OrderResponse
import com.quickmart.service.AddressService
import com.quickmart.service.AuthService
import com.quickmart.service.CartService
import com.quickmart.service.CategoryService
import com.quickmart.service.CheckoutService
import com.quickmart.service.DeliverySlotService
import com.quickmart.service.InventoryService
import com.quickmart.service.ProductService
import com.quickmart.test.shared.foundation.allureStep
import com.quickmart.test.shared.wiremock.payment.data.MockOnlineCheckoutFixture
import com.quickmart.test.shared.wiremock.payment.data.MockOnlinePaymentTestDataFactory
import java.math.BigDecimal

class MockOnlinePaymentCheckoutScenario(
    private val authService: AuthService,
    private val categoryService: CategoryService,
    private val productService: ProductService,
    private val inventoryService: InventoryService,
    private val addressService: AddressService,
    private val deliverySlotService: DeliverySlotService,
    private val cartService: CartService,
    private val checkoutService: CheckoutService,
) {
    private val freeDeliveryThreshold = BigDecimal("1500.00")
    private val baseDeliveryFee = BigDecimal("149.00")

    fun prepareFixture(
        prefix: String,
        unitPrice: BigDecimal = BigDecimal("599.00"),
        quantity: Int = 2,
        initialStock: Int = 20,
    ): MockOnlineCheckoutFixture =
        allureStep("Подготовить checkout fixture для WireMock-сценария: $prefix") {
            val authResponse = authService.register(MockOnlinePaymentTestDataFactory.registerRequest(prefix))
            val category = categoryService.create(MockOnlinePaymentTestDataFactory.categoryRequest(prefix))
            val product =
                productService.create(
                    MockOnlinePaymentTestDataFactory.productRequest(
                        categoryId = category.id,
                        prefix = prefix,
                        price = unitPrice,
                    ),
                )

            inventoryService.updateStock(product.id, initialStock)

            val address =
                addressService.create(
                    authResponse.user.id,
                    MockOnlinePaymentTestDataFactory.addressRequest(prefix),
                )
            val slot = deliverySlotService.create(MockOnlinePaymentTestDataFactory.deliverySlotRequest())

            cartService.addItem(
                authResponse.user.id,
                MockOnlinePaymentTestDataFactory.addCartItemRequest(product.id, quantity),
            )

            val subtotal = unitPrice.multiply(quantity.toBigDecimal())
            val deliveryFee = if (subtotal >= freeDeliveryThreshold) BigDecimal.ZERO else baseDeliveryFee

            MockOnlineCheckoutFixture(
                userId = authResponse.user.id,
                addressId = address.id,
                deliverySlotId = slot.id,
                productId = product.id,
                productName = product.name,
                quantity = quantity,
                unitPrice = unitPrice,
                expectedSubtotal = subtotal,
                expectedTotal = subtotal.add(deliveryFee),
                initialStock = initialStock,
            )
        }

    fun checkoutWithMockOnline(fixture: MockOnlineCheckoutFixture): OrderResponse =
        allureStep("Оформить заказ с MOCK_ONLINE для пользователя ${fixture.userId}") {
            checkoutService.checkout(
                fixture.userId,
                MockOnlinePaymentTestDataFactory.checkoutRequest(
                    addressId = fixture.addressId,
                    deliverySlotId = fixture.deliverySlotId,
                ),
            )
        }
}
