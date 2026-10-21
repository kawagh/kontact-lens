// Sample for observing PIT mutations: Java code tested with JUnit.

plugins {
    java
    alias(libs.plugins.pitest)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
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
