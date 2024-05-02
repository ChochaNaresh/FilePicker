plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nareshchocha.filepicker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nareshchocha.filepicker"
        minSdk = 21
        targetSdk = 34
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
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.13.1")

    // File Picker
     implementation(project(":filepickerlibrary"))

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
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
}
