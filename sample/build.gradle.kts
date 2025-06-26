import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.nareshchocha.filepicker"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.nareshchocha.filepicker"
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()
        versionCode = 4
        versionName = "0.7.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        jniLibs {
            excludes.add("lib/arm64-v8a/libandroidx.graphics.path.so")
            excludes.add("lib/armeabi-v7a/libandroidx.graphics.path.so")
            excludes.add("lib/x86/libandroidx.graphics.path.so")
            excludes.add("lib/x86_64/libandroidx.graphics.path.so")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isJniDebuggable = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        aidl = false
        buildConfig = false
        renderScript = false
        shaders = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.valueOf(libs.versions.jdkVersion.get())
        targetCompatibility = JavaVersion.valueOf(libs.versions.jdkVersion.get())
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}

dependencies {
    implementation(libs.core.splashscreen)
    implementation(libs.androidx.activity.ktx)
    // compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    implementation("io.coil-kt.coil3:coil-compose:3.2.0")

    // File Picker
    implementation(project(":filepickerlibrary"))
    // implementation(libs.androidx.lifecycle.runtime.ktx)

    // testing compose
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
