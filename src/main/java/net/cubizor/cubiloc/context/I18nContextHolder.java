package net.cubizor.cubiloc.context;

import java.util.Locale;

/**
 * Thread-local holder for {@link I18nContext}.
 *
 * This class manages the current I18nContext for each thread, allowing
 * message operations to automatically use the correct locale and color scheme
 * without explicitly passing them around.
 *
 * <p><strong>IMPORTANT:</strong> Always use try-with-resources when setting a context:
 * <pre>
 * try (var ctx = i18n.context(player)) {
 *     // Context is automatically set and cleared
 * }
 * </pre>
 *
 * <p>Memory Safety: The ThreadLocal is cleaned up automatically when the context
 * is closed. Manual cleanup can be done with {@link #clear()}.
 *
 * @see I18nContext
 */
public final class I18nContextHolder {

    private static final ThreadLocal<I18nContext> CONTEXT_HOLDER = new ThreadLocal<>();
    private static Locale defaultLocale = Locale.forLanguageTag("tr-TR");

    /**
     * Private constructor - this is a utility class.
     */
    private I18nContextHolder() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Gets the current I18nContext for this thread.
     * If no context is set, returns a default context.
     *
     * @return the current context, never null
     */
    public static I18nContext get() {
        I18nContext context = CONTEXT_HOLDER.get();
        if (context == null) {
            // Return default context instead of null
            return I18nContext.createDefault(defaultLocale);
        }
        return context;
    }

    /**
     * Gets the current I18nContext, or null if none is set.
     * Use this when you need to check if a context exists.
     *
     * @return the current context, or null
     */
    public static I18nContext getOrNull() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * Sets the current I18nContext for this thread.
     *
     * <p><strong>WARNING:</strong> This is an internal method.
     * Use {@code i18n.context(receiver)} instead.
     *
     * @param context the context to set
     */
    static void set(I18nContext context) {
        if (context == null) {
            CONTEXT_HOLDER.remove();
        } else {
            CONTEXT_HOLDER.set(context);
        }
    }

    /**
     * Clears the current context for this thread.
     *
     * <p><strong>IMPORTANT:</strong> This removes the ThreadLocal value to prevent
     * memory leaks in thread pools. This is called automatically by
     * {@link I18nContext#close()}.
     */
    static void clear() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * Sets the default locale used when no context is present.
     * This is called by I18n during initialization.
     *
     * @param locale the default locale
     */
    public static void setDefaultLocale(Locale locale) {
        defaultLocale = locale;
    }

    /**
     * Gets the default locale.
     *
     * @return the default locale
     */
    public static Locale getDefaultLocale() {
        return defaultLocale;
    }
}
