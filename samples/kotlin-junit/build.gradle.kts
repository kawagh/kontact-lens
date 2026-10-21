// Sample for observing PIT mutations: Kotlin code tested with JUnit.

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.pitest)
    alias(libs.plugins.detekt)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    detektPlugins(libs.detekt.rules.ktlint.wrapper)
}

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
    // Lets PIT run JUnit Platform tests.
    junit5PluginVersion = libs.versions.pitest.junit5.plugin
    targetClasses = setOf("io.github.kawagh.kontactlens.sample.*")
    // kontactLens reads the XML report.
    outputFormats = setOf("XML", "HTML")
    timestampedReports = false
}
