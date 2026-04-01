package com.quickmart.test.suites.wiremock.payment

import com.quickmart.test.shared.wiremock.payment.assertion.MockOnlinePaymentAssertions
import com.quickmart.test.shared.wiremock.payment.foundation.BaseMockOnlinePaymentComponentTest
import com.quickmart.test.shared.wiremock.payment.mock.MockOnlinePaymentWireMockStubs
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Owner
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@Epic("Quickmart Backend Tests")
@Feature("External HTTP Integration")
@Owner("backend-platform")
@Tag("wiremock")
@DisplayName("WireMock: mock online payment provider")
class MockOnlinePaymentCheckoutTest : BaseMockOnlinePaymentComponentTest() {
    @Test
    @Story("Success case")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Успешный ответ payment provider подтверждает MOCK_ONLINE checkout и сохраняет external reference")
    fun shouldAuthorizeCheckoutThroughWireMockProvider() {
        val fixture = checkoutScenario.prepareFixture(prefix = "payment-success", unitPrice = BigDecimal("600.00"), quantity = 2)
        val ordersBefore = orderRepository.count()
        val paymentsBefore = paymentRepository.count()

        MockOnlinePaymentWireMockStubs.stubApproved(
            wireMockServer = wireMockServer,
            apiKey = PAYMENT_PROVIDER_API_KEY,
            customerId = fixture.userId,
            amount = fixture.expectedTotal,
            paymentReference = "wm-pay-approved-001",
        )

        val response = checkoutScenario.checkoutWithMockOnline(fixture)

        MockOnlinePaymentAssertions.assertApprovedCheckout(response, fixture)
        MockOnlinePaymentAssertions.assertPersistedExternalReference(orderRepository, response.id, "wm-pay-approved-001")
        MockOnlinePaymentAssertions.assertCartCleared(cartRepository, fixture.userId)
        MockOnlinePaymentAssertions.assertStockQuantity(inventoryStockRepository, fixture.productId, fixture.expectedRemainingStock)
        MockOnlinePaymentWireMockStubs.verifyAuthorizationRequest(
            wireMockServer = wireMockServer,
            apiKey = PAYMENT_PROVIDER_API_KEY,
            customerId = fixture.userId,
            amount = fixture.expectedTotal,
        )

        assertThat(orderRepository.count()).isEqualTo(ordersBefore + 1)
        assertThat(paymentRepository.count()).isEqualTo(paymentsBefore + 1)
    }

    @Test
    @Story("Functional rejection")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Функциональный отказ провайдера переводится в контролируемый бизнес-ошибочный результат без побочных эффектов")
    fun shouldHandleProviderRejectionWithoutPersistingOrder() {
        val fixture = checkoutScenario.prepareFixture(prefix = "payment-rejected", unitPrice = BigDecimal("700.00"), quantity = 1)
        val ordersBefore = orderRepository.count()
        val paymentsBefore = paymentRepository.count()

        MockOnlinePaymentWireMockStubs.stubRejected(
            wireMockServer = wireMockServer,
            apiKey = PAYMENT_PROVIDER_API_KEY,
            customerId = fixture.userId,
            amount = fixture.expectedTotal,
            providerCode = "INSUFFICIENT_FUNDS",
            providerMessage = "Insufficient funds",
        )

        val failure = catchThrowable { checkoutScenario.checkoutWithMockOnline(fixture) }

        MockOnlinePaymentAssertions.assertBusinessFailure(failure!!, 409, "Insufficient funds")
        MockOnlinePaymentAssertions.assertCartRetained(cartRepository, fixture.userId, fixture.quantity)
        MockOnlinePaymentAssertions.assertStockQuantity(inventoryStockRepository, fixture.productId, fixture.initialStock)
        MockOnlinePaymentWireMockStubs.verifyAuthorizationRequest(
            wireMockServer = wireMockServer,
            apiKey = PAYMENT_PROVIDER_API_KEY,
            customerId = fixture.userId,
            amount = fixture.expectedTotal,
        )

        assertThat(orderRepository.count()).isEqualTo(ordersBefore)
        assertThat(paymentRepository.count()).isEqualTo(paymentsBefore)
    }

