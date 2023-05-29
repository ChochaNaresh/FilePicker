plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("io.gitlab.arturbosch.detekt")
    id("maven-publish")
}

android {
    namespace = "com.nareshchocha.filepickerlibrary"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
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

    // flex layout
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // Coil
    implementation("io.coil-kt:coil:2.2.2")

    // testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
publishing {
    (publications) {
        create<MavenPublication>("release") {
            groupId = "com.github.ChochaNaresh"
            artifactId = "FilePicker"
            version = "${project.version}"
            // from(components["release"])
        }
        /*release(MavenPublication) {
            groupId = "com.github.ChochaNaresh"
            artifactId = "FilePicker"
            version = "0.0.1"

            afterEvaluate {
                from = components . release
            }
        }*/
    }
}

detekt {
    toolVersion = "1.22.0"
    config = files("config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}
