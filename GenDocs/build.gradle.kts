import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "9.1.0"
    application
}

val pluginVersion: String by project.ext
val apiVersion: String by project.ext

group = "tororo1066"

repositories {
    mavenCentral()
    maven(url = "https://repo.onarandombox.com/dumptruckman-snapshots")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(rootProject)
    implementation(project(":DisplayMonitorAPI"))
    implementation("tororo1066:tororopluginapi:$apiVersion")
    implementation("io.papermc.paper:paper-api:$pluginVersion-R0.1-SNAPSHOT")
    implementation("com.google.code.gson:gson:2.12.1")

}
kotlin {
    jvmToolchain(17)
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
}

//メインクラスの指定
application {
    mainClass.set("tororo1066.displaymonitor.GenerateDocData")
}