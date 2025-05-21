// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    // compose
    alias(libs.plugins.kotlin.compose) apply false
    // parcelize
    alias(libs.plugins.kotlin.parcelize) apply false
    // code analyzer for Kotlin
    alias(libs.plugins.arturbosch.detekt) apply false
    alias(libs.plugins.maven.publish) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
