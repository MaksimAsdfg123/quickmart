package com.quickmart.test.support.listener

import io.qameta.allure.Allure
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import java.util.concurrent.TimeUnit

class AllureHttpLoggingFilter : Filter {
    override fun filter(
        requestSpec: FilterableRequestSpecification,
        responseSpec: FilterableResponseSpecification,
        filterContext: FilterContext,
    ): Response {
        val startedAt = System.nanoTime()
        val response = filterContext.next(requestSpec, responseSpec)
        val durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)

        Allure.addAttachment(
            "HTTP Request: ${requestSpec.method} ${requestSpec.uri}",
            "text/plain",
            buildRequestAttachment(requestSpec),
        )
        Allure.addAttachment(
            "HTTP Response: ${response.statusCode()} ${requestSpec.method} ${requestSpec.uri}",
            "text/plain",
            buildResponseAttachment(response, durationMs),
        )

        return response
    }

    private fun buildRequestAttachment(requestSpec: FilterableRequestSpecification): String =
        buildString {
            appendLine("Method: ${requestSpec.method}")
            appendLine("URL: ${requestSpec.uri}")
            appendLine("Headers:")
            requestSpec.headers.forEach { appendLine("  ${it.name}: ${it.value}") }

            appendLine("Query params:")
            if (requestSpec.queryParams.isEmpty()) {
                appendLine("  <none>")
            } else {
                requestSpec.queryParams.forEach { (key, value) -> appendLine("  $key: $value") }
            }

            appendLine("Path params:")
            if (requestSpec.pathParams.isEmpty()) {
                appendLine("  <none>")
            } else {
                requestSpec.pathParams.forEach { (key, value) -> appendLine("  $key: $value") }
            }

            appendLine("Body:")
            val rawBody =
                runCatching { requestSpec.getBody<Any>() }
                    .getOrNull()
            val bodyValue =
                rawBody
                    ?.toString()
                    ?.let { if (it.isBlank()) "<empty>" else it }
                    ?: "<empty>"
            appendLine(bodyValue)
        }

    private fun buildResponseAttachment(
        response: Response,
        durationMs: Long,
    ): String =
        buildString {
            appendLine("Status code: ${response.statusCode()}")
            appendLine("Response time: ${durationMs} ms")
            appendLine("Headers:")
            response.headers.forEach { appendLine("  ${it.name}: ${it.value}") }
            appendLine("Body:")
            appendLine(response.body.asPrettyString())
        }
}
