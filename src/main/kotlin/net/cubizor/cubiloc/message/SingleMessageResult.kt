package net.cubizor.cubiloc.message

import eu.okaeri.placeholders.Placeholders
import net.cubizor.cubicolor.api.ColorScheme
import net.cubizor.cubicolor.text.MessageTheme
import net.cubizor.cubiloc.context.I18nContextHolder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class SingleMessageResult internal constructor(
    private val rawValue: String,
    private val placeholders: Map<String, Any> = emptyMap(),
    internal val globalPlaceholders: Placeholders? = null,
    private val colorScheme: ColorScheme? = null,
    private val messageTheme: MessageTheme? = null,
    internal val messageMap: Map<String, Any>? = null,
) {
    private var processedValue: String? = null

    fun with(vararg pairs: Pair<String, Any>): SingleMessageResult =
        SingleMessageResult(rawValue, placeholders + pairs, globalPlaceholders, colorScheme, messageTheme, messageMap)

    fun withColorScheme(scheme: ColorScheme): SingleMessageResult =
        SingleMessageResult(rawValue, placeholders, globalPlaceholders, scheme, messageTheme, messageMap)

    fun withMessageTheme(theme: MessageTheme): SingleMessageResult =
        SingleMessageResult(rawValue, placeholders, globalPlaceholders, colorScheme, theme, messageMap)

    private fun process(): String {
        processedValue?.let { return it }
        var value = rawValue
        if (messageMap != null) {
            value = MessageReference.resolve(value, messageMap, placeholders)
        }
        val result = MessageResolver.resolvePlaceholders(value, placeholders, globalPlaceholders)
        processedValue = result
        return result
    }

    fun component(): Component {
        val processed = process()
        val context = I18nContextHolder.get()
        val mm = MessageResolver.buildMiniMessage(
            colorScheme ?: context.colorScheme,
            messageTheme ?: context.messageTheme,
        )
        return mm.deserialize(processed)
    }

    fun component(additionalResolver: TagResolver): Component {
        val processed = process()
        val context = I18nContextHolder.get()
        val mm = MessageResolver.buildMiniMessage(
            colorScheme ?: context.colorScheme,
            messageTheme ?: context.messageTheme,
            additionalResolver,
        )
        return mm.deserialize(processed)
    }

    fun componentLegacy(): Component =
        LegacyComponentSerializer.legacyAmpersand().deserialize(process())

    fun asString(): String = process()

    fun raw(): String = rawValue
}
