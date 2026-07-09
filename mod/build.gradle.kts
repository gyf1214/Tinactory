import org.gradle.jvm.tasks.Jar

plugins {
    id("tinactory-common")
    jacoco
}

base {
    archivesName = "tinactory"
}

neoForge {
    mods {
        create("tinactory") {
            sourceSet(sourceSets.main.get())
        }

        create("tinactory_test") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.test.get())
        }
    }

    runs {
        create("client") {
            client()
            gameDirectory = rootProject.file("run/client")
            loadedMods.set(listOf(mods["tinactory"]))
        }

        create("server") {
            server()
            gameDirectory = rootProject.file("run/server")
            loadedMods.set(listOf(mods["tinactory"]))
        }

        create("gameTestServer") {
            type = "gameTestServer"
            gameDirectory = rootProject.file("run/gameTestServer")
            loadedMods.set(listOf(mods["tinactory_test"]))

            systemProperty(
                "tinactory.dependencyChecker.reportFile",
                layout.buildDirectory.file("reports/dependencyChecker/unreachable-targets.txt")
                    .get().asFile.absolutePath,
            )
            systemProperty(
                "tinactory.dependencyChecker.verbose",
                providers.gradleProperty("tinactory.dependencyChecker.verbose")
                    .orElse(providers.systemProperty("tinactory.dependencyChecker.verbose"))
                    .orElse("false").get(),
            )
        }
    }

    unitTest {
        enable()
        testedMod = mods["tinactory_test"]
        loadedMods = listOf(mods["tinactory_test"])
    }
}

dependencies {
    val tinycorelibVersion = "${property("minecraft_version")}-${property("tinycorelib_version")}"

    compileOnly("org.shsts.tinycorelib:core:${tinycorelibVersion}:api")
    runtimeOnly("org.shsts.tinycorelib:core:${tinycorelibVersion}")

    implementation("mezz.jei:jei-${property("minecraft_version")}-neoforge:${property("jei_version")}")
    implementation("curse.maven:jade-324717:${property("jade_id")}")
    implementation("dev.architectury:architectury-neoforge:${property("architectury_version")}")
    implementation("dev.ftb.mods:ftb-library-neoforge:${property("ftb_library_version")}")
    implementation("dev.ftb.mods:ftb-quests-neoforge:${property("ftb_quests_version")}")

    runtimeOnly("curse.maven:ctm-267602:${property("ctm_id")}")
    runtimeOnly("dev.ftb.mods:ftb-teams-neoforge:${property("ftb_teams_version")}")
    runtimeOnly("dev.ftb.mods:ftb-filter-system-neoforge:${property("ftb_filter_system_version")}")
    runtimeOnly("dev.ftb.mods:ftb-xmod-compat-neoforge:${property("ftb_xmod_compat_version")}") {
        isTransitive = false
    }

    testCompileOnly("org.shsts.tinycorelib:core:${tinycorelibVersion}:api")
    testImplementation(platform("org.junit:junit-bom:5.12.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

checkSource {
    topPackage("org.shsts.tinactory")
    banImport("api", "core", "integration", "content", "compat")
    banImport("core", "integration", "content", "compat")
    banImport("integration", "content", "compat")
    banImport("unit", "integration", "content")
    includeTest()
}

sourceSets.main {
    resources {
        srcDir("src/generated/resources")
        exclude(".cache/")
    }
}

tasks.test {
    configure<JacocoTaskExtension> {
        includes = listOf("org.shsts.tinactory.api.*", "org.shsts.tinactory.core.*")
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
        csv.required = true
    }
    classDirectories.setFrom(
        sourceSets.main.get().output.asFileTree.matching {
            include("org/shsts/tinactory/api/**")
            include("org/shsts/tinactory/core/**")
        }
    )
}

configurations {
    maybeCreate("api")
}

val apiJar by tasks.registering(Jar::class) {
    archiveClassifier = "api"
    include("org/shsts/tinactory/api/**")
    from(sourceSets.main.get().output)
}

artifacts {
    add("api", apiJar)
}

tasks.build {
    dependsOn(apiJar)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.jar)
            artifact(apiJar)
            artifact(tasks.named("sourcesJar"))
        }
    }
}
