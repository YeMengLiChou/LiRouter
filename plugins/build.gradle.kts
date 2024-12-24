plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(libs.ksp.gradlePlugin)
    implementation(libs.android.gradlePlugin)
    implementation(libs.asm)
    implementation(libs.asm.commons)
}

gradlePlugin {
    plugins.create("LiRouterPlugin") {
        id = "li-router"
        implementationClass = "cn.li.router.plugins.LiRouterPlugin"
    }
}

publishing {
    publications {
        create<MavenPublication>("LiRouter") {
            artifact("build/libs/plugins.jar")
            artifactId = "li-router-plugin"
            groupId = "cn.li.router"
            version = "0.0.1"
        }
    }
    repositories {
        maven {
            url = uri("../repo")
        }
    }
}

task<Copy>("buildPlugin") {
    from("${layout.buildDirectory.get()}/libs/plugins.jar")
    into("${rootProject.layout.buildDirectory.get()}/libs/")
    dependsOn("jar")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

afterEvaluate {
    tasks.getByName("publishLiRouterPublicationToMavenRepository").mustRunAfter(":plugins:jar")
}
