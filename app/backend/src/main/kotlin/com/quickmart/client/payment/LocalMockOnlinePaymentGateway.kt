package com.quickmart.client.payment

import java.math.BigDecimal

class LocalMockOnlinePaymentGateway : MockOnlinePaymentGateway {
    private val approvalLimit = BigDecimal("50000.00")

    override fun authorize(command: MockOnlinePaymentAuthorizationCommand): MockOnlinePaymentAuthorizationResult =
        if (command.amount > approvalLimit) {
            MockOnlinePaymentAuthorizationResult.Rejected(
                providerCode = "SANDBOX_LIMIT_EXCEEDED",
                providerMessage = "Sandbox provider rejected payment above 50000.00 RUB",
            )
        } else {
            MockOnlinePaymentAuthorizationResult.Approved(paymentReference = "MOCK-${command.requestId}")
        }
}
