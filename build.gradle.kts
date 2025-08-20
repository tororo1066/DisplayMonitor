import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.7.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    `maven-publish`
    `java-library`
}

group = "tororo1066"

val pluginVersion: String by project.ext
val apiVersion: String by project.ext

allprojects {
    apply(plugin = "java")

    java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

    repositories {
        mavenCentral()
        maven(url = "https://repo.papermc.io/repository/maven-public/")

        maven {
            url = uri("https://maven.pkg.github.com/tororo1066/TororoPluginAPI")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:$pluginVersion-R0.1-SNAPSHOT")
    }
}

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/groups/public/")
    maven(url = "https://libraries.minecraft.net")
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.onarandombox.com/dumptruckman-snapshots")
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly("tororo1066:commandapi:$apiVersion")
    compileOnly("tororo1066:base:$apiVersion")
    shadow("tororo1066:tororopluginapi:$apiVersion")
    compileOnly("com.mojang:brigadier:1.0.18")
    shadow(project(":DisplayMonitorAPI"))

    shadow("com.dumptruckman.minecraft:JsonConfiguration:1.2-SNAPSHOT")
    shadow("net.minidev:json-smart:2.5.2")
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    configurations = listOf(project.configurations.getByName("shadow"))
}

tasks.named("build") {
    dependsOn("shadowJar")
}


tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.named("javadoc"))
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
    skip()
}

publishing {
    publications {
        create<MavenPublication>("plugin") {
            groupId = project.group.toString()
            artifactId = "display-monitor-plugin"
            version = System.getenv("VERSION")
            from(components["java"])
            artifact(tasks.named("javadocJar"))
            artifact(tasks.named("sourcesJar"))
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