plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.library)
}

android {
    namespace = "cn.li.router"

    defaultConfig {
        minSdk = 24
        compileSdk = 35
        version = 0
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain(11)
    }
}

dependencies {
    api(project(":api"))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
}