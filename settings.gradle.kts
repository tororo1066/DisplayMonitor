//plugins {
//    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
//}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "DisplayMonitor"
include("DisplayMonitorAPI")
include("GenDocs")