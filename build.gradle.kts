// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        //noinspection [ByDesign1.5]LocalDependency
        classpath(files("${layout.buildDirectory.get()}/libs/plugins.jar"))
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}
