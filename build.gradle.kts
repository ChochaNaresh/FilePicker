import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

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
    alias(libs.plugins.spotless) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
val detektVersion = libs.versions.detekt.get()

subprojects {
    // formatting code for all subprojects
    apply<SpotlessPlugin>()
    configure<SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            targetExclude("build/**/*.kt")
            ktlint()
            endWithNewline()
        }
        kotlinGradle {
            target("*.kts")
            ktlint()
        }
    }
    // code analysis for all subprojects
    apply<DetektPlugin>()
    configure<DetektExtension> {
        toolVersion = detektVersion
        config.from("$rootDir/config/detekt/detekt.yml")
        buildUponDefaultConfig = true
    }

    afterEvaluate {
        tasks.withType<KotlinCompile> {
            finalizedBy("spotlessApply")
            finalizedBy("detekt")
        }
    }
}
