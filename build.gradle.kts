plugins {
    base
    kotlin("jvm") version "1.9.25" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
    kotlin("plugin.jpa") version "1.9.25" apply false
    id("org.springframework.boot") version "3.3.4" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    id("org.gradle.test-retry") version "1.6.4" apply false
}

tasks.register("apiTest") {
    group = "verification"
    description = "Runs API automation suite."
    dependsOn(":api-tests:apiTest")
}

tasks.register("uiTest") {
    group = "verification"
    description = "Runs UI automation suite."
    dependsOn(":ui-tests:uiTest")
}

tasks.register("uiTestHeaded") {
    group = "verification"
    description = "Runs UI automation suite in headed mode."
    dependsOn(":ui-tests:uiTestHeaded")
}

tasks.register("uiTestDebug") {
    group = "verification"
    description = "Runs UI automation suite in headed debug mode with Playwright trace in console."
    dependsOn(":ui-tests:uiTestDebug")
}

tasks.register("installUiBrowsers") {
    group = "verification"
    description = "Installs Playwright browsers for UI tests."
    dependsOn(":ui-tests:installUiBrowsers")
}
