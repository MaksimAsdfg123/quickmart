plugins {
    kotlin("jvm")
    id("org.gradle.test-retry")
}

group = "com.quickmart"
version = "0.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("com.microsoft.playwright:playwright:1.49.0")
    testImplementation("io.qameta.allure:allure-junit5:2.29.1")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.16")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

sourceSets {
    test {
        kotlin.setSrcDirs(
            listOf(
                "shared/kotlin",
                "suites/kotlin",
            ),
        )
        resources.setSrcDirs(
            listOf(
                "resources",
                "../config",
            ),
        )
    }
}

val isCi = providers.environmentVariable("CI").isPresent
val retryCount = if (isCi) 2 else 1

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

    retry {
        maxRetries.set(retryCount)
        maxFailures.set(20)
        failOnPassedAfterRetry.set(false)
    }

    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showStackTraces = true
    }

    systemProperty("file.encoding", "UTF-8")
    systemProperty("allure.results.directory", layout.buildDirectory.dir("allure-results").get().asFile.absolutePath)
}

tasks.named<ProcessResources>("processTestResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Test>("uiTest") {
    description = "Runs UI test suite (JUnit tag: ui)."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform {
        includeTags("ui")
    }
}

tasks.register<JavaExec>("installUiBrowsers") {
    description = "Installs Playwright browsers required by UI tests."
    group = "verification"
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("com.microsoft.playwright.CLI")
    args("install", "chromium", "firefox")
}
