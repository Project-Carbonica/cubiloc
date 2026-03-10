package net.cubizor.cubiloc.context

import net.cubizor.cubicolor.api.ColorScheme
import net.cubizor.cubicolor.text.MessageTheme
import java.util.Locale

object I18nContextHolder {

    private val holder = ThreadLocal<I18nContext>()

    @JvmStatic
    var defaultLocale: Locale? = null

    @JvmStatic
    var defaultColorScheme: ColorScheme? = null

    @JvmStatic
    var defaultMessageTheme: MessageTheme? = null

    @JvmStatic
    fun get(): I18nContext =
        holder.get() ?: I18nContext.createDefault(defaultLocale, defaultColorScheme, defaultMessageTheme)

    @JvmStatic
    fun getOrNull(): I18nContext? = holder.get()

    @JvmStatic
    internal fun set(context: I18nContext?) {
        if (context == null) holder.remove() else holder.set(context)
    }

    @JvmStatic
    internal fun clear() = holder.remove()
}
