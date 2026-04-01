package com.quickmart.test.shared.wiremock.payment.data

import java.math.BigDecimal
import java.util.UUID

data class MockOnlineCheckoutFixture(
    val userId: UUID,
    val addressId: UUID,
    val deliverySlotId: UUID,
    val productId: UUID,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val expectedSubtotal: BigDecimal,
    val expectedTotal: BigDecimal,
    val initialStock: Int,
) {
    val expectedRemainingStock: Int
        get() = initialStock - quantity
}
