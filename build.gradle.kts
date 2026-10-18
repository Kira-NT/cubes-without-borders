@file:Suppress("UnstableApiUsage")

import kotlinx.serialization.json.*
import net.fabricmc.loom.task.*
import net.fabricmc.loom.util.*
import java.nio.file.*
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.*

plugins {
    id("dev.kirant.omni-loom")
}

val minVersion = "0.0.0"
val maxVersion = "99.99.99"
val firstUnobfuscatedVersion = "26.1"

val minecraft = sc.current.version
val loader = sc.current.project.substringAfterLast('+', "")
val loaders = when (loader) {
    "" -> listOf("fabric", "quilt", "forge", "neoforge")
    "fabric" -> listOf("fabric", "quilt")
    else -> listOf(loader)
}
sc.properties.tags(loaders + minecraft)

val isUnobfuscated = sc.current.parsed >= firstUnobfuscatedVersion
val baselineMinecraft = sc.properties.getOrNull("dependencies.minecraft") ?: minecraft
val nextMinecraft = sc.properties.getOrNull("dependencies.minecraft-next") ?: sc.tree.versions
    .filter { it.project.substringAfterLast('+', loader) == loader && it.parsed > sc.current.parsed }
    .minByOrNull { it.parsed }?.version ?: maxVersion

val modId = sc.properties["mod.id"]
val modVersion = formatVersion(sc.properties["mod.version"], minecraft, loader)
val modAuthor = sc.properties["mod.author"]
val modGroup = sc.properties.getOrNull("mod.group") ?: "dev.${modAuthor.lowercase().replace(Regex("[^a-z0-9]"), "")}.$modId"
val sourcesUrl = sc.properties.getOrNull("mod.sources") ?: "https://github.com/$modAuthor/$modId"
val java = when {
    sc.current.parsed >= "26.1" -> 25
    sc.current.parsed >= "1.20.5" -> 21
    sc.current.parsed >= "1.18" -> 17
    sc.current.parsed >= "1.17" -> 16
    else -> 8
}

base.archivesName = modId
version = modVersion
group = modGroup

val loomCache = (gradle.startParameter.projectCacheDir ?: rootProject.file(".gradle")).resolve("loom-cache/projects${project.path.replace(':', '/')}")
val accessWidener = rootProject.file("src/main/resources/$modId.accesswidener")
loom.accessWidenerPath = sc.process(accessWidener, loomCache.resolve(accessWidener.name).toString())

repositories {
    mavenCentral { name = "MavenCentralLWJGL" } // Arch Loom REALLY wants this repository.
    maven("https://api.modrinth.com/maven/") { content { includeGroup("maven.modrinth") } }
    maven("https://maven.neoforged.net/releases/") { content { includeGroupByRegex("(net|cpw).(neoforged|mods).*") } }
    maven("https://maven.minecraftforge.net/") { content { includeGroup("net.minecraftforge") } }
    maven("https://repo.spongepowered.org/repository/maven-public/") { content { includeGroup("org.spongepowered") } }
}

dependencies {
    fun use(name: String, consumer: (prop: String) -> Unit) = sc.properties.getOrNull("dependencies.$name").also {
        sc.constants[name] = it != null
        sc.targets[name] = it?.let { Regex("(?<!mc|\\d|\\.)\\d+\\.\\d+(?:\\.\\d+)?").find(it)?.value } ?: minVersion
        it?.let(consumer)
    }

    fun unJarJar(notation: String, target: String = ""): FileCollection {
        val name = target.ifBlank { notation.substringAfter(':').substringBefore(':') }
        val input = configurations.detachedConfiguration(dependencies.create(notation)).singleFile.toPath()
        val output = layout.buildDirectory.file("unJarJar/${notation.replace(':', '.')}.jar").get().asFile.toPath()
        input.takeUnless { output.exists() }?.let { it.asFileSystem.use { fs ->
            (fs.jarJarMetadata?.jarJarEntries?.firstOrNull { j -> j.name.contains(name) } ?: it).copyTo(output.createParentDirectories())
        } }
        return files(output)
    }

    minecraft("com.mojang:minecraft:$baselineMinecraft")
    mappings(loom.layered {
        val range = sc.parse(baselineMinecraft).rangeUntil(sc.parse(if (isUnobfuscated) maxVersion else firstUnobfuscatedVersion))
        takeUnless { isUnobfuscated }?.let { officialMojangMappings { nameSyntheticMembers = true } }
        rootProject.file("mappings").listFiles { it.extension == "tiny" }.orEmpty()
            .map { it to sc.tryParse(it.nameWithoutExtension).getOrNull() }
            .filter { it.second?.let(range::contains) ?: false }
            .sortedByDescending { it.second }.map { it.first }
            .ifEmpty { listOf(rootProject.file("mappings/.tiny")) }
            .forEach { mappings(it) }
    })

    use("fabric") { modImplementation("net.fabricmc:fabric-loader:$it") }
    use("fancymodloader") { compileOnly("net.neoforged.fancymodloader:loader:$it") }
    use("neoforge") { (if (isUnobfuscated) "compileOnly" else "neoForge")("net.neoforged:neoforge:$it") }
    use("forge") { when {
        isUnobfuscated -> {
            compileOnly("net.minecraftforge:forge:$it:universal")
            compileOnly("net.minecraftforge:javafmllanguage:$it")
            compileOnly("net.minecraftforge:fmlloader:$it")
            compileOnly("net.minecraftforge:fmlcore:$it")
        }
        else -> "forge"("net.minecraftforge:forge:$it")
    } }

    use("mixin") { compileOnly("org.spongepowered:mixin:$it") }
    use("mixinextras") {
        compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:$it")!!)
        implementation("include"("io.github.llamalad7:mixinextras-$loader:$it")!!)
    }

    use("sodium") { modCompileOnly(unJarJar("maven.modrinth:sodium:$it")) }
    use("modmenu") { modCompileOnly("maven.modrinth:modmenu:$it") }
    use("yacl") { modCompileOnly("maven.modrinth:yacl:$it") }
    use("cloth-config") { modCompileOnly("maven.modrinth:cloth-config:$it") }

    use("java-objc-bridge") { compileOnly("ca.weblite:java-objc-bridge:$it") }
}

