package com.quickmart.test.support.client

import com.quickmart.test.support.spec.ApiSpecifications
import com.quickmart.test.support.util.allureStep
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
