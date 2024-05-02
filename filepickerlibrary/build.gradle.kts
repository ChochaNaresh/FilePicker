import com.android.build.api.dsl.ManagedVirtualDevice
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("io.gitlab.arturbosch.detekt")
    id("com.vanniktech.maven.publish") // NEW
    id("maven-publish")
    id("signing")
}

android {
    namespace = "com.nareshchocha.filepickerlibrary"
    compileSdk = 34

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
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Coil
    implementation("io.coil-kt:coil:2.4.0")
    implementation("androidx.startup:startup-runtime:1.1.1")

    // testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.5")

    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")

    androidTestImplementation("com.google.truth:truth:1.1.5")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
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
    coordinates("io.github.chochanaresh", "filepicker", "0.2.5")

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
