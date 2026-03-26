package com.quickmart.test.support.base

import com.quickmart.test.support.client.AdminProductsApiClient
import com.quickmart.test.support.client.AuthApiClient
import com.quickmart.test.support.client.CartApiClient
import com.quickmart.test.support.config.ApiTestEnvironment
import com.quickmart.test.support.config.ApiTestEnvironmentLoader
import com.quickmart.test.support.scenario.AuthScenario
import com.quickmart.test.support.spec.ApiSpecifications
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

