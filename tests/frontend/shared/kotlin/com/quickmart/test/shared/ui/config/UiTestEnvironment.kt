package com.quickmart.test.shared.ui.config

import java.io.InputStream
import java.util.Properties

object UiTestEnvironment {
    private const val CONFIG_FILE = "test-environment.properties"

    private val properties: Properties =
        Properties().apply {
            val stream: InputStream? = Thread.currentThread().contextClassLoader.getResourceAsStream(CONFIG_FILE)
            stream?.use { load(it) }
        }

    val uiBaseUrl: String = readString("UI_BASE_URL", "ui.base-url", "http://127.0.0.1:5173")
    val apiBaseUrl: String = readString("API_BASE_URL", "api.base-url", "http://127.0.0.1:8080")

    val customerEmail: String = readString("E2E_CUSTOMER_EMAIL", "auth.customer.email", "anna@example.com")
    val customerPassword: String = readString("E2E_CUSTOMER_PASSWORD", "auth.customer.password", "password")

    val adminEmail: String = readString("E2E_ADMIN_EMAIL", "auth.admin.email", "admin@quickmart.local")
    val adminPassword: String = readString("E2E_ADMIN_PASSWORD", "auth.admin.password", "password")

    val headless: Boolean = readBoolean("UI_HEADLESS", "ui.headless", true)
    val browserName: String = readString("UI_BROWSER", "ui.browser", "chromium")
    val slowMoMs: Double = readLong("UI_SLOW_MO_MS", "ui.slow-mo-ms", 0L).toDouble()

    val actionTimeoutMs: Double = readLong("UI_ACTION_TIMEOUT_MS", "ui.action-timeout-ms", 10_000L).toDouble()
    val navigationTimeoutMs: Double = readLong("UI_NAVIGATION_TIMEOUT_MS", "ui.navigation-timeout-ms", 25_000L).toDouble()

    private fun readString(envKey: String, propertyKey: String, fallback: String): String =
        System.getenv(envKey)?.takeIf { it.isNotBlank() }
            ?: properties.getProperty(propertyKey)?.takeIf { it.isNotBlank() }
            ?: fallback

    private fun readLong(envKey: String, propertyKey: String, fallback: Long): Long =
        readString(envKey, propertyKey, fallback.toString()).toLongOrNull() ?: fallback

    private fun readBoolean(envKey: String, propertyKey: String, fallback: Boolean): Boolean =
        readString(envKey, propertyKey, fallback.toString()).toBooleanStrictOrNull() ?: fallback
}

