package com.quickmart.test.support.client

import com.quickmart.test.support.spec.ApiSpecifications
import com.quickmart.test.support.util.allureStep
import io.restassured.RestAssured.given
import io.restassured.response.Response

class CartApiClient(
    private val specifications: ApiSpecifications,
) {
    fun getCart(token: String): Response =
        allureStep("Запрос состояния корзины с валидным токеном") {
            given()
                .spec(specifications.authenticated(token))
                .`when`()
                .get("/api/cart")
                .then()
                .spec(specifications.jsonResponse())
                .extract()
                .response()
        }

    fun getCartWithoutToken(): Response =
        allureStep("Запрос состояния корзины без токена") {
            given()
                .spec(specifications.unauthenticated())
                .`when`()
                .get("/api/cart")
                .then()
                .spec(specifications.jsonResponse())
                .extract()
                .response()
        }
}
