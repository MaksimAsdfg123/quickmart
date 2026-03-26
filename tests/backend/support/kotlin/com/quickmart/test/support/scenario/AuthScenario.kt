package com.quickmart.test.support.scenario

import com.quickmart.test.support.client.AdminProductsApiClient
import com.quickmart.test.support.client.AuthApiClient
import com.quickmart.test.support.client.CartApiClient
import com.quickmart.test.support.data.AuthTestDataFactory
import com.quickmart.test.support.model.LoginRequestModel
import com.quickmart.test.support.model.AuthResponseModel
import com.quickmart.test.support.model.RegisterRequestModel
import com.quickmart.test.support.util.allureStep
import com.quickmart.test.support.util.toModel
import io.restassured.response.Response

data class RegistrationScenarioResult(
    val request: RegisterRequestModel,
    val response: Response,
)

data class RegistrationWithCartScenarioResult(
    val registration: RegistrationScenarioResult,
    val cartResponse: Response,
)

data class DuplicateRegistrationScenarioResult(
    val request: RegisterRequestModel,
    val firstResponse: Response,
    val secondResponse: Response,
)

class AuthScenario(
    private val authApiClient: AuthApiClient,
    private val cartApiClient: CartApiClient,
    private val adminProductsApiClient: AdminProductsApiClient,
) {
    fun registerCustomer(request: RegisterRequestModel = AuthTestDataFactory.newCustomerRegistration()): RegistrationScenarioResult =
        allureStep("Сценарий: регистрация нового пользователя") {
            RegistrationScenarioResult(
                request = request,
                response = authApiClient.register(request),
            )
        }

    fun registerCustomerAndFetchCart(request: RegisterRequestModel = AuthTestDataFactory.newCustomerRegistration()): RegistrationWithCartScenarioResult =
        allureStep("Сценарий: регистрация пользователя и получение его корзины") {
            val registration = registerCustomer(request)
            val token = registration.response.toModel<AuthResponseModel>().token
            RegistrationWithCartScenarioResult(
                registration = registration,
                cartResponse = cartApiClient.getCart(token),
            )
        }

    fun registerThenDuplicate(request: RegisterRequestModel = AuthTestDataFactory.newCustomerRegistration()): DuplicateRegistrationScenarioResult =
        allureStep("Сценарий: повторная регистрация с тем же email") {
            val firstResponse = authApiClient.register(request)
            val secondResponse = authApiClient.register(AuthTestDataFactory.duplicatedRegistration(request.email))
            DuplicateRegistrationScenarioResult(
                request = request,
                firstResponse = firstResponse,
                secondResponse = secondResponse,
            )
        }

    fun loginCustomer(request: LoginRequestModel = AuthTestDataFactory.seededCustomerLogin()): Response =
        allureStep("Сценарий: аутентификация покупателя") {
            authApiClient.login(request)
        }

    fun loginAdmin(request: LoginRequestModel = AuthTestDataFactory.adminLogin()): Response =
        allureStep("Сценарий: аутентификация администратора") {
            authApiClient.login(request)
        }

    fun tryLoginInvalidCredentials(request: LoginRequestModel): Response =
        allureStep("Сценарий: аутентификация с невалидными учетными данными") {
            authApiClient.login(request)
        }

    fun callProtectedCartWithoutToken(): Response =
        allureStep("Сценарий: доступ к защищенному ресурсу без токена") {
            cartApiClient.getCartWithoutToken()
        }

    fun callAdminProductsAsCustomer(customerToken: String): Response =
        allureStep("Сценарий: доступ покупателя к административному endpoint") {
            adminProductsApiClient.getProductsPage(customerToken)
        }
}
