import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.plugin.DetektPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.arturbosch.detekt) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.spotless) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

val detektVersion = libs.versions.detekt.get()

subprojects {
    configureDetektAndSpotless()

    afterEvaluate {
        tasks.withType<KotlinCompile> {
            finalizedBy("spotlessApply")
        }
        tasks.withType<KotlinCompile> {
            finalizedBy("detekt")
        }
    }
}

fun Project.configureDetektAndSpotless() {
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

    apply<DetektPlugin>()
    configure<DetektExtension> {
        config.from("$rootDir/config/detekt/detekt.yml")
        buildUponDefaultConfig = true
    }

    tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
        reports {
            checkstyle.required.set(false)
            sarif.required.set(false)
            markdown.required.set(false)
            html.required.set(true)
        }
    }
}
