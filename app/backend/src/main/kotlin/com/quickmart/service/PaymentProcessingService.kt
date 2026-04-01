package com.quickmart.service

import com.quickmart.client.payment.MockOnlinePaymentAuthorizationCommand
import com.quickmart.client.payment.MockOnlinePaymentAuthorizationResult
import com.quickmart.client.payment.MockOnlinePaymentFailureType
import com.quickmart.client.payment.MockOnlinePaymentGateway
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.domain.enums.PaymentStatus
import com.quickmart.exception.BusinessException
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class PaymentProcessingService(
    private val mockOnlinePaymentGateway: MockOnlinePaymentGateway,
) {
    fun resolvePayment(
        userId: UUID,
        method: PaymentMethod,
        amount: BigDecimal,
    ): PaymentProcessingResult =
        when (method) {
            PaymentMethod.CASH -> PaymentProcessingResult(status = PaymentStatus.PENDING)
            PaymentMethod.CARD -> PaymentProcessingResult(status = PaymentStatus.PAID)
            PaymentMethod.MOCK_ONLINE -> authorizeMockOnline(userId, amount)
        }

    private fun authorizeMockOnline(
        userId: UUID,
        amount: BigDecimal,
    ): PaymentProcessingResult =
        when (
            val outcome =
                mockOnlinePaymentGateway.authorize(
                    MockOnlinePaymentAuthorizationCommand(
                        customerId = userId,
                        amount = amount,
                    ),
                )
        ) {
            is MockOnlinePaymentAuthorizationResult.Approved ->
                PaymentProcessingResult(
                    status = PaymentStatus.PAID,
                    externalReference = outcome.paymentReference,
                )

            is MockOnlinePaymentAuthorizationResult.Rejected ->
                throw BusinessException(
                    message = "Оплата отклонена провайдером: ${outcome.providerMessage}",
                    status = 409,
                )

            is MockOnlinePaymentAuthorizationResult.TechnicalFailure ->
                throw BusinessException(
                    message = resolveFailureMessage(outcome.type),
                    status = resolveFailureStatus(outcome.type),
                )
        }

    private fun resolveFailureMessage(type: MockOnlinePaymentFailureType): String =
        when (type) {
            MockOnlinePaymentFailureType.TIMEOUT -> "Платежный провайдер не ответил вовремя"
            MockOnlinePaymentFailureType.INVALID_RESPONSE -> "Платежный провайдер вернул некорректный ответ"
            MockOnlinePaymentFailureType.SERVER_ERROR,
            MockOnlinePaymentFailureType.NETWORK_ERROR,
            -> "Платежный провайдер временно недоступен"
        }

    private fun resolveFailureStatus(type: MockOnlinePaymentFailureType): Int =
        when (type) {
            MockOnlinePaymentFailureType.INVALID_RESPONSE -> 502
            MockOnlinePaymentFailureType.TIMEOUT,
            MockOnlinePaymentFailureType.SERVER_ERROR,
            MockOnlinePaymentFailureType.NETWORK_ERROR,
            -> 503
        }
}

data class PaymentProcessingResult(
    val status: PaymentStatus,
    val externalReference: String? = null,
)
