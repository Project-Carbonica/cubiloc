package net.cubizor.cubiloc.locale

import java.util.Locale

interface LocaleProvider<T> {
    fun supports(type: Class<*>): Boolean
    fun getLocale(entity: T): Locale?
}
