// Declares the plugins used by the subprojects once, so that each plugin is loaded only once for the whole build.
// Without this, the Kotlin Gradle plugin is loaded separately in each subproject, which it does not support.

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.pitest) apply false
    alias(libs.plugins.detekt) apply false
}
