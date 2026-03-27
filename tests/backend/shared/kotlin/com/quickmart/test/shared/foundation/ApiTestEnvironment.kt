package com.quickmart.test.shared.foundation

import java.io.InputStream
import java.util.Properties

data class ApiTestEnvironment(
    val environmentName: String,
    val baseUrl: String,
    val connectTimeoutMs: Int,
    val readTimeoutMs: Int,
    val adminEmail: String,
    val adminPassword: String,
    val customerEmail: String,
    val customerPassword: String,
)

object ApiTestEnvironmentLoader {
    private val properties: Properties by lazy {
        val loaded = Properties()
        resourceStream("test-environment.properties")?.use { loaded.load(it) }
        loaded
    }

    fun load(): ApiTestEnvironment =
        ApiTestEnvironment(
            environmentName = resolve("TEST_ENVIRONMENT", "test.environment", "local"),
            baseUrl = resolve("API_BASE_URL", "api.base-url", "http://127.0.0.1:8080"),
            connectTimeoutMs = resolveInt("API_CONNECT_TIMEOUT_MS", "api.connect-timeout-ms", 5000),
            readTimeoutMs = resolveInt("API_READ_TIMEOUT_MS", "api.read-timeout-ms", 10000),
            adminEmail = resolve("AUTH_ADMIN_EMAIL", "auth.admin.email", "admin@quickmart.local"),
            adminPassword = resolve("AUTH_ADMIN_PASSWORD", "auth.admin.password", "password"),
            customerEmail = resolve("AUTH_CUSTOMER_EMAIL", "auth.customer.email", "anna@example.com"),
            customerPassword = resolve("AUTH_CUSTOMER_PASSWORD", "auth.customer.password", "password"),
        )

    private fun resolve(
        envKey: String,
        propertyKey: String,
        fallback: String,
    ): String =
        System.getenv(envKey)
            ?: System.getProperty(envKey.lowercase().replace("_", "."))
            ?: properties.getProperty(propertyKey)
            ?: fallback

    private fun resolveInt(
        envKey: String,
        propertyKey: String,
        fallback: Int,
    ): Int = resolve(envKey, propertyKey, fallback.toString()).toIntOrNull() ?: fallback

    private fun resourceStream(name: String): InputStream? =
        Thread.currentThread().contextClassLoader.getResourceAsStream(name)
}

