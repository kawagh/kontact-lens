// kontactLens: reads PIT reports and renders them as an HTML report.

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)

    // Apply the java-library plugin for API and implementation separation.
    `java-library`

    // Apply the PIT plugin for mutation testing.
    alias(libs.plugins.pitest)

    alias(libs.plugins.detekt)

    // API documentation from KDoc.
    alias(libs.plugins.dokka)
}

repositories {
    // Use Maven Central for resolving dependencies.
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
    // Run the tests on another JDK with -PtestJavaVersion=21 (used by the CI matrix).
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = providers.gradleProperty("testJavaVersion").map(JavaLanguageVersion::of)
            .orElse(JavaLanguageVersion.of(17))
    }
}

// Supported range: Gradle 9.0+ running on JDK 17+.
// Build for Java 17, and use the Kotlin API of Gradle 9.0's embedded Kotlin (2.2) because the code runs inside Gradle.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    compilerOptions {
        apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2
    }
}

// Static analysis and formatting (ktlint rules). Format with ./gradlew detekt --auto-correct
detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
}

pitest {
    pitestVersion = libs.versions.pitest.core
    targetClasses = setOf("io.github.kawagh.kontactlens.*")
    // kontactLens itself reads the XML report.
    outputFormats = setOf("XML", "HTML")
    timestampedReports = false
}

// Generates kontactLens reports from the PIT results of the samples, e.g. ./gradlew sampleReports
val sampleReports by tasks.registering {
    group = "kontactlens"
    description = "Generates kontactLens reports for all samples."
}

for (sample in listOf("kotlin-kotest", "kotlin-junit", "java-junit")) {
    val sampleDir = rootProject.layout.projectDirectory.dir("samples/$sample")
    val output = layout.buildDirectory.file("reports/kontactlens/$sample.html")
    val taskName = "sampleReport" + sample.split('-').joinToString("") { it.replaceFirstChar(Char::uppercase) }

    val report = tasks.register<JavaExec>(taskName) {
        group = "kontactlens"
        description = "Generates the kontactLens report for samples/$sample."
        dependsOn(":samples:$sample:pitest")
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass = "io.github.kawagh.kontactlens.MainKt"
        args(
            sampleDir.file("build/reports/pitest/mutations.xml").asFile.path,
            output.get().asFile.path,
            sampleDir.dir("src/main/kotlin").asFile.path,
            sampleDir.dir("src/main/java").asFile.path,
        )
        outputs.file(output)
    }
    sampleReports { dependsOn(report) }
}
