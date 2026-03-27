package com.quickmart.test.shared.ui.base

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Tracing
import com.microsoft.playwright.options.ScreenshotType
import com.quickmart.test.shared.ui.config.UiTestEnvironment
import com.quickmart.test.shared.ui.helpers.AllureHelper
import com.quickmart.test.shared.ui.helpers.ArtifactManager
import com.quickmart.test.shared.ui.helpers.TestArtifacts
import com.quickmart.test.shared.ui.listeners.NetworkDiagnosticsCollector
import io.qameta.allure.Allure
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.TestInstance
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
abstract class BaseUiSuite {
    companion object {
        private lateinit var playwright: Playwright
        private lateinit var sharedBrowser: Browser

        @JvmStatic
        @BeforeAll
        fun globalSetUp() {
            playwright = Playwright.create()
            sharedBrowser = launchBrowser(playwright)
        }

        @JvmStatic
        @AfterAll
        fun globalTearDown() {
            if (::sharedBrowser.isInitialized) {
                sharedBrowser.close()
            }
            if (::playwright.isInitialized) {
                playwright.close()
            }
        }

        private fun launchBrowser(playwright: Playwright): Browser {
            val browserType =
                when (UiTestEnvironment.browserName.lowercase()) {
                    "firefox" -> playwright.firefox()
                    "webkit" -> playwright.webkit()
                    else -> playwright.chromium()
                }

            return browserType.launch(
                BrowserType.LaunchOptions()
                    .setHeadless(UiTestEnvironment.headless),
            )
        }
    }

    protected lateinit var page: Page
    protected lateinit var artifacts: TestArtifacts

    private lateinit var context: com.microsoft.playwright.BrowserContext
    private lateinit var networkDiagnosticsCollector: NetworkDiagnosticsCollector

    @BeforeEach
    fun setUp(testInfo: TestInfo) {
        artifacts = ArtifactManager.prepare(testInfo.displayName)

        context =
            sharedBrowser.newContext(
                Browser.NewContextOptions()
                    .setBaseURL(UiTestEnvironment.uiBaseUrl)
                    .setIgnoreHTTPSErrors(true)
                    .setRecordVideoDir(artifacts.rootDir.resolve("videos"))
                    .setViewportSize(1440, 900),
            )

        page = context.newPage()
        page.setDefaultTimeout(UiTestEnvironment.actionTimeoutMs)
        page.setDefaultNavigationTimeout(UiTestEnvironment.navigationTimeoutMs)

        networkDiagnosticsCollector = NetworkDiagnosticsCollector()
        networkDiagnosticsCollector.bind(page)

        context.tracing().start(
            Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true),
        )

        Allure.label("layer", "ui")
    }

    @AfterEach
    fun tearDown() {
        runCatching {
            page.screenshot(
                Page.ScreenshotOptions()
                    .setType(ScreenshotType.PNG)
                    .setFullPage(true)
                    .setPath(artifacts.screenshotPath),
            )
            AllureHelper.attachFile("Скриншот выполнения", "image/png", artifacts.screenshotPath)
        }

        runCatching {
            context.tracing().stop(Tracing.StopOptions().setPath(artifacts.tracePath))
            AllureHelper.attachFile("Playwright trace", "application/zip", artifacts.tracePath)
        }

        runCatching {
            networkDiagnosticsCollector.writeReport(artifacts.networkLogPath)
            networkDiagnosticsCollector.attachToAllure(artifacts.networkLogPath)
        }

        val recordedVideoPath =
            runCatching {
                val video = page.video()
                context.close()
                video?.path()
            }.getOrNull()

        if (recordedVideoPath != null) {
            AllureHelper.attachFile("Видео выполнения теста", "video/webm", recordedVideoPath)
        }
    }
}

