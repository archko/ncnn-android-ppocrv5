plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.tencent.ppocrv5ncnn"
    compileSdk = 36
    ndkVersion = "29.0.13846066"

    defaultConfig {
        applicationId = "com.tencent.ppocrv5ncnn"

        minSdk = 24
        targetSdk = 35
        ndk.abiFilters.add("arm64-v8a")

        externalNativeBuild {
            cmake {
                arguments.add("-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON")
            }
        }
    }

    externalNativeBuild {
        cmake {
            version = "3.22.1"
            path = file("src/main/jni/CMakeLists.txt")
        }
    }

    dependencies {
        implementation(libs.kotlin.stdlib)
        implementation(libs.androidx.appcompat) {
            exclude(group = "org.jetbrains.kotlin")
        }
    }

    packaging {
        jniLibs {
            //useLegacyPackaging = true
        }
    }
}
