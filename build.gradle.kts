plugins {
    id("net.fabricmc.fabric-loom")
}

val minecraft = required("dependencies.minecraft")
val java = required("dependencies.java")
val modId = required("mod.id")
val modVersion = formatVersion("${required("mod.version")}+${minecraft}")
val modAuthor = optional("mod.author")
val modGroup = optional("mod.group", modAuthor?.let { "dev.${it.lowercase().replace(Regex("[^a-z0-9]"), "")}.$modId" })
val sourcesUrl = optional("mod.sources", "https://github.com/$modAuthor/$modId")

base.archivesName = modId
version = modVersion
group = modGroup ?: ""
loom.accessWidenerPath = file("src/main/resources/$modId.accesswidener")

repositories {
    maven("https://api.modrinth.com/maven/") { content { includeGroup("maven.modrinth") } }
    maven("https://maven.neoforged.net/releases/") { content { includeGroupByRegex("net.neoforged.*") } }
    maven("https://maven.minecraftforge.net/") { content { includeGroup("net.minecraftforge") } }
}

dependencies {
    use("dependencies.minecraft") { minecraft("com.mojang:minecraft:$it") }
    use("dependencies.fabric-loader") { implementation("net.fabricmc:fabric-loader:$it") }

    use("dependencies.modmenu") { compileOnly("maven.modrinth:modmenu:$it") }
    use("dependencies.yacl") { compileOnly("maven.modrinth:yacl:$it") }
    use("dependencies.cloth-config") { compileOnly("maven.modrinth:cloth-config:$it") }

    compileOnly("ca.weblite:java-objc-bridge:1.1")
}

tasks.processResources {
    filesMatching(listOf("*.mod.json", "META-INF/*.toml", "*.mcmeta")) {
        expand(mapOf(
            "id" to modId,
            "version" to modVersion,
            "name" to optional("mod.name", modId),
            "description" to optional("mod.description"),
            "environment" to optional("mod.environment", "*"),
            "author" to modAuthor,
            "group" to modGroup,
            "license" to optional("mod.license", "ARR"),
            "modrinth" to optional("mod.modrinth.id"),
            "curseforge" to optional("mod.curseforge.id"),
            "homepage" to optional("mod.homepage", sourcesUrl),
            "sources" to sourcesUrl,
            "issues" to optional("mod.issues", "$sourcesUrl/issues"),
            "fabric_loader" to optional("dependencies.fabric-loader", "0.0.0"),
            "neoforge" to optional("dependencies.neoforge", "0.0.0"),
            "forge" to optional("dependencies.forge", "0.0.0")!!.split('-').last(),
            "minecraft" to minecraft,
            "java" to java,
        ))
    }
}

tasks.jar {
    from(".") { include("LICENSE*") }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = Integer.valueOf(java)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.toVersion(java)
    targetCompatibility = sourceCompatibility
}

fun required(key: String) = property(key) as String
fun optional(key: String, default: String? = "") = findProperty(key) as? String? ?: default
fun use(name: String, consumer: (prop: String) -> Unit) = optional(name, null)?.let(consumer)

fun formatVersion(version: String): String {
    val isRelease = (System.getenv("GITHUB_REF") ?: "").startsWith("refs/tags/") || System.getenv("CI_RELEASE") == "1"
    val runNumber = System.getenv("GITHUB_RUN_NUMBER")?.ifBlank { null } ?: "0"
    return when {
        isRelease -> version
        version.contains('-') -> version.replace(Regex("^([^+]+)(.*)$"), "$1.${runNumber}$2")
        else -> version.replace(Regex("^((?:\\d+\\.)*)(\\d+)(.*)$")) { "${it.groupValues[1]}${(it.groupValues[2].toInt() + 1)}-alpha.${runNumber}${it.groupValues[3]}" }
    }
}
