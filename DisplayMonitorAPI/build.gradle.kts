plugins {
    id("java")
    `maven-publish`
}

group = "tororo1066"

publishing {
    publications {
        create<MavenPublication>("plugin") {
            groupId = project.group.toString()
            artifactId = "display-monitor-api"
            version = System.getenv("VERSION")
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