val mainMixinConfig = "$modId.mixins.json"
val sodiumMixinConfig = "$modId.sodium.mixins.json"
val mixinConfigs = when {
    sc.eval(sc.targets["sodium"]!!, ">0.0.0 <0.8.0") -> listOf(mainMixinConfig, sodiumMixinConfig)
    else -> listOf(mainMixinConfig)
}

stonecutter.replacements {
    string(sc.eval(sc.targets["sodium"]!!, ">=0.6.0")) {
        replace("me.jellysquid.", "net.caffeinemc.")
        replace("me/jellysquid/", "net/caffeinemc/")
    }

    string(sc.eval(sc.targets["modmenu"]!!, ">=1.15.0")) {
        replace("io.github.prospector.modmenu.", "com.terraformersmc.modmenu.")
    }

    string(loader != "forge" || sc.eval(sc.targets["cloth-config"]!!, ">3.0.0")) {
        replace("me.shedaniel.forge.clothconfig2.", "me.shedaniel.clothconfig2.")
    }
}

tasks.processResources {
    fun MutableMap<String, String?>.add(key: String, value: String?) {
        inputs.property(key, value)
        set(key, value)
    }

    val metadata = buildMap {
        add("id", modId)
        add("version", modVersion)
        add("name", sc.properties.getOrNull("mod.name") ?: modId)
        add("description", sc.properties.getOrNull("mod.description") ?: "")
        add("environment", sc.properties.getOrNull("mod.environment") ?: "*")
        add("author", modAuthor)
        add("group", modGroup)
        add("license", sc.properties.getOrNull("mod.license") ?: "ARR")
        add("modrinth", sc.properties.getOrNull("mod.modrinth.id") ?: "")
        add("curseforge", sc.properties.getOrNull("mod.curseforge.id") ?: "")
        add("homepage", sc.properties.getOrNull("mod.homepage") ?: sourcesUrl)
        add("sources", sourcesUrl)
        add("issues", sc.properties.getOrNull("mod.issues") ?: "$sourcesUrl/issues")
        add("loaders", loaders.joinToString(", ") { "\"$it\"" }.trim('"'))
        add("mixins", mixinConfigs.joinToString(", ") { "\"$it\"" }.trim('"'))
        add("mixins_neoforge", mixinConfigs.mapIndexed { i, x -> "${if (i == 0) "" else "[[mixins]]\nconfig = "}\"$x\"" }.joinToString("\n\n").trim('"'))
        add("fabric_loader", sc.properties.getOrNull("dependencies.fabric") ?: minVersion)
        add("neoforge", sc.properties.getOrNull("dependencies.neoforge") ?: minVersion)
        add("forge", sc.properties.getOrNull("dependencies.forge") ?: minVersion)
        add("minecraft", minecraft)
        add("minecraft_next", nextMinecraft)
        add("java", "$java")
    }

    filesMatching(listOf("*.mod.json", "META-INF/*.toml", "*.mcmeta")) {
        expand(metadata)
    }
}

tasks.jar {
    from(rootDir) { include("LICENSE*") }
    manifest.attributes("MixinConfigs" to mixinConfigs.joinToString(","))
}

