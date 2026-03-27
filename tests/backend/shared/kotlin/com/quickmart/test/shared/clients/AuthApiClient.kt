package com.quickmart.test.shared.clients

import com.quickmart.test.shared.auth.model.LoginRequestModel
import com.quickmart.test.shared.auth.model.RegisterRequestModel
import com.quickmart.test.shared.foundation.ApiSpecifications
import com.quickmart.test.shared.foundation.allureStep
import io.restassured.RestAssured.given
import io.restassured.response.Response

class AuthApiClient(
    private val specifications: ApiSpecifications,
) {
    fun register(request: RegisterRequestModel): Response =
        allureStep("Отправка запроса на регистрацию пользователя") {
            given()
                .spec(specifications.unauthenticated())
                .body(request)
                .`when`()
                .post("/api/auth/register")
                .then()
                .spec(specifications.jsonResponse())
                .extract()
                .response()
        }

    fun login(request: LoginRequestModel): Response =
        allureStep("Отправка запроса на аутентификацию пользователя") {
            given()
                .spec(specifications.unauthenticated())
                .body(request)
                .`when`()
                .post("/api/auth/login")
                .then()
                .spec(specifications.jsonResponse())
                .extract()
                .response()
        }
}

