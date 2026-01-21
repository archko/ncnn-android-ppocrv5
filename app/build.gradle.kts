plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.tencent.ppocrv5ncnn.test"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tencent.ppocrv5ncnn"

        minSdk = 24
        targetSdk = 35
        ndk.abiFilters.add("arm64-v8a")
    }

    dependencies {
        implementation(libs.kotlin.stdlib)
        implementation(libs.androidx.appcompat) {
            exclude(group = "org.jetbrains.kotlin")
        }
        api(project(":ncnn"))
    }
}
