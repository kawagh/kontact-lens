// Sample for observing PIT mutations: Kotlin code tested with Kotest.

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.pitest)
    alias(libs.plugins.detekt)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)

    // Lets PIT run Kotest specs and identify each test case.
    pitest(libs.kotest.extensions.pitest)

    detektPlugins(libs.detekt.rules.ktlint.wrapper)
}

// Kotest runs on the JUnit Platform.
tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// Static analysis and formatting (ktlint rules). Format with ./gradlew detekt --auto-correct
detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
}

pitest {
    pitestVersion = libs.versions.pitest.core
    targetClasses = setOf("io.github.kawagh.kontactlens.sample.*")
    // kontactLens reads the XML report.
    outputFormats = setOf("XML", "HTML")
    timestampedReports = false
}
