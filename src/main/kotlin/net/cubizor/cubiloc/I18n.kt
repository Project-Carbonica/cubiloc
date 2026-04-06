package net.cubizor.cubiloc

import eu.okaeri.placeholders.Placeholders
import net.cubizor.cubicolor.api.ColorScheme
import net.cubizor.cubicolor.exporter.MessageThemeJsonParser
import net.cubizor.cubicolor.exporter.ThemeLoader
import net.cubizor.cubicolor.text.MessageTheme
import net.cubizor.cubiloc.context.I18nContext
import net.cubizor.cubiloc.context.I18nContextHolder
import net.cubizor.cubiloc.locale.DefaultLocaleProvider
import net.cubizor.cubiloc.locale.LocaleProvider
import net.cubizor.cubiloc.locale.ReflectionLocaleProvider
import net.cubizor.cubiloc.message.ListMessageResult
import net.cubizor.cubiloc.message.SingleMessageResult
import java.io.File
import java.io.IOException
import java.util.Locale

class I18n(val defaultLocale: Locale) {

    private val localeMessages = mutableMapOf<String, Map<String, Any>>()
    private val colorSchemes = mutableMapOf<String, ColorScheme>()
    private val messageThemes = mutableMapOf<String, MessageTheme>()
    private val userSchemePreferences = mutableMapOf<Any, String>()
    private val localeProviders = mutableListOf<LocaleProvider<*>>()
    private val themeLoader = ThemeLoader()
    private val messageThemeJsonParser = MessageThemeJsonParser()
    val placeholders: Placeholders = Placeholders.create()
    private var defaultSchemeName = "dark"

    constructor(defaultLocale: String) : this(parseLocale(defaultLocale))

    init {
        localeProviders.add(DefaultLocaleProvider(defaultLocale))
        localeProviders.add(ReflectionLocaleProvider(defaultLocale))
        I18nContextHolder.defaultLocale = defaultLocale
    }

    // ==================== Locale Providers ====================

    fun registerLocaleProvider(provider: LocaleProvider<*>): I18n {
        localeProviders.add(0, provider)
        return this
    }

    // ==================== Message Loading ====================

    fun loadMessages(path: String, dataFolder: File): I18n {
        val dir = File(dataFolder, path)
        dir.mkdirs()
        dir.listFiles { _, name -> name.endsWith(".yml") || name.endsWith(".yaml") }?.forEach { file ->
            val locale = file.nameWithoutExtension
            localeMessages[locale] = YamlMessageLoader.load(file)
        }
        return this
    }

    // ==================== Message Access ====================

    fun message(key: String): SingleMessageResult {
        val locale = currentLocaleStr()
        val rawValue = resolveKey(key, locale) as? String ?: "key not found: $key"
        return SingleMessageResult(
            rawValue = rawValue,
            globalPlaceholders = placeholders,
            messageMap = getMessageMap(locale),
        )
    }

    fun list(key: String): ListMessageResult {
        val locale = currentLocaleStr()
        val rawValue = resolveKey(key, locale)
        val lines = when (rawValue) {
            is List<*> -> rawValue.map { it.toString() }
            is String -> listOf(rawValue)
            else -> listOf("key not found: $key")
        }
        return ListMessageResult(
            rawValues = lines,
            globalPlaceholders = placeholders,
            messageMap = getMessageMap(locale),
        )
    }

    // ==================== Color Schemes & Themes ====================

    fun loadColorSchemeFromString(key: String, json: String): I18n {
        if (json.contains("\"messages\"")) {
            val theme = messageThemeJsonParser.parse(json)
            messageThemes[key] = theme
            if (key == defaultSchemeName) I18nContextHolder.defaultMessageTheme = theme
        } else {
            val scheme = themeLoader.loadColorSchemeFromString(json)
            colorSchemes[key] = scheme
            if (key == defaultSchemeName) I18nContextHolder.defaultColorScheme = scheme
        }
        return this
    }

