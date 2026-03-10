package net.cubizor.cubiloc.locale

import java.util.Locale

class PlayerLocaleProvider(
    private val fallbackLocale: Locale = Locale.ENGLISH,
) : LocaleProvider<Any> {

    private val playerClass: Class<*>? = findPlayerClass()

    override fun supports(type: Class<*>): Boolean =
        playerClass?.isAssignableFrom(type) == true

    override fun getLocale(entity: Any): Locale? {
        System.getProperty("cubiloc.forcedLocale")?.let {
            return Locale.forLanguageTag(it)
        }
        tryMethod(entity, "locale", Locale::class.java)?.let { return it }
        tryMethod(entity, "getLocale", Locale::class.java)?.let { return it }
        tryMethod(entity, "getEffectiveLocale", Locale::class.java)?.let { return it }
        tryMethod(entity, "getLocale", String::class.java)?.let { str ->
            return Locale.forLanguageTag(str.replace("_", "-"))
        }
        return fallbackLocale
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> tryMethod(entity: Any, methodName: String, returnType: Class<T>): T? = try {
        val method = entity.javaClass.getMethod(methodName)
        val result = method.invoke(entity)
        if (returnType.isInstance(result)) result as T else null
    } catch (_: Exception) {
        null
    }

    companion object {
        private fun findPlayerClass(): Class<*>? {
            val classNames = listOf(
                "org.bukkit.entity.Player",
                "net.md_5.bungee.api.connection.ProxiedPlayer",
                "com.velocitypowered.api.proxy.Player",
            )
            for (name in classNames) {
                try { return Class.forName(name) } catch (_: ClassNotFoundException) { }
            }
            return null
        }
    }
}
