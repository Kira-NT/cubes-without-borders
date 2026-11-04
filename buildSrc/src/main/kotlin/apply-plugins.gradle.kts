for (pluginId in findProperty("gradle.plugins")?.toString()?.split(';').orEmpty()) {
    pluginId.trim().takeUnless { it.isEmpty() }?.let(plugins::apply)
}
