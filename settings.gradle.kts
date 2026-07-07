pluginManagement {
    val moddevVersion = providers.gradleProperty("moddev_version").get()
    val kotlinVersion = providers.gradleProperty("kotlin_version").get()
    val checksourceVersion = providers.gradleProperty("checksource_version").get()

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
    plugins {
        id("net.neoforged.moddev") version moddevVersion
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.shsts.checksource") version checksourceVersion
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        maven {
            name = "Mojang Minecraft Libraries"
            url = uri("https://libraries.minecraft.net")
        }
        maven {
            name = "Mojang Meta"
            url = uri("https://maven.neoforged.net/mojang-meta/")
            metadataSources {
                gradleMetadata()
                mavenPom()
            }
        }
        maven {
            name = "NeoForged"
            url = uri("https://maven.neoforged.net/releases")
        }
        maven {
            name = "ParchmentMC"
            url = uri("https://maven.parchmentmc.org")
        }
        maven {
            name = "Jared's maven"
            url = uri("https://maven.blamejared.com/")
        }
        maven {
            name = "FTB"
            url = uri("https://maven.ftb.dev/releases")
        }
        maven {
            name = "Architectury"
            url = uri("https://maven.architectury.dev")
        }
        maven {
            url = uri("https://cursemaven.com")
            content {
                includeGroup("curse.maven")
            }
        }
        maven {
            name = "Kotlin for Forge"
            url = uri("https://thedarkcolour.github.io/KotlinForForge/")
        }
        maven {
            name = "shsts"
            url = uri("https://www.shsts.org/m2")
        }
    }
}

rootProject.name = "Tinactory"

include("mod")
include("datagen")
