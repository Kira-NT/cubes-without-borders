plugins {
    id("net.fabricmc.fabric-loom")
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
    maven("https://api.modrinth.com/maven/") { content { includeGroup("maven.modrinth") } }
    maven("https://maven.neoforged.net/releases/") { content { includeGroupByRegex("net.neoforged.*") } }
    maven("https://maven.minecraftforge.net/") { content { includeGroup("net.minecraftforge") } }
}

dependencies {
    fun use(name: String, consumer: (prop: String) -> Unit) = sc.properties.getOrNull("dependencies.$name").also {
        sc.constants[name] = it != null
        sc.targets[name] = it?.let { Regex("(?<!mc|\\d|\\.)\\d+\\.\\d+(?:\\.\\d+)?").find(it)?.value } ?: minVersion
        it?.let(consumer)
    }

    minecraft("com.mojang:minecraft:$baselineMinecraft")
    use("fabric") { implementation("net.fabricmc:fabric-loader:$it") }
    use("fancymodloader") { compileOnly("net.neoforged.fancymodloader:loader:$it") }
    use("neoforge") { compileOnly("net.neoforged:neoforge:$it") }
    use("forge") {
        compileOnly("net.minecraftforge:forge:$it:universal")
        compileOnly("net.minecraftforge:javafmllanguage:$it")
        compileOnly("net.minecraftforge:fmlloader:$it")
        compileOnly("net.minecraftforge:fmlcore:$it")
    }

    use("sodium") { compileOnly("maven.modrinth:sodium:$it") }
    use("modmenu") { compileOnly("maven.modrinth:modmenu:$it") }
    use("yacl") { compileOnly("maven.modrinth:yacl:$it") }
    use("cloth-config") { compileOnly("maven.modrinth:cloth-config:$it") }

    use("java-objc-bridge") { compileOnly("ca.weblite:java-objc-bridge:$it") }
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
    manifest.attributes("MixinConfigs" to "$modId.mixins.json")
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
