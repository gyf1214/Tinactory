import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("tinactory-common")
    id("org.jetbrains.kotlin.jvm")
}

evaluationDependsOn(":mod")

base {
    archivesName = "tinactory_datagen"
}

neoForge {
    runs {
        create("data") {
            data()
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
            jvmArgument("-ea")

            ideName = "${rootProject.name}.${project.name}.main"
            gameDirectory = rootProject.file("run/data")
            sourceSet = sourceSets.main.get()
            programArguments.addAll(
                "--mod",
                "tinactory_datagen",
                "--all",
                "--output",
                rootProject.project(":mod").file("src/generated/resources/").absolutePath,
            )
            programArguments.addAll("--existing", rootProject.project(":mod").file("src/main/resources/").absolutePath)
            programArguments.addAll("--existing", file("src/main/resources/").absolutePath)
            programArguments.addAll(
                "--existing",
                rootProject.file("libs/tinactory_extra_resources_${property("extra_resources_version")}.zip").absolutePath,
            )
        }
    }

    mods {
        create("tinactory") {
            sourceSet(project(":mod").extensions.getByType<SourceSetContainer>().named("main").get())
        }
        create("tinactory_datagen") {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    compileOnly("org.shsts.tinycorelib:core:${property("minecraft_version")}-${property("tinycorelib_version")}:api")
    compileOnly("org.shsts.tinycorelib:datagen:${property("minecraft_version")}-${property("tinycorelib_version")}:api")
    runtimeOnly("org.shsts.tinycorelib:core:${property("minecraft_version")}-${property("tinycorelib_version")}")
    runtimeOnly("org.shsts.tinycorelib:datagen:${property("minecraft_version")}-${property("tinycorelib_version")}")

    implementation(project(":mod"))

    implementation("thedarkcolour:kotlinforforge-neoforge:${property("kff_version")}")
    implementation(rootProject.files("libs/tinactory_extra_resources_${property("extra_resources_version")}.zip"))

    implementation("dev.architectury:architectury-neoforge:${property("architectury_version")}")
    implementation("dev.ftb.mods:ftb-library-neoforge:${property("ftb_library_version")}")
    runtimeOnly("dev.ftb.mods:ftb-filter-system-neoforge:${property("ftb_filter_system_version")}")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

checkSource {
    topPackage("org.shsts.tinactory")
    includeKotlin()
}

tasks.register<JavaExec>("extractQuestLanguage") {
    group = "tinactory"
    description = "Extract FTB Quests chapter text into language metadata."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "org.shsts.tinactory.datagen.quest.QuestLanguageExtractor"
    workingDir = rootProject.file("run/questLanguage")
    systemProperty("tinactory.projectRoot", rootProject.projectDir.absolutePath)
    doFirst {
        workingDir.mkdirs()
    }
    args("extract")
}

tasks.register<JavaExec>("checkQuestLanguage") {
    group = "verification"
    description = "Check FTB Quests chapter text language metadata synchronization."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "org.shsts.tinactory.datagen.quest.QuestLanguageExtractor"
    workingDir = rootProject.file("run/questLanguage")
    systemProperty("tinactory.projectRoot", rootProject.projectDir.absolutePath)
    doFirst {
        workingDir.mkdirs()
    }
    args("check")
}
