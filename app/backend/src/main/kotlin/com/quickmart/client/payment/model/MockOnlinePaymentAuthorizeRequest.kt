package com.quickmart.client.payment.model

import java.math.BigDecimal
import java.util.UUID

data class MockOnlinePaymentAuthorizeRequest(
    val customerId: UUID,
    val amount: BigDecimal,
    val currency: String,
    val paymentMethod: String,
)
