package net.cubizor.cubiloc.message

import eu.okaeri.placeholders.Placeholders
import net.cubizor.cubicolor.api.ColorScheme
import net.cubizor.cubicolor.text.MessageTheme
import net.cubizor.cubiloc.context.I18nContextHolder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class ListMessageResult internal constructor(
    private val rawValues: List<String>,
    private val placeholders: Map<String, Any> = emptyMap(),
    internal val globalPlaceholders: Placeholders? = null,
    private val colorScheme: ColorScheme? = null,
    private val messageTheme: MessageTheme? = null,
    internal val messageMap: Map<String, Any>? = null,
) {
    private var processedValues: List<String>? = null

    fun with(vararg pairs: Pair<String, Any>): ListMessageResult =
        ListMessageResult(rawValues, placeholders + pairs, globalPlaceholders, colorScheme, messageTheme, messageMap)

    fun withColorScheme(scheme: ColorScheme): ListMessageResult =
        ListMessageResult(rawValues, placeholders, globalPlaceholders, scheme, messageTheme, messageMap)

    fun withMessageTheme(theme: MessageTheme): ListMessageResult =
        ListMessageResult(rawValues, placeholders, globalPlaceholders, colorScheme, theme, messageMap)

    private fun process(): List<String> {
        processedValues?.let { return it }
        val result = rawValues.map { line ->
            var value = line
            if (messageMap != null) {
                value = MessageReference.resolve(value, messageMap, placeholders)
            }
            MessageResolver.resolvePlaceholders(value, placeholders, globalPlaceholders)
        }
        processedValues = result
        return result
    }

    fun components(): List<Component> {
        val processed = process()
        val context = I18nContextHolder.get()
        val mm = MessageResolver.buildMiniMessage(
            colorScheme ?: context.colorScheme,
            messageTheme ?: context.messageTheme,
        )
        return processed.map { mm.deserialize(it) }
    }

    fun component(): Component {
        val processed = process()
        if (processed.isEmpty()) return Component.empty()
        val context = I18nContextHolder.get()
        val mm = MessageResolver.buildMiniMessage(
            colorScheme ?: context.colorScheme,
            messageTheme ?: context.messageTheme,
        )
        return processed.drop(1).fold(mm.deserialize(processed[0])) { acc, line ->
            acc.append(Component.newline()).append(mm.deserialize(line))
        }
    }

    fun componentLegacy(): Component {
        val processed = process()
        if (processed.isEmpty()) return Component.empty()
        val s = LegacyComponentSerializer.legacyAmpersand()
        return processed.drop(1).fold(s.deserialize(processed[0])) { acc, line ->
            acc.append(Component.newline()).append(s.deserialize(line))
        }
    }

    fun asString(): String = process().joinToString("\n")

    fun asList(): List<String> = process().toList()

    fun raw(): List<String> = rawValues.toList()
}
