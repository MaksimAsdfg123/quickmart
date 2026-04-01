package com.quickmart.client.payment.model

data class MockOnlinePaymentAuthorizeResponse(
    val decision: String,
    val paymentReference: String?,
)
