import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
}
val keystoreProperties =
    Properties().apply {
        val keystorePropertiesFile = rootProject.file("release-keystore.properties")
        if (keystorePropertiesFile.exists()) {
            load(keystorePropertiesFile.inputStream())
        }
    }

val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            load(localPropertiesFile.inputStream())
        }
    }

// Helper: local.properties → System env → fallback
fun adProperty(
    key: String,
    fallback: String
): String =
    localProperties.getProperty(key)
        ?: System.getenv(key)
        ?: fallback

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

        // AdMob IDs: local.properties → env var → test IDs
        val admobAppId = adProperty("ADMOB_APP_ID", "ca-app-pub-3940256099942544~3347511713")
        val admobBannerId = adProperty("ADMOB_BANNER_AD_UNIT_ID", "ca-app-pub-3940256099942544/9214589741")
        val admobInterstitialId = adProperty("ADMOB_INTERSTITIAL_AD_UNIT_ID", "ca-app-pub-3940256099942544/1033173712")
        val admobAppOpenId = adProperty("ADMOB_APP_OPEN_AD_UNIT_ID", "ca-app-pub-3940256099942544/9257395921")

        manifestPlaceholders["ADMOB_APP_ID"] = admobAppId
        buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"$admobBannerId\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", "\"$admobInterstitialId\"")
        buildConfigField("String", "ADMOB_APP_OPEN_AD_UNIT_ID", "\"$admobAppOpenId\"")
    }

    packaging {
        jniLibs {
            excludes.add("lib/arm64-v8a/libandroidx.graphics.path.so")
            excludes.add("lib/armeabi-v7a/libandroidx.graphics.path.so")
            excludes.add("lib/x86/libandroidx.graphics.path.so")
            excludes.add("lib/x86_64/libandroidx.graphics.path.so")
        }
    }
    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
            storeFile = keystoreProperties["storeFile"]?.let { file(it as String) }
            storePassword = keystoreProperties["storePassword"] as String?

            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
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
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        aidl = false
        buildConfig = true
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

    // ads
    implementation(libs.play.services.ads)

    // user consent (GDPR / UMP) for EEA, UK, Switzerland
    implementation(libs.user.messaging.platform)

    // lifecycle (for App Open ads)
    implementation(libs.androidx.lifecycle.process)

    // FilePicker library
    implementation(project(":filepickerlibrary"))

    // testing compose
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
