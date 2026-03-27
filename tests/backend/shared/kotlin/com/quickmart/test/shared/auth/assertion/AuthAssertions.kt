package com.quickmart.test.shared.auth.assertion

import com.quickmart.test.shared.auth.model.AuthResponseModel
import com.quickmart.test.shared.auth.model.RegisterRequestModel
import com.quickmart.test.shared.foundation.allureStep
import com.quickmart.test.shared.foundation.toModel
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat

object AuthAssertions {
    fun assertRegistrationSucceeded(
        response: Response,
        request: RegisterRequestModel,
    ): AuthResponseModel =
        allureStep("Проверка ответа успешной регистрации") {
            assertThat(response.statusCode).isEqualTo(201)
            val authResponse = response.toModel<AuthResponseModel>()
            assertTokenShape(authResponse.token)
            assertThat(authResponse.user.role).isEqualTo("CUSTOMER")
            assertThat(authResponse.user.email).isEqualTo(request.email.trim().lowercase())
            assertThat(authResponse.user.fullName).isEqualTo(request.fullName.trim())
            assertThat(authResponse.user.id).isNotNull
            authResponse
        }

    fun assertLoginSucceeded(
        response: Response,
        expectedEmail: String,
        expectedRole: String,
    ): AuthResponseModel =
        allureStep("Проверка ответа успешной аутентификации") {
            assertThat(response.statusCode).isEqualTo(200)
            val authResponse = response.toModel<AuthResponseModel>()
            assertTokenShape(authResponse.token)
            assertThat(authResponse.user.email).isEqualTo(expectedEmail.trim().lowercase())
            assertThat(authResponse.user.role).isEqualTo(expectedRole)
            authResponse
        }

    private fun assertTokenShape(token: String) {
        assertThat(token).isNotBlank
        assertThat(token.split("."))
            .describedAs("JWT должен состоять из 3 сегментов")
            .hasSize(3)
    }
}