tasks.withType<AbstractRemapJarTask> {
    targetNamespace.takeIf { isUnobfuscated }?.set("official")

    doLast {
        archiveFile.get().asFile.toPath().asFileSystem.use { fs ->
            // Convert the access widener to an access transformer if we target (Neo)Forge.
            // By this time, it should have been remapped already.
            fs.getPath("").takeIf { loaders.any { it.endsWith("forge") } }?.listDirectoryEntries("*.accesswidener")?.firstOrNull()?.let { aw ->
                fs.getPath("META-INF/accesstransformer.cfg").writeLines(aw.readLines().drop(1).map(::aw2at))
                aw.takeUnless { "fabric" in loaders || "quilt" in loaders }?.deleteExisting()
            }

            // The Gradle way would be to exclude these files from the final jar during the processResources phase.
            // Here, however, we HATE Gradle and its way of doing things! :3
            // Oh, and also Loom will not remap the access widener if there's no fabric.mod.json present,
            // so it's the best place to do it, really.
            fs.getPath(sodiumMixinConfig).takeUnless { sodiumMixinConfig in mixinConfigs }?.deleteIfExists()
            fs.getPath("fabric.mod.json").takeUnless { "fabric" in loaders }?.deleteIfExists()
            fs.getPath("quilt.mod.json").takeUnless { "quilt" in loaders }?.deleteIfExists()
            fs.getPath("META-INF/mods.toml").takeUnless { "forge" in loaders }?.deleteIfExists()
            fs.getPath("META-INF/neoforge.mods.toml").takeUnless { "neoforge" in loaders }?.deleteIfExists()
            fs.getPath("pack.mcmeta").takeUnless { loaders.any { it.endsWith("forge") } }?.deleteIfExists()
        }

        // Fix up the jar after we've meddled with it.
        if (isReproducibleFileOrder || !isPreserveFileTimestamps || entryCompression != ZipEntryCompression.DEFLATED) {
            ZipReprocessorUtil.reprocessZip(archiveFile.get().asFile.toPath(), isReproducibleFileOrder, isPreserveFileTimestamps, entryCompression)
        }
    }
}

tasks.withType<JavaCompile> {
    // The --release option was introduced in Java 9.
    // Attempting to use it with any older version will result in an error.
    options.release.takeUnless { java <= 8 }?.set(java)
    options.encoding = "UTF-8"
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.toVersion(java)
    targetCompatibility = sourceCompatibility

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(java)
    }
}

fun formatVersion(version: String, minecraft: String, loader: String): String = buildString {
    val isRelease = (System.getenv("GITHUB_REF") ?: "").startsWith("refs/tags/") || System.getenv("CI_RELEASE") == "1"
    when {
        isRelease || version.contains('-') -> append(version)
        else -> append(version.substringBeforeLast(".")).append('.').append(version.substringAfterLast(".").toInt() + 1).append("-alpha")
    }
    (System.getenv("GITHUB_RUN_NUMBER")?.ifBlank { null } ?: "0").takeIf { !isRelease }?.let { append('.').append(it) }
    append('+')
    append(minecraft)
    ".0".takeIf { loader.isNotBlank() && minecraft.indexOf('.') == minecraft.lastIndexOf('.') }?.let { append(it) }
    loader.ifBlank { null }?.let { append('.').append(it) }
}

fun aw2at(entry: String): String = buildString(entry.length) {
    // https://docs.fabricmc.net/develop/class-tweakers/access-widening
    // https://docs.neoforged.net/docs/advanced/accesstransformers
    val parts = entry.substringBefore('#').trim().removePrefix("transitive-").split(' ', '\t').filter { it.isNotEmpty() }
    if (parts.size < 3 || parts[0] !in listOf("accessible", "extendable", "mutable")) {
        return@buildString
    }

    append(if (parts[0] == "accessible") "public" else "public-f").append(' ').append(parts[2].replace('/', '.'))
    if (parts.size == 5) {
        append(' ').append(parts[3]).takeIf { parts[2] == "method" }?.append(parts[4])
    }
}

val FileSystem.jarJarMetadata: Path?
    get() = getPath("META-INF/jarjar/metadata.json").takeIf { it.exists() }

val Path.jarJarEntries: List<Path>
    get() = Json.parseToJsonElement(readText()).jsonObject
        .getOrDefault("jars", null)?.jsonArray.orEmpty()
        .mapNotNull { it.jsonObject.getOrDefault("path", null)?.jsonPrimitive?.content }
        .map { fileSystem.getPath(it) }
        .filter { it.exists() }

val Path.asFileSystem: FileSystem
    get() = FileSystems.newFileSystem(this, null as ClassLoader?)
