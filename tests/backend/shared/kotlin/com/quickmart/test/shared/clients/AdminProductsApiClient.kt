package com.quickmart.test.shared.clients

import com.quickmart.test.shared.foundation.ApiSpecifications
import com.quickmart.test.shared.foundation.allureStep
import io.restassured.RestAssured.given
import io.restassured.response.Response

class AdminProductsApiClient(
    private val specifications: ApiSpecifications,
) {
    fun getProductsPage(token: String): Response =
        allureStep("Запрос административного списка товаров") {
            given()
                .spec(specifications.authenticated(token))
                .queryParam("page", 0)
                .queryParam("size", 20)
                .`when`()
                .get("/api/admin/products")
                .then()
                .spec(specifications.jsonResponse())
                .extract()
                .response()
        }
}