    @Test
    @Story("Server-side error")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Server-side ошибка платежного провайдера завершается предсказуемым 503 without order persistence")
    fun shouldHandleProviderServerError() {
        val fixture = checkoutScenario.prepareFixture(prefix = "payment-server-error", unitPrice = BigDecimal("650.00"), quantity = 1)
        val ordersBefore = orderRepository.count()
        val paymentsBefore = paymentRepository.count()

        MockOnlinePaymentWireMockStubs.stubServerError(
            wireMockServer = wireMockServer,
            apiKey = PAYMENT_PROVIDER_API_KEY,
            customerId = fixture.userId,
            amount = fixture.expectedTotal,
        )

        val failure = catchThrowable { checkoutScenario.checkoutWithMockOnline(fixture) }

        MockOnlinePaymentAssertions.assertBusinessFailure(failure!!, 503, "временно недоступен")
        MockOnlinePaymentAssertions.assertCartRetained(cartRepository, fixture.userId, fixture.quantity)
        MockOnlinePaymentAssertions.assertStockQuantity(inventoryStockRepository, fixture.productId, fixture.initialStock)
        MockOnlinePaymentWireMockStubs.verifyAuthorizationRequest(
            wireMockServer = wireMockServer,
            apiKey = PAYMENT_PROVIDER_API_KEY,
            customerId = fixture.userId,
            amount = fixture.expectedTotal,
        )

        assertThat(orderRepository.count()).isEqualTo(ordersBefore)
        assertThat(paymentRepository.count()).isEqualTo(paymentsBefore)
    }

    @Test
    @Story("Timeout")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Timeout внешнего payment provider обрабатывается как контролируемая техническая недоступность")
    fun shouldHandleProviderTimeout() {
        val fixture = checkoutScenario.prepareFixture(prefix = "payment-timeout", unitPrice = BigDecimal("550.00"), quantity = 1)
        val ordersBefore = orderRepository.count()
        val paymentsBefore = paymentRepository.count()

        MockOnlinePaymentWireMockStubs.stubTimeout(
            wireMockServer = wireMockServer,
            apiKey = PAYMENT_PROVIDER_API_KEY,
            customerId = fixture.userId,
            amount = fixture.expectedTotal,
            delayMs = 1_500,
        )

        val failure = catchThrowable { checkoutScenario.checkoutWithMockOnline(fixture) }

        MockOnlinePaymentAssertions.assertBusinessFailure(failure!!, 503, "не ответил вовремя")
        MockOnlinePaymentAssertions.assertCartRetained(cartRepository, fixture.userId, fixture.quantity)
        MockOnlinePaymentAssertions.assertStockQuantity(inventoryStockRepository, fixture.productId, fixture.initialStock)
        MockOnlinePaymentWireMockStubs.verifyAuthorizationRequest(
            wireMockServer = wireMockServer,
            apiKey = PAYMENT_PROVIDER_API_KEY,
            customerId = fixture.userId,
            amount = fixture.expectedTotal,
        )

        assertThat(orderRepository.count()).isEqualTo(ordersBefore)
        assertThat(paymentRepository.count()).isEqualTo(paymentsBefore)
    }

    @Test
    @Story("Malformed payload")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Некорректный payload от внешнего провайдера обрабатывается как bad gateway без скрытых побочных эффектов")
    fun shouldHandleMalformedProviderPayload() {
        val fixture = checkoutScenario.prepareFixture(prefix = "payment-malformed", unitPrice = BigDecimal("720.00"), quantity = 1)
        val ordersBefore = orderRepository.count()
        val paymentsBefore = paymentRepository.count()

        MockOnlinePaymentWireMockStubs.stubMalformedSuccess(
            wireMockServer = wireMockServer,
            apiKey = PAYMENT_PROVIDER_API_KEY,
            customerId = fixture.userId,
            amount = fixture.expectedTotal,
        )

        val failure = catchThrowable { checkoutScenario.checkoutWithMockOnline(fixture) }

        MockOnlinePaymentAssertions.assertBusinessFailure(failure!!, 502, "некорректный ответ")
        MockOnlinePaymentAssertions.assertCartRetained(cartRepository, fixture.userId, fixture.quantity)
        MockOnlinePaymentAssertions.assertStockQuantity(inventoryStockRepository, fixture.productId, fixture.initialStock)
        MockOnlinePaymentWireMockStubs.verifyAuthorizationRequest(
            wireMockServer = wireMockServer,
            apiKey = PAYMENT_PROVIDER_API_KEY,
            customerId = fixture.userId,
            amount = fixture.expectedTotal,
        )

        assertThat(orderRepository.count()).isEqualTo(ordersBefore)
        assertThat(paymentRepository.count()).isEqualTo(paymentsBefore)
    }
}
