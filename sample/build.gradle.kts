plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.nareshchocha.filepicker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nareshchocha.filepicker"
        minSdk = 21
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments.putAll(
            mapOf(
                "clearPackageData" to "true",
                /*"coverage" to "true",
                "disableAnalytics" to "true",
                "useTestStorageService" to "false",
                "numShards" to numShards,
                "shardIndex" to shardIndex*/
            ),
        )
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
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = false
        managedDevices {
            devices {

                maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel4api27").apply {
                    // Use device profiles you typically see in Android Studio.
                    device = "Pixel 4"
                    // Use only API levels 27 and higher.
                    apiLevel = 27

                    // To include Google services, use "google".
                    systemImageSource = "google"
                } // ./gradlew pixel4api27debugAndroidTest

                maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel4api28").apply {
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

    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    // core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // File Picker
     implementation(project(":filepickerlibrary"))

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
