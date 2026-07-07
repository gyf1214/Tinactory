import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    checkstyle
    eclipse
    id("net.neoforged.moddev")
    id("org.shsts.checksource")
    `maven-publish`
}

group = "org.shsts.tinactory"
version = "${property("minecraft_version")}-${property("mod_version")}"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

checkstyle {
    toolVersion = property("checkstyle_version").toString()
}

tasks.withType<Checkstyle>().configureEach {
    classpath = files()
}

neoForge {
    version = property("neo_version").toString()
    parchment {
        minecraftVersion = property("minecraft_version").toString()
        mappingsVersion = property("parchment_version").toString()
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 21
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

val modMetadataProperties = mapOf(
    "mod_version" to version,
)

val generateModMetadata by tasks.registering(ProcessResources::class) {
    inputs.properties(modMetadataProperties)
    expand(modMetadataProperties)
    from("src/main/templates")
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}

sourceSets.main {
    resources.srcDir(generateModMetadata)
}

neoForge.ideSyncTask(generateModMetadata)

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn(tasks.classes)
    archiveClassifier = "sources"
    from(sourceSets.main.get().allSource)
}

tasks.build {
    dependsOn(sourcesJar)
}

publishing {
    repositories {
        if (project.findProperty("shstsUser") != null) {
            maven {
                name = "shsts"
                url = uri("https://www.shsts.org/m2")
                credentials {
                    username = project.findProperty("shstsUser").toString()
                    password = project.findProperty("shstsPassword")?.toString() ?: ""
                }
            }
        }
    }
}
