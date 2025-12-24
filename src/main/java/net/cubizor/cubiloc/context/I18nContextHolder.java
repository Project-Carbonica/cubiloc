package net.cubizor.cubiloc.context;

import net.cubizor.cubicolor.api.ColorScheme;
import net.cubizor.cubicolor.text.MessageTheme;
import java.util.Locale;

/**
 * Thread-local holder for {@link I18nContext}.
 */
public final class I18nContextHolder {

    private static final ThreadLocal<I18nContext> CONTEXT_HOLDER = new ThreadLocal<>();
    private static Locale defaultLocale = Locale.forLanguageTag("tr-TR");
    private static ColorScheme defaultColorScheme;
    private static MessageTheme defaultMessageTheme;

    private I18nContextHolder() {}

    public static I18nContext get() {
        I18nContext context = CONTEXT_HOLDER.get();
        if (context == null) {
            return I18nContext.createDefault(defaultLocale, defaultColorScheme, defaultMessageTheme);
        }
        return context;
    }

    public static I18nContext getOrNull() {
        return CONTEXT_HOLDER.get();
    }

    static void set(I18nContext context) {
        if (context == null) {
            CONTEXT_HOLDER.remove();
        } else {
            CONTEXT_HOLDER.set(context);
        }
    }

    static void clear() {
        CONTEXT_HOLDER.remove();
    }

    public static void setDefaultLocale(Locale locale) {
        defaultLocale = locale;
    }

    public static void setDefaultColorScheme(ColorScheme scheme) {
        defaultColorScheme = scheme;
    }

    public static void setDefaultMessageTheme(MessageTheme theme) {
        defaultMessageTheme = theme;
    }
}
