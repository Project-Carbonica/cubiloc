package net.cubizor.cubiloc.message

import eu.okaeri.placeholders.Placeholders
import eu.okaeri.placeholders.message.CompiledMessage
import net.cubizor.cubicolor.api.ColorScheme
import net.cubizor.cubicolor.text.MessageTheme
import net.cubizor.cubiloc.color.ColorSchemeTagResolver
import net.cubizor.cubiloc.color.MessageThemeTagResolver
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

internal object MessageResolver {

    fun buildMiniMessage(
        colorScheme: ColorScheme?,
        messageTheme: MessageTheme?,
        vararg additionalResolvers: TagResolver,
    ): MiniMessage {
        val themeResolver = when {
            messageTheme != null -> MessageThemeTagResolver.of(messageTheme)
            colorScheme != null -> ColorSchemeTagResolver.of(colorScheme)
            else -> TagResolver.empty()
        }
        val resolvers = listOf(TagResolver.standard(), themeResolver) + additionalResolvers
        return MiniMessage.builder()
            .tags(TagResolver.resolver(resolvers))
            .postProcessor { it.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE) }
            .build()
    }

    fun resolvePlaceholders(value: String, placeholders: Map<String, Any>, global: Placeholders?): String {
        val compiled = CompiledMessage.of(value)
        val instance = global ?: Placeholders.create()
        val ctx = instance.contextOf(compiled)

        for ((k, v) in expandDottedKeys(placeholders)) {
            ctx.with(k, v)
        }

        ctx.placeholders.fallbackResolver { parent, field, _ ->
            val name = field.unsafe().name
            when {
                parent is Map<*, *> -> parent[name]
                placeholders.containsKey(name) -> placeholders[name]
                else -> null
            }
        }

        return ctx.apply()
    }

    @Suppress("UNCHECKED_CAST")
    fun expandDottedKeys(source: Map<String, Any>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        for ((key, value) in source) {
            if ("." in key) {
                val parts = key.split(".")
                var current = result
                for (i in 0 until parts.size - 1) {
                    val next = current.getOrPut(parts[i]) { mutableMapOf<String, Any>() }
                    current = if (next is MutableMap<*, *>) {
                        next as MutableMap<String, Any>
                    } else {
                        mutableMapOf<String, Any>().also { current[parts[i]] = it }
                    }
                }
                current[parts.last()] = value
            } else {
                result[key] = value
            }
        }
        return result
    }
}
