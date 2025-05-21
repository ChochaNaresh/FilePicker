import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.arturbosch.detekt)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.signing)
    // id("maven-publish")
    // id("signing")
}

android {
    namespace = "com.nareshchocha.filepickerlibrary"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
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

    kotlinOptions {
        jvmTarget = JavaVersion.valueOf(libs.versions.jdkVersion.get()).toString()
    }
}

dependencies {

    // compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)


    // testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // testing compose
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)




    // Old code
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
