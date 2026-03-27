package com.quickmart.test.shared.ui.helpers

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class TestArtifacts(
    val testRunId: String,
    val rootDir: Path,
    val screenshotPath: Path,
    val tracePath: Path,
    val networkLogPath: Path,
)

object ArtifactManager {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")
    private val artifactsRoot: Path = Paths.get("artifacts").toAbsolutePath().normalize()

    fun prepare(displayName: String): TestArtifacts {
        val safeName = displayName.lowercase().replace(Regex("[^a-zа-я0-9]+"), "-").trim('-')
        val testRunId = "${safeName}-${LocalDateTime.now().format(formatter)}-${Thread.currentThread().threadId()}"

        val screenshotPath = artifactsRoot.resolve("screenshots").resolve("$testRunId.png")
        val tracePath = artifactsRoot.resolve("traces").resolve("$testRunId.zip")
        val networkLogPath = artifactsRoot.resolve("logs").resolve("$testRunId-network.json")

        listOf(screenshotPath.parent, tracePath.parent, networkLogPath.parent, artifactsRoot.resolve("videos")).forEach {
            Files.createDirectories(it)
        }

        return TestArtifacts(
            testRunId = testRunId,
            rootDir = artifactsRoot,
            screenshotPath = screenshotPath,
            tracePath = tracePath,
            networkLogPath = networkLogPath,
        )
    }
}

