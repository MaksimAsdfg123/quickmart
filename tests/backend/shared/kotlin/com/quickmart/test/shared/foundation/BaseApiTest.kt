package com.quickmart.test.shared.foundation

import com.quickmart.test.shared.auth.scenario.AuthScenario
import com.quickmart.test.shared.clients.AdminProductsApiClient
import com.quickmart.test.shared.clients.AuthApiClient
import com.quickmart.test.shared.clients.CartApiClient
import io.restassured.RestAssured
import org.junit.jupiter.api.BeforeEach

abstract class BaseApiTest {
    protected val environment: ApiTestEnvironment = ApiTestEnvironmentLoader.load()

    protected lateinit var specifications: ApiSpecifications
    protected lateinit var authApiClient: AuthApiClient
    protected lateinit var cartApiClient: CartApiClient
    protected lateinit var adminProductsApiClient: AdminProductsApiClient
    protected lateinit var authScenario: AuthScenario

    @BeforeEach
    fun baseSetup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()

        specifications = ApiSpecifications(environment)
        authApiClient = AuthApiClient(specifications)
        cartApiClient = CartApiClient(specifications)
        adminProductsApiClient = AdminProductsApiClient(specifications)
        authScenario = AuthScenario(authApiClient, cartApiClient, adminProductsApiClient)
    }
}

