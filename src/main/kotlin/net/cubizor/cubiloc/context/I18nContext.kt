package net.cubizor.cubiloc.context

import net.cubizor.cubicolor.api.ColorScheme
import net.cubizor.cubicolor.text.MessageTheme
import java.util.Locale

class I18nContext private constructor(
    val receiver: Any?,
    val locale: Locale?,
    val colorScheme: ColorScheme?,
    val messageTheme: MessageTheme?,
    private val managed: Boolean,
) : AutoCloseable {

    private val previousContext: I18nContext? = if (managed) I18nContextHolder.getOrNull() else null

    init {
        if (managed) I18nContextHolder.set(this)
    }

    constructor(
        receiver: Any?,
        locale: Locale?,
        colorScheme: ColorScheme?,
        messageTheme: MessageTheme?,
    ) : this(receiver, locale, colorScheme, messageTheme, managed = true)

    override fun close() {
        if (!managed) return
        if (previousContext != null) {
            I18nContextHolder.set(previousContext)
        } else {
            I18nContextHolder.clear()
        }
    }

    override fun toString(): String =
        "I18nContext(receiver=$receiver, locale=$locale, colorScheme=${if (colorScheme != null) "present" else "null"})"

    companion object {
        internal fun createDefault(
            locale: Locale?,
            colorScheme: ColorScheme?,
            messageTheme: MessageTheme?,
        ): I18nContext = I18nContext(null, locale, colorScheme, messageTheme, managed = false)
    }
}
