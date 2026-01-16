plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.signing)
}

val versionName = project.findProperty("VERSION_NAME") as String? ?: "0.0.1"
android {
    namespace = "com.nareshchocha.filepickerlibrary"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
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
        compose = true
        aidl = false
        buildConfig = false
        shaders = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.valueOf(libs.versions.jdkVersion.get())
        targetCompatibility = JavaVersion.valueOf(libs.versions.jdkVersion.get())
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

    // testing compose
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
val automaticRelease: Boolean = true
mavenPublishing {
    publishToMavenCentral(automaticRelease)
    signAllPublications()
    coordinates("io.github.chochanaresh", "filepicker", versionName)

    pom {
        name.set("filepicker")
        description.set(
            "All file and media picker library for android. This library is designed to simplify the process of selecting and retrieving media files from an Android device, and supports media capture for images and videos."
        )
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
