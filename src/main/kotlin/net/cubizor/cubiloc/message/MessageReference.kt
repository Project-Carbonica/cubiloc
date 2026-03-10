package net.cubizor.cubiloc.message

internal object MessageReference {

    // {@key} or {@key|fallback}
    private val DIRECT = Regex("""\{@([a-zA-Z0-9_.]+)(?:\|([^}]*))?\}""")

    // {trueKey,falseKey@#field}
    private val CONDITIONAL = Regex("""\{([a-zA-Z0-9_.]+),([a-zA-Z0-9_.]+)@#([a-zA-Z0-9_.]+)\}""")

    fun resolve(message: String, messageMap: Map<String, Any>, placeholders: Map<String, Any>): String {
        var result = message
        result = resolveConditional(result, messageMap, placeholders)
        result = resolveDirect(result, messageMap)
        return result
    }

    private fun resolveDirect(message: String, messageMap: Map<String, Any>): String =
        DIRECT.replace(message) { match ->
            val key = match.groupValues[1]
            val fallback = match.groupValues[2].ifEmpty { null }
            when (val resolved = messageMap[key]) {
                is String -> resolved
                is List<*> -> resolved.joinToString("\n")
                null -> fallback ?: match.value
                else -> resolved.toString()
            }
        }

    private fun resolveConditional(
        message: String,
        messageMap: Map<String, Any>,
        placeholders: Map<String, Any>,
    ): String = CONDITIONAL.replace(message) { match ->
        val trueKey = match.groupValues[1]
        val falseKey = match.groupValues[2]
        val field = match.groupValues[3]
        val condition = toBoolean(placeholders[field])
        val selectedKey = if (condition) trueKey else falseKey
        when (val resolved = messageMap[selectedKey]) {
            is String -> resolved
            null -> selectedKey
            else -> resolved.toString()
        }
    }

    private fun toBoolean(value: Any?): Boolean = when (value) {
        null -> false
        is Boolean -> value
        is Number -> value.toDouble() != 0.0
        is String -> value.lowercase() in setOf("true", "yes", "1", "on", "active", "enabled")
        else -> true
    }
}
