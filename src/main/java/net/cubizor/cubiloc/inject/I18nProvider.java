package net.cubizor.cubiloc.inject;

import net.cubizor.cubiloc.I18n;
import net.cubizor.cubiloc.config.MessageConfig;

/**
 * Provider interface for I18n dependency injection support.
 * Provides access to the I18n instance and MessageConfig classes.
 * 
 * <p>Example usage with Guice:</p>
 * <pre>
 * public class MyPlugin {
 *     {@literal @}Inject
 *     private I18nProvider i18nProvider;
 *     
 *     public void sendMessage(Player player) {
 *         MyMessages msg = i18nProvider.config(player, MyMessages.class);
 *         Component message = i18nProvider.i18n().get(player, msg.welcome())
 *             .with("player", player.getName())
 *             .component();
 *         player.sendMessage(message);
 *     }
 * }
 * </pre>
 */
public class I18nProvider {
    
    private final I18n i18n;
    
    /**
     * Creates a new I18nProvider wrapping the given I18n instance.
     * 
     * @param i18n the I18n instance to wrap
     */
    public I18nProvider(I18n i18n) {
        this.i18n = i18n;
    }
    
    /**
     * Gets the underlying I18n instance.
     * 
     * @return the I18n instance
     */
    public I18n i18n() {
        return i18n;
    }
    
    /**
     * Gets the MessageConfig instance for a specific locale.
     * 
     * @param locale the locale identifier
     * @param configClass the MessageConfig class
     * @return the MessageConfig instance for the locale
     */
    public <T extends MessageConfig> T config(String locale, Class<T> configClass) {
        return i18n.config(locale, configClass);
    }
    
    /**
     * Gets the MessageConfig instance for a user's locale.
     * 
     * @param localeProvider an object that provides locale (e.g., player)
     * @param configClass the MessageConfig class
     * @return the MessageConfig instance for the user's locale
     */
    public <T extends MessageConfig> T config(Object localeProvider, Class<T> configClass) {
        return i18n.config(localeProvider, configClass);
    }
    
    /**
     * Gets the default locale MessageConfig instance.
     * 
     * @param configClass the MessageConfig class
     * @return the MessageConfig instance for the default locale
     */
    public <T extends MessageConfig> T config(Class<T> configClass) {
        return i18n.config(configClass);
    }
}
