plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = AppConfig.Sample.NAMESPACE
    compileSdk = Versions.COMPILE_SDK

    defaultConfig {
        applicationId = AppConfig.Sample.APPLICATION_ID
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK
        versionCode = AppConfig.Sample.VERSION_CODE
        versionName = AppConfig.Sample.VERSION_NAME
        testInstrumentationRunner = AppConfig.TEST_INSTRUMENTATION_RUNNER
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
        shaders = false
    }

    compileOptions {
        sourceCompatibility = CompileOptions.SOURCE_COMPATIBILITY
        targetCompatibility = CompileOptions.TARGET_COMPATIBILITY
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

    // landscapist
    implementation(libs.skydoves.landscapist.coil3)

    // FilePicker library
    implementation(project(":filepickerlibrary"))

    // testing compose
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
