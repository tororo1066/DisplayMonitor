import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask.JarUrl

plugins {
    kotlin("jvm") version "1.7.20"
    id("com.github.ben-manes.versions") version "0.41.0"
    id("dev.s7a.gradle.minecraft.server") version "1.2.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    `maven-publish`
}

group = "tororo1066"

val pluginVersion: String by project.ext
val apiVersion: String by project.ext

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/groups/public/")
    maven(url = "https://papermc.io/repo/repository/maven-public/")
    maven(url = "https://libraries.minecraft.net")
    maven(url = "https://jitpack.io")
    maven {
        url = uri("https://maven.pkg.github.com/tororo1066/TororoPluginAPI")
        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

val shadowImplementation: Configuration by configurations.creating
configurations["implementation"].extendsFrom(shadowImplementation)

val shadowAll: Configuration by configurations.creating
configurations["compileOnly"].extendsFrom(shadowAll)

dependencies {
    shadowAll(kotlin("stdlib"))
    compileOnly("io.papermc.paper:paper-api:$pluginVersion-R0.1-SNAPSHOT")
    shadowAll("tororo1066:commandapi:$apiVersion")
    shadowAll("tororo1066:base:$apiVersion")
    shadowImplementation("tororo1066:tororopluginapi:$apiVersion")
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("com.ezylang:EvalEx:3.1.2")
}

tasks.register("shadowNormal", ShadowJar::class) {
    from(sourceSets.main.get().output)
    configurations = listOf(shadowImplementation)
    archiveClassifier.set("")
    exclude("kotlin/**")
    exclude("org/intellij/lang/annotations/**")
    exclude("org/jetbrains/annotations/**")

    relocate("kotlin", "tororo1066.libs.kotlin")
    relocate("org.intellij.lang.annotations", "tororo1066.libs.org.intellij.lang.annotations")
    relocate("org.jetbrains.annotations", "tororo1066.libs.org.jetbrains.annotations")
}

tasks.register("shadowAll", ShadowJar::class) {
    val projectName = project.name.lowercase()
    from(sourceSets.main.get().output)
    configurations = listOf(shadowImplementation, shadowAll)
    archiveClassifier.set("")
    exclude("kotlin/**")
    exclude("org/intellij/lang/annotations/**")
    exclude("org/jetbrains/annotations/**")

    relocate("kotlin", "tororo1066.libs.${projectName}.kotlin")
    relocate("org.intellij.lang.annotations", "tororo1066.libs.${projectName}.org.intellij.lang.annotations")
    relocate("org.jetbrains.annotations", "tororo1066.libs.${projectName}.org.jetbrains.annotations")
    relocate("org.mongodb", "tororo1066.libs.${projectName}.org.mongodb")
    relocate("com.ezylang", "tororo1066.libs.${projectName}.com.ezylang")
}

task<LaunchMinecraftServerTask>("buildAndLaunchServer") {
    val dir = layout.buildDirectory.get().asFile
    dependsOn("build")
    doFirst {
        copy {
            from(dir.resolve("libs/${project.name}.jar"))
            into(dir.resolve("MinecraftServer/plugins"))
        }
    }

    jarUrl.set(JarUrl.Paper(pluginVersion))
    jarName.set("server.jar")
    serverDirectory.set(dir.resolve("MinecraftServer"))
    nogui.set(true)
    agreeEula.set(true)
}

publishing {
    publications {
        create<MavenPublication>("plugin") {
            groupId = project.group.toString()
            artifactId = project.name.lowercase()
            version = project.version.toString()
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/tororo1066/DisplayMonitor")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}