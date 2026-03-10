package net.cubizor.cubiloc.locale

import java.util.Locale

class ReflectionLocaleProvider(
    private val fallbackLocale: Locale = Locale.ENGLISH,
) : LocaleProvider<Any> {

    override fun supports(type: Class<*>): Boolean = true

    override fun getLocale(entity: Any): Locale? {
        tryMethod(entity, "locale")?.let { return it }
        tryMethod(entity, "getLocale")?.let { return it }
        return fallbackLocale
    }

    private fun tryMethod(entity: Any, methodName: String): Locale? = try {
        val method = entity.javaClass.getMethod(methodName)
        when (val result = method.invoke(entity)) {
            is Locale -> result
            is String -> Locale.forLanguageTag(result.replace("_", "-"))
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}
