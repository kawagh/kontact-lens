// Gradle plugin that generates the kontactLens report from the PIT results of the project it is applied to.

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    alias(libs.plugins.detekt)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))

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
