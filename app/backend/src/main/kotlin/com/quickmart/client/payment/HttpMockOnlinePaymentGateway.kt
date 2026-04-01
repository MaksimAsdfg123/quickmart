package com.quickmart.client.payment

import com.fasterxml.jackson.databind.ObjectMapper
import com.quickmart.client.payment.model.MockOnlinePaymentAuthorizeRequest
import com.quickmart.client.payment.model.MockOnlinePaymentAuthorizeResponse
import com.quickmart.client.payment.model.MockOnlinePaymentErrorResponse
import com.quickmart.config.MockOnlinePaymentProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets

class HttpMockOnlinePaymentGateway(
    @Qualifier("mockOnlinePaymentRestClient")
    private val restClient: RestClient,
    private val properties: MockOnlinePaymentProperties,
    private val objectMapper: ObjectMapper,
) : MockOnlinePaymentGateway {
    override fun authorize(command: MockOnlinePaymentAuthorizationCommand): MockOnlinePaymentAuthorizationResult {
        val requestBody =
            MockOnlinePaymentAuthorizeRequest(
                customerId = command.customerId,
                amount = command.amount,
                currency = command.currency,
                paymentMethod = "MOCK_ONLINE",
            )

        return try {
            restClient
                .post()
                .uri(AUTHORIZE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(API_KEY_HEADER, properties.apiKey)
                .header(REQUEST_ID_HEADER, command.requestId.toString())
                .body(requestBody)
                .exchange { _, response ->
                    val status = response.statusCode.value()
                    val body = response.body.use { inputStream -> String(inputStream.readAllBytes(), StandardCharsets.UTF_8) }

                    when {
                        status in 200..299 -> mapSuccessfulResponse(body)
                        status in 400..499 -> mapRejectedResponse(status, body)
                        else ->
                            MockOnlinePaymentAuthorizationResult.TechnicalFailure(
                                type = MockOnlinePaymentFailureType.SERVER_ERROR,
                                details = "Provider returned HTTP $status",
                            )
                    }
                }
        } catch (ex: ResourceAccessException) {
            val failureType =
                if (isTimeout(ex)) {
                    MockOnlinePaymentFailureType.TIMEOUT
                } else {
                    MockOnlinePaymentFailureType.NETWORK_ERROR
                }
            MockOnlinePaymentAuthorizationResult.TechnicalFailure(
                type = failureType,
                details = ex.message ?: "Network error while calling payment provider",
            )
        } catch (ex: RestClientException) {
            MockOnlinePaymentAuthorizationResult.TechnicalFailure(
                type = MockOnlinePaymentFailureType.NETWORK_ERROR,
                details = ex.message ?: "Unexpected RestClient error",
            )
        }
    }

    private fun mapSuccessfulResponse(body: String): MockOnlinePaymentAuthorizationResult {
        val parsed =
            parseBody<MockOnlinePaymentAuthorizeResponse>(body)
                ?: return invalidResponse("Provider returned unreadable success payload")

        if (parsed.decision != "APPROVED" || parsed.paymentReference.isNullOrBlank()) {
            return invalidResponse("Provider returned incomplete approval payload")
        }

        return MockOnlinePaymentAuthorizationResult.Approved(parsed.paymentReference)
    }

    private fun mapRejectedResponse(
        status: Int,
        body: String,
    ): MockOnlinePaymentAuthorizationResult {
        val parsed = parseBody<MockOnlinePaymentErrorResponse>(body)

        return MockOnlinePaymentAuthorizationResult.Rejected(
            providerCode = parsed?.code?.takeIf { it.isNotBlank() } ?: "HTTP_$status",
            providerMessage = parsed?.message?.takeIf { it.isNotBlank() } ?: "Payment rejected by provider",
        )
    }

    private inline fun <reified T> parseBody(body: String): T? = runCatching { objectMapper.readValue(body, T::class.java) }.getOrNull()

    private fun invalidResponse(details: String): MockOnlinePaymentAuthorizationResult.TechnicalFailure =
        MockOnlinePaymentAuthorizationResult.TechnicalFailure(
            type = MockOnlinePaymentFailureType.INVALID_RESPONSE,
            details = details,
        )

    private fun isTimeout(ex: ResourceAccessException): Boolean {
        var current: Throwable? = ex
        while (current != null) {
            if (current is SocketTimeoutException) {
                return true
            }
            current = current.cause
        }
        return false
    }

    companion object {
        const val AUTHORIZE_PATH = "/api/v1/payments/authorize"
        const val API_KEY_HEADER = "X-Api-Key"
        const val REQUEST_ID_HEADER = "X-Request-Id"
    }
}
