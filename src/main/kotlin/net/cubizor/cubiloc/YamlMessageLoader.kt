package net.cubizor.cubiloc

import org.yaml.snakeyaml.Yaml
import java.io.File

internal object YamlMessageLoader {

    fun load(file: File): Map<String, Any> {
        val yaml = Yaml()
        val data: Map<String, Any> = file.reader().use { yaml.load(it) } ?: return emptyMap()
        return flatten(data)
    }

    @Suppress("UNCHECKED_CAST")
    fun flatten(map: Map<String, Any>, prefix: String = ""): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        for ((key, value) in map) {
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
            when (value) {
                is Map<*, *> -> result.putAll(flatten(value as Map<String, Any>, fullKey))
                else -> result[fullKey] = value
            }
        }
        return result
    }
}
