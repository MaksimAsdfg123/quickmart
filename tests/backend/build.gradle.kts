plugins {
    kotlin("jvm")
}

group = "com.quickmart"
version = "0.1.0"

val isCi = providers.environmentVariable("CI").isPresent
val traceToConsoleDefault = !isCi

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.4"))
    testImplementation(project(":backend"))
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("io.rest-assured:rest-assured:5.5.1")
    testImplementation("io.rest-assured:kotlin-extensions:5.5.1")
    testImplementation("io.rest-assured:json-path:5.5.1")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.2")
    testImplementation("com.h2database:h2")
    testImplementation("org.awaitility:awaitility-kotlin:4.2.1")
    testImplementation("io.qameta.allure:allure-junit5:2.29.1")
    testImplementation("io.qameta.allure:allure-rest-assured:2.29.1")
    testImplementation("org.assertj:assertj-core:3.26.3")
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

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events(
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT,
            org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR,
        )
        showStandardStreams = true
        showExceptions = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    reports {
        junitXml.required.set(true)
        junitXml.isOutputPerTestCase = true
        html.required.set(true)
    }

    systemProperty("file.encoding", "UTF-8")
    systemProperty("allure.results.directory", layout.buildDirectory.dir("allure-results").get().asFile.absolutePath)
    systemProperty(
        "api.http.trace.console",
        providers.environmentVariable("API_HTTP_TRACE_CONSOLE").orNull ?: traceToConsoleDefault.toString(),
    )
}

tasks.named<ProcessResources>("processTestResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("kafka", "cache", "wiremock")
    }
}

tasks.register<Test>("apiTest") {
    description = "Runs API automation suite (JUnit tag: api)."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    useJUnitPlatform {
        includeTags("api")
    }
}

tasks.register<Test>("apiSmokeTest") {
    description = "Runs API smoke suite (JUnit tag: smoke)."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    useJUnitPlatform {
        includeTags("smoke")
    }
}

tasks.register<Test>("kafkaTest") {
    description = "Runs Kafka component suite (JUnit tag: kafka)."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    useJUnitPlatform {
        includeTags("kafka")
    }
}

tasks.register<Test>("cacheTest") {
    description = "Runs cache component suite (JUnit tag: cache)."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    useJUnitPlatform {
        includeTags("cache")
    }
}

tasks.register<Test>("wiremockTest") {
    description = "Runs WireMock-backed external integration suite (JUnit tag: wiremock)."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    useJUnitPlatform {
        includeTags("wiremock")
    }
}
