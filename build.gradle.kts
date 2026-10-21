// Declares the plugins used by the subprojects once, so that each plugin is loaded only once for the whole build.
// Without this, the Kotlin Gradle plugin is loaded separately in each subproject, which it does not support.

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.pitest) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.licensee) apply false

    // Aggregates the API documentation of the modules below: ./gradlew dokkaGenerate (build/dokka/html)
    alias(libs.plugins.dokka)
}

// Dokka downloads its own components from here.
repositories {
    mavenCentral()
}

dokka {
    moduleName = "kontactLens"
}

dependencies {
    dokka(project(":kontact-lens"))
    dokka(project(":kontact-lens-gradle-plugin"))
}
