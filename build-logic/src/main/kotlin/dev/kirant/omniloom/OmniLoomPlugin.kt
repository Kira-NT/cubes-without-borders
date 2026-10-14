@file:Suppress("UnstableApiUsage")

package dev.kirant.omniloom

import org.gradle.api.*
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtensionAware

abstract class OmniLoomPlugin : Plugin<ExtensionAware> {
    override fun apply(target: ExtensionAware) {
        when (target) {
            is Settings -> target.gradle.settingsEvaluated {
                target.gradle.lifecycle.beforeProject(OmniLoomAction)
            }

            is Project -> {
                target.findProperty("loom.plugin")?.let { target.plugins.apply("$it") }
            }

            else -> {
                throw UnsupportedOperationException("The plugin may only be applied to settings and projects, not ${target::class.qualifiedName}")
            }
        }
    }
}

private object OmniLoomAction : IsolatedAction<Project> {
    override fun execute(target: Project): Unit = with(target) {
        // We default to architectury-loom for all obfuscated versions of Minecraft (<26.1),
        // even when targeting Fabric, because it handles conflict resolution much better
        // when layering mappings on top of mojmap.
        //
        // For modern versions, however, we switch to vanilla Loom, since layering mappings
        // on top of the official ones is no longer necessary, and upstream updates
        // are naturally much faster.
        val loader = name.substringAfterLast('+', "fabric").takeUnless { this == rootProject } ?: return
        val (pluginId, pluginVersion) = when {
            name.indexOf('.') == 1 || loader != "fabric" -> "dev.architectury.loom" to findProperty("plugins.architectury-loom")
            else -> "net.fabricmc.fabric-loom-remap" to findProperty("plugins.fabric-loom-remap")
        }

        extensions.extraProperties["loom.platform"] = loader
        extensions.extraProperties["loom.plugin"] = pluginId
        extensions.extraProperties["loom.version"] = pluginVersion
        buildscript.dependencies.add("classpath", "$pluginId:$pluginId.gradle.plugin:$pluginVersion")
    }

    private fun readResolve(): Any = this
}
