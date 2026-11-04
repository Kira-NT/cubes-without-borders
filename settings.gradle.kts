@file:Suppress("UnstableApiUsage")

import dev.eav.tomlkt.*
import kotlin.math.*

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases/")
        maven("https://maven.kikugie.dev/snapshots/")
        maven("https://maven.architectury.dev/")
        maven("https://files.minecraftforge.net/maven/")
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradleup.shadow") version providers.gradleProperty("plugins.shadow") apply false
    id("dev.kikugie.stonecutter") version providers.gradleProperty("plugins.stonecutter")
    id("org.gradle.toolchains.foojay-resolver-convention") version providers.gradleProperty("plugins.foojay-resolver-convention")
}

stonecutter {
    create(rootProject) {
        versions(Toml.parseToTomlTable(file("stonecutter.properties.toml").readText())
            .filterKeys { it.matches(Regex("^\\d+\\.\\d+(\\.\\d+)?$")) }
            .mapValues { it.value as? TomlTable }.filterValues { it != null }
            .flatMap { listOf(it.toPair()) + it.value!!.map { x -> "${it.key}+${x.key}" to x.value as? TomlTable } }
            .filter { it.second?.containsKey("dependencies") ?: false }
            .map { it.first to it.first.substringBefore('+') }
            .sortedByDescending {
                // 26.1 > 1.21.11 > 1.21.11+fabric > 1.21.11+forge > 1.21.11+neoforge
                (it.second.split('.') + listOf("0")).take(3).fold(0) { acc, x -> acc * 100 + x.toInt() } * 100
                    + abs(2 - abs(it.first.length - it.second.length - 7))
            })
    }
}

gradle.lifecycle.beforeProject {
    val loader = name.substringAfterLast('+', "fabric").takeUnless { this == rootProject } ?: return@beforeProject
    val version = (name.split('.', '+') + listOf("0")).take(3).fold(0) { acc, x -> acc * 100 + (x.toIntOrNull() ?: 0) }
    val (pluginId, pluginVersion) = when (loader) {
        "fabric" -> "net.fabricmc.fabric-loom-remap" to findProperty("plugins.fabric-loom-remap")
        else -> "dev.architectury.loom" to findProperty("plugins.architectury-loom")
    }

    extensions.extraProperties["loom.platform"] = loader
    extensions.extraProperties["fabric.loom.dropNonIntermediateRootMethods"] = "${version < when (loader) {
        "neoforge" -> 1_20_02
        "forge" -> 1_17_00
        else -> 26_01_00
    }}"

    extensions.extraProperties["gradle.plugins"] = "${findProperty("gradle.plugins") ?: ""};$pluginId"
    buildscript.dependencies.add("classpath", "$pluginId:$pluginId.gradle.plugin:$pluginVersion")
}
