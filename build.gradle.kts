import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
    id("com.gradleup.shadow") version "9.2.2"
    id("maven-publish")
}

loom {
    accessWidenerPath = file("src/main/resources/cloth-config.classtweaker")
}

val maven_group: String by properties

val common by configurations.creating
val shadowCommon by configurations.creating

configurations {
    // Don't use shadow from the shadow plugin because we don't want IDEA to index this.
    named("compileClasspath") {
        extendsFrom(common)
    }
    named("runtimeClasspath") {
        extendsFrom(common)
    }
}

repositories {
    maven {
        url = uri("https://maven.terraformersmc.com/releases/")
        content {
            includeGroup("com.terraformersmc")
        }
    }
    maven {
        url = uri("https://maven.shedaniel.me/")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")

    api("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    api(fabricApi.module("fabric-resource-loader-v0", property("fabric_api_version").toString()))
    api(fabricApi.module("fabric-screen-api-v1", property("fabric_api_version").toString()))
    api(fabricApi.module("fabric-key-mapping-api-v1", property("fabric_api_version").toString()))
    api(fabricApi.module("fabric-lifecycle-events-v1", property("fabric_api_version").toString()))

    api("me.shedaniel.cloth:basic-math:0.6.1")
    include("me.shedaniel.cloth:basic-math:0.6.1")

    implementation("blue.endless:jankson:${property("jankson_version")}")
    add(shadowCommon.name, "blue.endless:jankson:${property("jankson_version")}")

    compileOnly("com.terraformersmc:modmenu:${property("mod_menu_version")}") {
        isTransitive = false
    }
    localRuntime("com.terraformersmc:modmenu:${property("mod_menu_version")}") {
        isTransitive = false
    }
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
    inputs.property("version", project.version)
}

tasks.named<ShadowJar>("shadowJar") {
    relocate("blue.endless.jankson", "${maven_group}.clothconfig.shadowed.blue.endless.jankson")
    relocate("com.moandjiezana.toml", "${maven_group}.clothconfig.shadowed.com.moandjiezana.toml")
    relocate("org.yaml.snakeyaml", "${maven_group}.clothconfig.shadowed.org.yaml.snakeyaml")

    configurations = listOf(project.configurations[shadowCommon.name])
    archiveClassifier.set("")
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}

publishing {
    publications {
        create<MavenPublication>("mavenFabric") {
            artifactId = "${property("archives_base_name")}-fabric"
            from(components["java"])
        }
    }

    repositories {
        if (System.getenv("MAVEN_PASS") != null) {
            maven {
                url = uri("https://deploy.shedaniel.me/")
                credentials {
                    username = "shedaniel"
                    password = System.getenv("MAVEN_PASS")
                }
            }
        }
    }
}

