package com.quickmart.client.payment

import java.math.BigDecimal
import java.util.UUID

interface MockOnlinePaymentGateway {
    fun authorize(command: MockOnlinePaymentAuthorizationCommand): MockOnlinePaymentAuthorizationResult
}

data class MockOnlinePaymentAuthorizationCommand(
    val customerId: UUID,
    val amount: BigDecimal,
    val currency: String = "RUB",
    val requestId: UUID = UUID.randomUUID(),
)

sealed interface MockOnlinePaymentAuthorizationResult {
    data class Approved(
        val paymentReference: String,
    ) : MockOnlinePaymentAuthorizationResult

    data class Rejected(
        val providerCode: String,
        val providerMessage: String,
    ) : MockOnlinePaymentAuthorizationResult

    data class TechnicalFailure(
        val type: MockOnlinePaymentFailureType,
        val details: String,
    ) : MockOnlinePaymentAuthorizationResult
}

enum class MockOnlinePaymentFailureType {
    SERVER_ERROR,
    TIMEOUT,
    INVALID_RESPONSE,
    NETWORK_ERROR,
}
