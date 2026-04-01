package com.quickmart.test.shared.wiremock.payment.data

import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.dto.address.AddressRequest
import com.quickmart.dto.admin.DeliverySlotRequest
import com.quickmart.dto.auth.RegisterRequest
import com.quickmart.dto.cart.AddCartItemRequest
import com.quickmart.dto.category.CategoryRequest
import com.quickmart.dto.order.CheckoutRequest
import com.quickmart.dto.product.ProductRequest
import com.quickmart.test.shared.common.util.RandomDataUtils
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

object MockOnlinePaymentTestDataFactory {
    fun registerRequest(prefix: String): RegisterRequest =
        RegisterRequest(
            email = RandomDataUtils.uniqueEmail("wiremock.$prefix"),
            password = "password123",
            fullName = RandomDataUtils.uniqueName("WireMock Customer $prefix"),
        )

    fun categoryRequest(prefix: String): CategoryRequest =
        CategoryRequest(
            name = RandomDataUtils.uniqueName("WireMock Category $prefix"),
            description = "WireMock category for $prefix",
            active = true,
        )

    fun productRequest(
        categoryId: UUID,
        prefix: String,
        price: BigDecimal,
    ): ProductRequest =
        ProductRequest(
            name = RandomDataUtils.uniqueName("WireMock Product $prefix"),
            description = "WireMock product for $prefix",
            price = price,
            categoryId = categoryId,
            imageUrl = "https://cdn.quickmart.local/wiremock/$prefix.png",
            active = true,
        )

    fun addressRequest(prefix: String): AddressRequest =
        AddressRequest(
            label = "Home $prefix",
            city = "Yekaterinburg",
            street = "Lenina",
            house = "10",
            apartment = "25",
            entrance = null,
            floor = null,
            comment = "WireMock checkout address",
            isDefault = true,
        )

    fun deliverySlotRequest(daysFromNow: Long = 1): DeliverySlotRequest =
        DeliverySlotRequest(
            slotDate = LocalDate.now().plusDays(daysFromNow),
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(12, 0),
            orderLimit = 20,
            active = true,
        )

    fun addCartItemRequest(
        productId: UUID,
        quantity: Int,
    ): AddCartItemRequest =
        AddCartItemRequest(
            productId = productId,
            quantity = quantity,
        )

    fun checkoutRequest(
        addressId: UUID,
        deliverySlotId: UUID,
    ): CheckoutRequest =
        CheckoutRequest(
            addressId = addressId,
            deliverySlotId = deliverySlotId,
            promoCode = null,
            paymentMethod = PaymentMethod.MOCK_ONLINE,
        )
}
