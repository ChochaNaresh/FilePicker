import com.android.build.api.dsl.ManagedVirtualDevice
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("io.gitlab.arturbosch.detekt")
    id("maven-publish")
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
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {

    // core
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Coil
    implementation("io.coil-kt:coil:2.4.0")

    // testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.5")

    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")

    androidTestImplementation("com.google.truth:truth:1.1.5")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
}
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.github.ChochaNaresh"
            artifactId = "FilePicker"
            version = "0.0.1"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

detekt {
    toolVersion = "1.23.1"
    config.setFrom("$projectDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}
