import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("tinactory-common")
    id("org.jetbrains.kotlin.jvm")
}

evaluationDependsOn(":mod")

val extraResources = "libs/tinactory_extra_resources_${property("extra_resources_version")}.zip"

neoForge {
    mods {
        create("tinactory") {
            sourceSet(project(":mod").sourceSets.main.get())
        }

        create("tinactory_datagen") {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        create("data") {
            data()
            gameDirectory = rootProject.file("run/data")

            programArguments.addAll(
                "--mod", "tinactory_datagen",
                "--all",
                "--output", rootProject.project(":mod").file("src/generated/resources/").absolutePath,
                "--existing", rootProject.project(":mod").file("src/main/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath,
                "--existing", rootProject.file(extraResources).absolutePath,
            )
        }
    }
}

dependencies {
    val tinycorelibVersion = "${property("minecraft_version")}-${property("tinycorelib_version")}"

    compileOnly("org.shsts.tinycorelib:core:${tinycorelibVersion}:api")
    compileOnly("org.shsts.tinycorelib:datagen:${tinycorelibVersion}:api")
    runtimeOnly("org.shsts.tinycorelib:core:${tinycorelibVersion}")
    runtimeOnly("org.shsts.tinycorelib:datagen:${tinycorelibVersion}")

    implementation(rootProject.files(extraResources))
    implementation(project(":mod"))

    implementation("thedarkcolour:kotlinforforge-neoforge:${property("kff_version")}")
    implementation("dev.architectury:architectury-neoforge:${property("architectury_version")}")
    implementation("dev.ftb.mods:ftb-library-neoforge:${property("ftb_library_version")}")
}

checkSource {
    topPackage("org.shsts.tinactory.datagen")
    includeKotlin()
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

tasks.register<JavaExec>("checkQuestLanguage") {
    group = "verification"
    description = "Check FTB Quests native translation tables."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "org.shsts.tinactory.datagen.quest.QuestTranslationChecker"
    workingDir = rootProject.file("run/questLanguage")
    systemProperty("tinactory.projectRoot", rootProject.projectDir.absolutePath)
    doFirst {
        workingDir.mkdirs()
    }
}
