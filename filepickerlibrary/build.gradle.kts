import com.android.build.api.dsl.ManagedVirtualDevice
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.arturbosch.detekt)
    alias(libs.plugins.maven.publish)
    id("kotlin-parcelize")
    id("maven-publish")
    id("signing")
}

android {
    namespace = "com.nareshchocha.filepickerlibrary"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = false
        managedDevices {
            devices {

                maybeCreate<ManagedVirtualDevice>("pixel4api27").apply {
                    // Use device profiles you typically see in Android Studio.
                    device = "Pixel 4"
                    // Use only API levels 27 and higher.
                    apiLevel = 27
                    // To include Google services, use "google".
                    systemImageSource = "google"
                } // ./gradlew pixel4api27debugAndroidTest

                maybeCreate<ManagedVirtualDevice>("pixel4api28").apply {
                    // Use device profiles you typically see in Android Studio.
                    device = "Pixel 4"
                    // Use only API levels 27 and higher.
                    apiLevel = 28
                    // To include Google services, use "google".
                    systemImageSource = "google"
                } // ./gradlew pixel4api28debugAndroidTest
            }
            groups {
                maybeCreate("phoneAndTablet").apply {
                    targetDevices.add(devices["pixel4api27"])
                    targetDevices.add(devices["pixel4api28"])
                } // ./gradlew phoneAndTabletGroupdebugAndroidTest
            }
        }
    }

}

dependencies {

    // core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // timber
    implementation(libs.timber)

    // Coil
    implementation(libs.coil)
    implementation(libs.androidx.startup.runtime)

    // testing
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.androidx.espresso.contrib)
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.S01, true)
    signAllPublications()
}

detekt {
    toolVersion = "1.23.1"
    config.setFrom("$projectDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}

mavenPublishing {
    coordinates("io.github.chochanaresh", "filepicker", "0.3.3")

    pom {
        name.set("filepicker")
        description.set("All file and media picker library for android. This library is designed to simplify the process of selecting and retrieving media files from an Android device, and supports media capture for images and videos.")
        inceptionYear.set("2023")
        url.set("https://github.com/ChochaNaresh/FilePicker")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("ChochaNaresh")
                name.set("Naresh Chocha")
                url.set("https://github.com/ChochaNaresh")
            }
        }
        scm {
            url.set("https://github.com/ChochaNaresh/FilePicker")
            connection.set("scm:git:git://github.com/ChochaNaresh/FilePicker.git")
            developerConnection.set("scm:git:ssh://git@github.com/ChochaNaresh/FilePicker.git")
        }
    }
}
