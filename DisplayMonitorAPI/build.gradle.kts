plugins {
    id("java")
    `maven-publish`
    `java-library`
}

group = "tororo1066"

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.javadoc {
    options.encoding = "UTF-8"
}

publishing {
    publications {
        create<MavenPublication>("api") {
            groupId = project.group.toString()
            artifactId = "display-monitor-api"
            version = System.getenv("VERSION")
            from(components["java"])
            artifact(tasks["javadocJar"])
            artifact(tasks["sourcesJar"])
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