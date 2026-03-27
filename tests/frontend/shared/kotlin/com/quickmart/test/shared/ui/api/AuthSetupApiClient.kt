package com.quickmart.test.shared.ui.api

import com.quickmart.test.shared.ui.config.UiTestEnvironment
import com.quickmart.test.shared.ui.helpers.AllureHelper
import com.quickmart.test.shared.ui.helpers.JsonHelper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class AuthSetupApiClient(
    private val env: UiTestEnvironment = UiTestEnvironment,
) {
    private val httpClient: HttpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build()

    fun login(email: String, password: String): AuthApiResponse =
        AllureHelper.stepWithResult("API: получение JWT для пользователя $email") {
            val payload = LoginApiPayload(email = email, password = password)
            val result = post("/api/auth/login", payload)
            require(result.statusCode == 200) {
                "Ожидался 200 при login, получен ${result.statusCode}. body=${result.body}"
            }
            result.data ?: error("Пустой ответ login")
        }

    fun register(payload: RegisterApiPayload): ApiCallResult<AuthApiResponse> =
        AllureHelper.stepWithResult("API: регистрация тестового пользователя ${payload.email}") {
            post("/api/auth/register", payload)
        }

    fun ensureUserExists(payload: RegisterApiPayload) {
        val result = register(payload)
        require(result.statusCode == 201 || result.statusCode == 409) {
            "Ожидался 201 или 409 для ensureUserExists, получен ${result.statusCode}. body=${result.body}"
        }
    }

    private fun post(path: String, payload: Any): ApiCallResult<AuthApiResponse> {
        val requestBody = JsonHelper.mapper.writeValueAsString(payload)
        val request =
            HttpRequest.newBuilder()
                .uri(URI.create(env.apiBaseUrl.removeSuffix("/") + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        val responseBody = response.body().orEmpty()

        val parsed =
            if (response.statusCode() in 200..299) {
                JsonHelper.mapper.readValue(responseBody, AuthApiResponse::class.java)
            } else {
                null
            }

        return ApiCallResult(
            statusCode = response.statusCode(),
            body = responseBody,
            data = parsed,
        )
    }
}

