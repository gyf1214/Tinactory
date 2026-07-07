println(
    "Java: ${System.getProperty("java.version")}, " +
        "JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), " +
        "Arch: ${System.getProperty("os.arch")}",
)
println(
    "Minecraft: ${property("minecraft_version")}, " +
        "NeoForge: ${property("neo_version")}, " +
        "ModDevGradle: ${property("moddev_version")}",
)
