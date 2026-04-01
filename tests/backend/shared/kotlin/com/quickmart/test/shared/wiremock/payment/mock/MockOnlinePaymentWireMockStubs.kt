package com.quickmart.test.shared.wiremock.payment.mock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.quickmart.test.shared.foundation.allureStep
import java.math.BigDecimal
import java.util.UUID

object MockOnlinePaymentWireMockStubs {
    fun stubApproved(
        wireMockServer: WireMockServer,
        apiKey: String,
        customerId: UUID,
        amount: BigDecimal,
        paymentReference: String,
    ) {
        allureStep("Настроить WireMock stub для успешной авторизации платежа") {
            wireMockServer.stubFor(
                authorizationRequest(apiKey, customerId, amount)
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"decision":"APPROVED","paymentReference":"$paymentReference"}"""),
                    ),
            )
        }
    }

    fun stubRejected(
        wireMockServer: WireMockServer,
        apiKey: String,
        customerId: UUID,
        amount: BigDecimal,
        providerCode: String,
        providerMessage: String,
    ) {
        allureStep("Настроить WireMock stub для функционального отклонения платежа") {
            wireMockServer.stubFor(
                authorizationRequest(apiKey, customerId, amount)
                    .willReturn(
                        aResponse()
                            .withStatus(409)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"code":"$providerCode","message":"$providerMessage"}"""),
                    ),
            )
        }
    }

    fun stubServerError(
        wireMockServer: WireMockServer,
        apiKey: String,
        customerId: UUID,
        amount: BigDecimal,
    ) {
        allureStep("Настроить WireMock stub для server-side ошибки платежного провайдера") {
            wireMockServer.stubFor(
                authorizationRequest(apiKey, customerId, amount)
                    .willReturn(
                        aResponse()
                            .withStatus(503)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"code":"TEMPORARY_UNAVAILABLE","message":"Provider maintenance"}"""),
                    ),
            )
        }
    }

    fun stubTimeout(
        wireMockServer: WireMockServer,
        apiKey: String,
        customerId: UUID,
        amount: BigDecimal,
        delayMs: Int,
    ) {
        allureStep("Настроить WireMock stub для timeout-сценария") {
            wireMockServer.stubFor(
                authorizationRequest(apiKey, customerId, amount)
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withFixedDelay(delayMs)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"decision":"APPROVED","paymentReference":"timeout-reference"}"""),
                    ),
            )
        }
    }

    fun stubMalformedSuccess(
        wireMockServer: WireMockServer,
        apiKey: String,
        customerId: UUID,
        amount: BigDecimal,
    ) {
        allureStep("Настроить WireMock stub для некорректного payload от платежного провайдера") {
            wireMockServer.stubFor(
                authorizationRequest(apiKey, customerId, amount)
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"decision":"APPROVED"}"""),
                    ),
            )
        }
    }

    fun verifyAuthorizationRequest(
        wireMockServer: WireMockServer,
        apiKey: String,
        customerId: UUID,
        amount: BigDecimal,
        count: Int = 1,
    ) {
        allureStep("Проверить исходящий HTTP-запрос в платежный sandbox provider") {
            wireMockServer.verify(
                count,
                postRequestedFor(urlEqualTo(AUTHORIZE_PATH))
                    .withHeader(API_KEY_HEADER, equalTo(apiKey))
                    .withHeader(REQUEST_ID_HEADER, matching(UUID_REGEX))
                    .withRequestBody(equalToJson(expectedRequestBody(customerId, amount), true, true)),
            )
        }
    }

    private fun authorizationRequest(
        apiKey: String,
        customerId: UUID,
        amount: BigDecimal,
    ) = post(urlEqualTo(AUTHORIZE_PATH))
        .withHeader(API_KEY_HEADER, equalTo(apiKey))
        .withHeader(REQUEST_ID_HEADER, matching(UUID_REGEX))
        .withRequestBody(equalToJson(expectedRequestBody(customerId, amount), true, true))

    private fun expectedRequestBody(
        customerId: UUID,
        amount: BigDecimal,
    ): String =
        """
        {
          "customerId": "$customerId",
          "amount": ${amount.toPlainString()},
          "currency": "RUB",
          "paymentMethod": "MOCK_ONLINE"
        }
        """.trimIndent()

    private const val AUTHORIZE_PATH = "/api/v1/payments/authorize"
    private const val API_KEY_HEADER = "X-Api-Key"
    private const val REQUEST_ID_HEADER = "X-Request-Id"
    private const val UUID_REGEX = "^[0-9a-fA-F-]{36}$"
}
