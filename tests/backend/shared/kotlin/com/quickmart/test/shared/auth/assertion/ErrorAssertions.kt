package com.quickmart.test.shared.auth.assertion

import com.quickmart.test.shared.auth.model.ApiErrorModel
import com.quickmart.test.shared.foundation.allureStep
import com.quickmart.test.shared.foundation.toModel
import io.qameta.allure.Attachment
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat

object ErrorAssertions {
    fun assertInvalidCredentials(response: Response) {
        allureStep("Проверка ошибки: неверные учетные данные") {
            val error = attachAndMap(response)
            assertThat(response.statusCode).isEqualTo(401)
            assertThat(error.error).isEqualTo("Unauthorized")
            assertThat(error.message).isEqualTo("Неверный email или пароль")
            assertThat(error.path).isEqualTo("/api/auth/login")
        }
    }

    fun assertDuplicateEmailConflict(response: Response) {
        allureStep("Проверка ошибки: email уже зарегистрирован") {
            val error = attachAndMap(response)
            assertThat(response.statusCode).isEqualTo(409)
            assertThat(error.error).isEqualTo("Conflict")
            assertThat(error.message).contains("Email уже зарегистрирован")
            assertThat(error.path).isEqualTo("/api/auth/register")
        }
    }

    fun assertValidationErrorForField(
        response: Response,
        field: String,
    ) {
        allureStep("Проверка ошибки валидации поля `$field`") {
            val error = attachAndMap(response)
            assertThat(response.statusCode).isEqualTo(400)
            assertThat(error.error).isEqualTo("Bad Request")
            assertThat(error.message).isEqualTo("Ошибка валидации")
            assertThat(error.fieldErrors).isNotNull
            assertThat(error.fieldErrors).containsKey(field)
        }
    }

    fun assertUnauthorizedWithoutToken(
        response: Response,
        expectedPath: String,
    ) {
        allureStep("Проверка ошибки: доступ без авторизации") {
            val error = attachAndMap(response)
            assertThat(response.statusCode).isEqualTo(401)
            assertThat(error.error).isEqualTo("Unauthorized")
            assertThat(error.message).isNotBlank
            assertThat(error.path).isEqualTo(expectedPath)
        }
    }

    fun assertAdminEndpointRejectedForCustomer(response: Response) {
        allureStep("Проверка ошибки: административный endpoint недоступен для роли CUSTOMER") {
            val error = attachAndMap(response)
            assertThat(response.statusCode).isIn(401, 403)
            assertThat(error.error).isIn("Unauthorized", "Forbidden")
            assertThat(error.message).isNotBlank
            assertThat(error.path).isIn("/api/admin/products", "/error")
        }
    }

    @Attachment(value = "Ошибка API", type = "application/json")
    private fun attachErrorBody(errorBody: String): String = errorBody

    private fun attachAndMap(response: Response): ApiErrorModel {
        val body = response.body.asPrettyString()
        attachErrorBody(body)
        return response.toModel()
    }
}

