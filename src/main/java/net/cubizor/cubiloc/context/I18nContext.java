package net.cubizor.cubiloc.context;

import net.cubizor.cubicolor.api.ColorScheme;
import net.cubizor.cubicolor.text.MessageTheme;
import net.cubizor.cubiloc.config.MessageConfig;

import java.util.Locale;

/**
 * Context for internationalization (i18n) operations.
 * Holds the current locale, color scheme, and message config for a thread.
 *
 * This class is thread-local and should be used with try-with-resources:
 * <pre>
 * try (var ctx = i18n.context(player)) {
 *     // Use messages without passing player around
 *     MyMessages msg = i18n.config(MyMessages.class);
 *     msg.welcome().with("player", "Deichor").component();
 * }
 * </pre>
 *
 * @see I18nContextHolder
 */
public final class I18nContext implements AutoCloseable {

    private final Object receiver;
    private final Locale locale;
    private final ColorScheme colorScheme;
    private final MessageTheme messageTheme;
    private final boolean shouldRestore;
    private final I18nContext previousContext;

    /**
     * Creates a new I18nContext and sets it as the current context.
     *
     * @param receiver the receiver object (e.g., player)
     * @param locale the locale for this context
     * @param colorScheme the color scheme for this context
     */
    public I18nContext(Object receiver, Locale locale, ColorScheme colorScheme) {
        this(receiver, locale, colorScheme, null);
    }

    /**
     * Creates a new I18nContext and sets it as the current context.
     *
     * @param receiver the receiver object (e.g., player)
     * @param locale the locale for this context
     * @param colorScheme the color scheme for this context
     * @param messageTheme the message theme for this context
     */
    public I18nContext(Object receiver, Locale locale, ColorScheme colorScheme, MessageTheme messageTheme) {
        this.receiver = receiver;
        this.locale = locale;
        this.colorScheme = colorScheme;
        this.messageTheme = messageTheme;
        this.previousContext = I18nContextHolder.get();
        this.shouldRestore = previousContext != null;

        // Set this context as current
        I18nContextHolder.set(this);
    }

    /**
     * Creates a default I18nContext with default locale and no color scheme.
     * Used as fallback when no context is set.
     */
    static I18nContext createDefault(Locale defaultLocale) {
        return new I18nContext(null, defaultLocale, null, null, false);
    }

    private I18nContext(Object receiver, Locale locale, ColorScheme colorScheme, MessageTheme messageTheme, boolean shouldRestore) {
        this.receiver = receiver;
        this.locale = locale;
        this.colorScheme = colorScheme;
        this.messageTheme = messageTheme;
        this.previousContext = null;
        this.shouldRestore = shouldRestore;
    }

    /**
     * Gets the receiver object for this context.
     *
     * @return the receiver (e.g., player), may be null for default context
     */
    public Object getReceiver() {
        return receiver;
    }

    /**
     * Gets the locale for this context.
     *
     * @return the locale, never null
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Gets the color scheme for this context.
     *
     * @return the color scheme, may be null
     */
    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    /**
     * Gets the message theme for this context.
     *
     * @return the message theme, may be null
     */
    public MessageTheme getMessageTheme() {
        return messageTheme;
    }

    /**
     * Closes this context and restores the previous context if any.
     * This method is automatically called when using try-with-resources.
     */
    @Override
    public void close() {
        if (shouldRestore) {
            I18nContextHolder.set(previousContext);
        } else {
            I18nContextHolder.clear();
        }
    }

    @Override
    public String toString() {
        return "I18nContext{" +
                "receiver=" + receiver +
                ", locale=" + locale +
                ", colorScheme=" + (colorScheme != null ? "present" : "null") +
                '}';
    }
}
