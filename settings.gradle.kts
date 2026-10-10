import dev.eav.tomlkt.*
import kotlin.math.*

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases/")
        maven("https://maven.kikugie.dev/snapshots/")
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("net.fabricmc.fabric-loom") version providers.gradleProperty("plugins.fabric-loom")
    }
}

plugins {
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
