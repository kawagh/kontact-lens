// Gradle plugin that generates the kontactLens report from the PIT results of the project it is applied to.

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    alias(libs.plugins.detekt)

    // API documentation from KDoc.
    alias(libs.plugins.dokka)

    // Checks the licenses of the runtime dependencies (runs as part of ./gradlew check).
    alias(libs.plugins.licensee)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":kontact-lens"))

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)

    detektPlugins(libs.detekt.rules.ktlint.wrapper)
}

gradlePlugin {
    plugins {
        create("kontactLens") {
            id = "io.github.kawagh.kontactlens"
            implementationClass = "io.github.kawagh.kontactlens.gradle.KontactLensPlugin"
            displayName = "kontactLens"
            description = "Renders PIT mutation testing results as an HTML report next to the source code."
        }
    }
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

// Licenses allowed for the dependencies passed on to users. Kotlin's standard library is Apache-2.0.
licensee {
    allow("Apache-2.0")
}
