package net.cubizor.cubiloc.locale

import java.util.Locale

class DefaultLocaleProvider(
    private val fallbackLocale: Locale = Locale.ENGLISH,
) : LocaleProvider<Any> {

    override fun supports(type: Class<*>): Boolean =
        String::class.java.isAssignableFrom(type) || Locale::class.java.isAssignableFrom(type)

    override fun getLocale(entity: Any): Locale? = when (entity) {
        is Locale -> entity
        is String -> Locale.forLanguageTag(entity.replace("_", "-"))
        else -> fallbackLocale
    }
}
