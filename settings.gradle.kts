pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        gradlePluginPortal()
        maven("https://files.minecraftforge.net/maven/")
        maven("https://jitpack.io")
    }
}

if (JavaVersion.current().ordinal + 1 < 25) {
    throw IllegalStateException("Please run gradle with Java 25+!")
}

rootProject.name = "cloth-config"
