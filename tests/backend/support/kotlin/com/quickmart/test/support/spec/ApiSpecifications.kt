package com.quickmart.test.support.spec

import com.quickmart.test.support.config.ApiTestEnvironment
import com.quickmart.test.support.listener.AllureHttpLoggingFilter
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.config.HttpClientConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification

class ApiSpecifications(
    private val environment: ApiTestEnvironment,
) {
    private val loggingFilter = AllureHttpLoggingFilter()

    private val restAssuredConfig: RestAssuredConfig =
        RestAssuredConfig.config().httpClient(
            HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", environment.connectTimeoutMs)
                .setParam("http.socket.timeout", environment.readTimeoutMs),
        )

    private val jsonResponseSpecification: ResponseSpecification =
        ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .build()

    fun unauthenticated(): RequestSpecification =
        RequestSpecBuilder()
            .setBaseUri(environment.baseUrl)
            .setConfig(restAssuredConfig)
            .setAccept(ContentType.JSON)
            .setContentType(ContentType.JSON)
            .addFilter(loggingFilter)
            .build()

    fun authenticated(token: String): RequestSpecification =
        RequestSpecBuilder()
            .addRequestSpecification(unauthenticated())
            .addHeader("Authorization", "Bearer $token")
            .build()

    fun jsonResponse(): ResponseSpecification = jsonResponseSpecification
}

