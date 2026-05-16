package net.cubizor.cubiloc.message

import eu.okaeri.placeholders.Placeholders
import net.cubizor.cubicolor.api.ColorScheme
import net.cubizor.cubicolor.text.MessageTheme
import net.cubizor.cubiloc.I18n
import net.cubizor.cubiloc.context.I18nContextHolder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class SingleMessageResult internal constructor(
    private val i18n: I18n? = null,
    private val messageKey: String? = null,
    private val rawValueOverride: String? = null,
    private val placeholders: Map<String, Any> = emptyMap(),
    internal val globalPlaceholders: Placeholders? = null,
    private val colorScheme: ColorScheme? = null,
    private val messageTheme: MessageTheme? = null,
    internal val messageMap: Map<String, Any>? = null,
) {
    // Legacy constructor for callers that already have a resolved string.
    internal constructor(
        rawValue: String,
        placeholders: Map<String, Any> = emptyMap(),
        globalPlaceholders: Placeholders? = null,
        colorScheme: ColorScheme? = null,
        messageTheme: MessageTheme? = null,
        messageMap: Map<String, Any>? = null,
    ) : this(
        i18n = null,
        messageKey = null,
        rawValueOverride = rawValue,
        placeholders = placeholders,
        globalPlaceholders = globalPlaceholders,
        colorScheme = colorScheme,
        messageTheme = messageTheme,
        messageMap = messageMap,
    )

    fun with(vararg pairs: Pair<String, Any>): SingleMessageResult =
        SingleMessageResult(i18n, messageKey, rawValueOverride, placeholders + pairs, globalPlaceholders, colorScheme, messageTheme, messageMap)

    fun withColorScheme(scheme: ColorScheme): SingleMessageResult =
        SingleMessageResult(i18n, messageKey, rawValueOverride, placeholders, globalPlaceholders, scheme, messageTheme, messageMap)

    fun withMessageTheme(theme: MessageTheme): SingleMessageResult =
        SingleMessageResult(i18n, messageKey, rawValueOverride, placeholders, globalPlaceholders, colorScheme, theme, messageMap)

    private fun resolveRawAndMap(): Pair<String, Map<String, Any>?> {
        if (rawValueOverride != null) {
            // Legacy / explicit raw value path. messageMap is whatever was passed in.
            return rawValueOverride to messageMap
        }
        if (i18n != null && messageKey != null) {
            // Lazy path: resolve against whatever context is active right now.
            val locale = i18n.currentLocaleStrInternal()
            val raw = i18n.resolveKey(messageKey, locale) as? String ?: "key not found: $messageKey"
            return raw to i18n.getMessageMapInternal(locale)
        }
        return ("" to messageMap)
    }

    private fun process(): String {
        val (rawValue, map) = resolveRawAndMap()
        var value = rawValue
        if (map != null) {
            value = MessageReference.resolve(value, map, placeholders)
        }
        return MessageResolver.resolvePlaceholders(value, placeholders, globalPlaceholders)
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

    fun raw(): String = resolveRawAndMap().first
}
