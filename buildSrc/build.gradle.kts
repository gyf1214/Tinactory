import java.util.Properties

plugins {
    `kotlin-dsl`
}

val rootProperties = Properties().apply {
    file("../gradle.properties").inputStream().use(::load)
}

repositories {
    maven {
        name = "shsts"
        url = uri("https://www.shsts.org/m2")
    }
    gradlePluginPortal()
    mavenCentral()
    maven {
        name = "NeoForged"
        url = uri("https://maven.neoforged.net/releases")
        content {
            includeGroup("net.neoforged")
            includeGroup("net.neoforged.moddev")
        }
    }
    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
    }
}

dependencies {
    implementation("net.neoforged:moddev-gradle:${rootProperties.getProperty("moddev_version")}")
    implementation("org.shsts.checksource:plugin:${rootProperties.getProperty("checksource_version")}")
}
