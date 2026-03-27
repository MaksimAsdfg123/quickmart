plugins {
    base
    kotlin("jvm") version "1.9.25" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
    kotlin("plugin.jpa") version "1.9.25" apply false
    id("org.springframework.boot") version "3.3.4" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    id("org.gradle.test-retry") version "1.6.4" apply false
}

tasks.register("uiTest") {
    group = "verification"
    description = "Runs UI automation suite."
    dependsOn(":ui-tests:uiTest")
}

tasks.register("installUiBrowsers") {
    group = "verification"
    description = "Installs Playwright browsers for UI tests."
    dependsOn(":ui-tests:installUiBrowsers")
}
