plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.serialization)
}


dependencies {
    compileOnly(gradleApi())
    compileOnly(libs.kspApi)
    implementation(project(":api"))
//    compileOnly(libs.android.gradlePlugin)
//    compileOnly(libs.android.tools.common)
    implementation(libs.serialization.core)
    implementation(libs.serialization.json)

}