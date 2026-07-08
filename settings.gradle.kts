pluginManagement {
    val moddevVersion = providers.gradleProperty("moddev_version").get()
    val kotlinVersion = providers.gradleProperty("kotlin_version").get()
    val checksourceVersion = providers.gradleProperty("checksource_version").get()

    repositories {
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
        maven {
            name = "shsts"
            url = uri("https://www.shsts.org/m2")
        }
    }
    plugins {
        id("net.neoforged.moddev") version moddevVersion
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.shsts.checksource") version checksourceVersion
    }
}

rootProject.name = "Tinactory"

include("mod")
include("datagen")