    fun loadColorSchemeFromClasspath(key: String, resourcePath: String): I18n {
        val content = javaClass.classLoader.getResourceAsStream(resourcePath)
            ?.use { String(it.readAllBytes()) }
            ?: throw IOException("Resource not found: $resourcePath")
        return loadColorSchemeFromString(key, content)
    }

    fun loadColorScheme(key: String, file: File): I18n =
        loadColorSchemeFromString(key, file.readText())

    fun loadThemesFromClasspath(dir: String): I18n {
        try { loadColorSchemeFromClasspath("dark", "$dir/dark.json") } catch (_: IOException) { }
        try { loadColorSchemeFromClasspath("light", "$dir/light.json") } catch (_: IOException) { }
        return this
    }

    fun defaultScheme(key: String): I18n {
        defaultSchemeName = key
        I18nContextHolder.defaultColorScheme = colorSchemes[key]
        I18nContextHolder.defaultMessageTheme = messageThemes[key]
        return this
    }

    fun setUserScheme(user: Any, schemeKey: String): I18n {
        userSchemePreferences[user] = schemeKey
        return this
    }

    fun clearUserScheme(user: Any): I18n {
        userSchemePreferences.remove(user)
        return this
    }

    fun getColorScheme(key: String): ColorScheme? = colorSchemes[key]
    fun getMessageTheme(key: String): MessageTheme? = messageThemes[key]
    fun getDefaultColorScheme(): ColorScheme? = colorSchemes[defaultSchemeName]
    fun getDefaultMessageTheme(): MessageTheme? = messageThemes[defaultSchemeName]

    fun getColorSchemeForUser(user: Any): ColorScheme? {
        val key = userSchemePreferences[user] ?: defaultSchemeName
        return colorSchemes[key] ?: colorSchemes[defaultSchemeName]
    }

    fun getMessageThemeForUser(user: Any): MessageTheme? {
        val key = userSchemePreferences[user] ?: defaultSchemeName
        return messageThemes[key] ?: messageThemes[defaultSchemeName]
    }

    // ==================== Context ====================

    fun context(receiver: Any): I18nContext {
        val locale = resolveLocale(receiver)
        val scheme = getColorSchemeForUser(receiver)
        val theme = getMessageThemeForUser(receiver)
        return I18nContext(receiver, locale, scheme, theme)
    }

    fun context(receiver: Any, theme: MessageTheme): I18nContext {
        val locale = resolveLocale(receiver)
        val scheme = getColorSchemeForUser(receiver)
        return I18nContext(receiver, locale, scheme, theme)
    }

    fun context(receiver: Any, scheme: ColorScheme): I18nContext {
        val locale = resolveLocale(receiver)
        val theme = getMessageThemeForUser(receiver)
        return I18nContext(receiver, locale, scheme, theme)
    }

    // ==================== Locale Resolution ====================

    @Suppress("UNCHECKED_CAST")
    fun resolveLocale(obj: Any?): Locale {
        if (obj == null) return defaultLocale
        for (provider in localeProviders) {
            if (provider.supports(obj.javaClass)) {
                val locale = (provider as LocaleProvider<Any>).getLocale(obj)
                if (locale != null) return locale
            }
        }
        return defaultLocale
    }

    // ==================== Internal ====================

    private fun currentLocaleStr(): String {
        val context = I18nContextHolder.getOrNull()
        return formatLocale(context?.locale ?: defaultLocale)
    }

    private fun resolveKey(key: String, locale: String): Any? =
        localeMessages[locale]?.get(key)
            ?: localeMessages[formatLocale(defaultLocale)]?.get(key)

    private fun getMessageMap(locale: String): Map<String, Any> {
        val defaultMap = localeMessages[formatLocale(defaultLocale)] ?: emptyMap()
        val localeMap = localeMessages[locale] ?: return defaultMap
        return defaultMap + localeMap
    }

    companion object {
        @JvmStatic
        fun formatLocale(locale: Locale): String =
            if (locale.country.isEmpty()) locale.language
            else "${locale.language}_${locale.country}"

        private fun parseLocale(localeStr: String): Locale =
            Locale.forLanguageTag(localeStr.replace("_", "-"))
    }
}
