import org.gradle.api.JavaVersion

object Versions {
    const val COMPILE_SDK = 37
    const val TARGET_SDK = 37
    const val MIN_SDK = 23
}

object CompileOptions {
    val SOURCE_COMPATIBILITY = JavaVersion.VERSION_24
    val TARGET_COMPATIBILITY = JavaVersion.VERSION_24
}

object AppConfig {
    const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"

    object Library {
        const val NAMESPACE = "com.nareshchocha.filepickerlibrary"
    }

    object Sample {
        const val NAMESPACE = "com.nareshchocha.filepicker"
        const val APPLICATION_ID = "com.nareshchocha.filepicker"
        const val VERSION_CODE = 4
        const val VERSION_NAME = "0.7.0"
    }
}
