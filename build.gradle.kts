@file:Suppress("UnstableApiUsage")

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.Function

plugins {
    java
    idea
    id("quiet-fabric-loom") version ("1.9-SNAPSHOT")
    kotlin("jvm") version ("2.1.0")
    `maven-publish`
}
val modId = project.properties["mod_id"].toString()
version = project.properties["mod_version"].toString()
group = project.properties["mod_group"].toString()

val modName = project.properties["mod_name"].toString()
base.archivesName.set(modName)

val minecraftVersion = project.properties["minecraft_version"].toString()

loom {
    mixin.useLegacyMixinAp.set(false)
    interfaceInjection.enableDependencyInterfaceInjection.set(true)
    splitEnvironmentSourceSets()
    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
    if (file("src/main/resources/$modId.accesswidener").exists()) {
        accessWidenerPath.set(file("src/main/resources/$modId.accesswidener"))
    }
}

val modImplementationInclude by configurations.register("modImplementationInclude")

configurations {
    modImplementationInclude
}

repositories {
    mavenCentral()
    maven( "https://jitpack.io")
    maven("https://maven.parchmentmc.org")
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots1"
        mavenContent { snapshotsOnly() }
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.impactdev.net/repository/development/")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$minecraftVersion:${project.properties["parchment_version"]}")
    })

    modImplementation("net.fabricmc:fabric-loader:${project.properties["loader_version"].toString()}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_version"].toString()}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.properties["fabric_kotlin_version"].toString()}")

    // Adventure Text!
    modImplementation(include("net.kyori:adventure-platform-fabric:5.14.2") {
        exclude("com.google.code.gson")
        exclude("ca.stellardrift", "colonel")
        exclude("net.fabricmc")
    })

    // PermissionsAPI
    modImplementation("me.lucko:fabric-permissions-api:0.3.1")

    // GUI libraries
//    modImplementation("ca.landonjw.gooeylibs:fabric-api-repack:3.1.0-1.21.1-SNAPSHOT@jar")
//    modImplementation("eu.pb4:sgui:1.6.1+1.21.1")

    // Placeholder Mods
//    modImplementation("io.github.miniplaceholders:miniplaceholders-api:2.2.3")
//    modImplementation("io.github.miniplaceholders:miniplaceholders-kotlin-ext:2.2.3")
//    modImplementation("eu.pb4:placeholder-api:2.4.1+1.21")

    // Impactor Libraries
    modImplementation("net.impactdev.impactor:common:5.3.0+1.21.1-SNAPSHOT")
    modImplementation("net.impactdev.impactor.api:economy:5.3.0-SNAPSHOT")
    modImplementation("net.impactdev.impactor.api:text:5.3.0-SNAPSHOT")

    // Cobblemon
    modImplementation("com.cobblemon:fabric:1.6.1+1.21.1")

    modCompileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

tasks.processResources {
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = base.archivesName.get()
        from(components["java"])
    }

    repositories {
        mavenLocal()
    }
}

tasks.processResources {
    inputs.property("mod_version", version)

    filesMatching("fabric.mod.json") {
        expand("id" to modId, "version" to version, "name" to modName)
    }

    filesMatching("**/lang/*.json") {
        expand("id" to modId, "version" to version, "name" to modName)
    }
}

tasks.remapJar {
    archiveFileName.set("${project.name}-fabric-$minecraftVersion-${project.version}.jar")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}

tasks.withType<AbstractArchiveTask> {
    from("LICENSE") {
        rename { "${it}_${modId}" }
    }
}
tasks.create("hydrate") {
    doLast {
        val applyFileReplacements: Function<String, String> = Function { path ->
            path.replace("\$mod_name$", project.properties["mod_name"].toString())
                .replace("\$mod_id$", project.properties["mod_id"].toString())
                .replace("\$mod_group$", project.properties["mod_group"].toString())
        }
        val applyPathReplacements: Function<String, String> = Function { path ->
            path.replace("\$mod_name$", project.properties["mod_name"].toString())
                .replace("\$mod_id$", project.properties["mod_id"].toString())
                .replace("\$mod_group$", project.properties["mod_group"].toString().replace(".", "/"))
        }
        project.extensions.getByType<JavaPluginExtension>().sourceSets.forEach { sourceSet ->
            sourceSet.allSource.sourceDirectories.asFileTree.forEach { file ->
                val newPath = Paths.get(applyPathReplacements.apply(file.path))
                Files.createDirectories(newPath.parent)

                if (!file.path.endsWith(".png")) {
                    val lines =
                        Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)
                            .map { applyFileReplacements.apply(it) }
                    Files.deleteIfExists(file.toPath())
                    Files.write(
                        newPath,
                        lines
                    )
                } else {
                    Files.move(file.toPath(), newPath)
                }

                var parent = file.parentFile
                while (parent.listFiles()?.isEmpty() == true) {
                    Files.deleteIfExists(parent.toPath())
                    parent = parent.parentFile
                }
            }
        }
    }
}
