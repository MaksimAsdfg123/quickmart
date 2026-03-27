package com.quickmart.test.suites.api.auth

import com.quickmart.test.shared.auth.assertion.AuthAssertions
import com.quickmart.test.shared.auth.assertion.CartAssertions
import com.quickmart.test.shared.auth.assertion.ErrorAssertions
import com.quickmart.test.shared.auth.data.AuthTestDataFactory
import com.quickmart.test.shared.foundation.BaseApiTest
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Owner
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Epic("Quickmart Backend API")
@Feature("Аутентификация и авторизация")
@Owner("qa-automation")
@Tag("api")
@DisplayName("API: Аутентификация")
class AuthApiTest : BaseApiTest() {
    @Test
    @Story("Регистрация пользователя")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Регистрация создает пользователя CUSTOMER и возвращает JWT")
    fun shouldRegisterCustomerAndReturnJwt() {
        val request = AuthTestDataFactory.registrationWithWhitespaceAndUppercaseEmail()

        val result = authScenario.registerCustomer(request)

        AuthAssertions.assertRegistrationSucceeded(result.response, result.request)
    }

    @Test
    @Story("Регистрация пользователя")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("После регистрации пользователь получает доступ к собственной корзине")
    fun shouldAllowRegisteredCustomerToAccessCart() {
        val request = AuthTestDataFactory.newCustomerRegistration()

        val result = authScenario.registerCustomerAndFetchCart(request)

        AuthAssertions.assertRegistrationSucceeded(result.registration.response, result.registration.request)
            .also { CartAssertions.assertEmptyCartReturned(result.cartResponse) }
    }

    @Test
    @Story("Аутентификация пользователя")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Логин покупателя работает для email с разным регистром")
    fun shouldLoginCustomerWithNormalizedEmail() {
        val loginRequest = AuthTestDataFactory.seededCustomerLogin(email = "ANNA@EXAMPLE.COM")

        val response = authScenario.loginCustomer(loginRequest)

        AuthAssertions.assertLoginSucceeded(response, expectedEmail = "anna@example.com", expectedRole = "CUSTOMER")
    }

    @Test
    @Story("Аутентификация пользователя")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Логин администратора возвращает роль ADMIN")
    fun shouldLoginAdminWithAdminRole() {
        val response = authScenario.loginAdmin()

        AuthAssertions.assertLoginSucceeded(response, expectedEmail = environment.adminEmail, expectedRole = "ADMIN")
    }

    @Test
    @Story("Регистрация пользователя")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Повторная регистрация с тем же email отклоняется с ошибкой 409")
    fun shouldRejectDuplicateEmail() {
        val request = AuthTestDataFactory.newCustomerRegistration()

        val result = authScenario.registerThenDuplicate(request)

        AuthAssertions.assertRegistrationSucceeded(result.firstResponse, result.request)
        ErrorAssertions.assertDuplicateEmailConflict(result.secondResponse)
    }

    @Test
    @Story("Аутентификация пользователя")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Логин с неверным паролем возвращает 401 и бизнес-сообщение")
    fun shouldRejectInvalidCredentials() {
        val response =
            authScenario.tryLoginInvalidCredentials(
                AuthTestDataFactory.seededCustomerLogin(password = "wrong-password"),
            )

        ErrorAssertions.assertInvalidCredentials(response)
    }

    @Test
    @Story("Валидация входных данных")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Регистрация с коротким паролем возвращает 400 и fieldErrors.password")
    fun shouldValidateRegistrationPayload() {
        val response = authScenario.registerCustomer(AuthTestDataFactory.invalidShortPasswordRegistration()).response

        ErrorAssertions.assertValidationErrorForField(response, "password")
    }

    @Test
    @Story("Авторизация доступа")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Доступ к защищенному endpoint без токена возвращает 401")
    fun shouldRejectProtectedResourceWithoutToken() {
        val response = authScenario.callProtectedCartWithoutToken()

        ErrorAssertions.assertUnauthorizedWithoutToken(response, "/api/cart")
    }

    @Test
    @Story("Авторизация доступа")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Покупатель не может получить доступ к административному endpoint")
    fun shouldRejectAdminEndpointForCustomerRole() {
        val customerLoginResponse = authScenario.loginCustomer(AuthTestDataFactory.seededCustomerLogin())
        val customerToken = AuthAssertions.assertLoginSucceeded(customerLoginResponse, "anna@example.com", "CUSTOMER").token

        val adminEndpointResponse = authScenario.callAdminProductsAsCustomer(customerToken)

        ErrorAssertions.assertAdminEndpointRejectedForCustomer(adminEndpointResponse)
    }
}

