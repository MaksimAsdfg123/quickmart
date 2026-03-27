package com.quickmart.test.shared.ui.listeners

import com.microsoft.playwright.Page
import com.microsoft.playwright.Request
import com.microsoft.playwright.Response
import com.quickmart.test.shared.ui.helpers.AllureHelper
import com.quickmart.test.shared.ui.helpers.JsonHelper
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList

data class NetworkEvent(
    val timestamp: String,
    val type: String,
    val method: String,
    val url: String,
    val status: Int? = null,
    val failureText: String? = null,
    val requestBody: String? = null,
    val responseBody: String? = null,
)

class NetworkDiagnosticsCollector {
    private val events = CopyOnWriteArrayList<NetworkEvent>()

    fun bind(page: Page) {
        page.onRequest { request -> recordRequest(request) }
        page.onResponse { response -> recordResponse(response) }
        page.onRequestFailed { request -> recordFailure(request) }
    }

    fun writeReport(reportPath: Path): Path {
        val payload = JsonHelper.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(events)
        reportPath.toFile().writeText(payload)
        return reportPath
    }

    fun attachToAllure(reportPath: Path) {
        val payload = JsonHelper.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(events)
        AllureHelper.attachJson("Сетевой лог", payload)
        AllureHelper.attachText("Путь к сетевому логу", reportPath.toAbsolutePath().toString())
    }

    private fun recordRequest(request: Request) {
        if (!isApiCall(request.url())) return
        events +=
            NetworkEvent(
                timestamp = Instant.now().toString(),
                type = "request",
                method = request.method(),
                url = request.url(),
                requestBody = request.postData(),
            )
    }

    private fun recordResponse(response: Response) {
        val request = response.request()
        if (!isApiCall(request.url())) return

        val responseBody =
            if (response.status() >= 400) {
                runCatching { response.text().take(2_000) }.getOrNull()
            } else {
                null
            }

        events +=
            NetworkEvent(
                timestamp = Instant.now().toString(),
                type = "response",
                method = request.method(),
                url = request.url(),
                status = response.status(),
                responseBody = responseBody,
            )
    }

    private fun recordFailure(request: Request) {
        if (!isApiCall(request.url())) return
        events +=
            NetworkEvent(
                timestamp = Instant.now().toString(),
                type = "request-failed",
                method = request.method(),
                url = request.url(),
                failureText = request.failure(),
                requestBody = request.postData(),
            )
    }

    private fun isApiCall(url: String): Boolean = url.contains("/api/")
}

