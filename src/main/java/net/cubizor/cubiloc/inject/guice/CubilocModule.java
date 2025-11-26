package net.cubizor.cubiloc.inject.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import net.cubizor.cubiloc.I18n;
import net.cubizor.cubiloc.inject.I18nProvider;

/**
 * Guice module for Cubiloc I18n dependency injection.
 * 
 * <p>Usage example:</p>
 * <pre>
 * // In your plugin initialization
 * I18n i18n = I18nBuilder.create("en-US")
 *     .registerPlayerLocaleProvider()  // Auto-detect player locale
 *     .register(MyMessages.class)
 *         .path("messages")
 *         .suffix(".yml")
 *         .unpack(true)
 *         .dataFolder(dataFolder)
 *         .done()
 *     .loadColorScheme("dark", "themes/dark.json")
 *     .defaultScheme("dark")
 *     .build();
 * 
 * // Create Guice injector with I18n module
 * Injector injector = Guice.createInjector(
 *     new CubilocModule(i18n),
 *     new YourOtherModules()
 * );
 * 
 * // Now you can inject I18n anywhere
 * </pre>
 * 
 * <p>In your classes (I18n now has player-aware methods):</p>
 * <pre>
 * public class MessageService {
 *     private final I18n i18n;
 *     
 *     {@literal @}Inject
 *     public MessageService(I18n i18n) {
 *         this.i18n = i18n;
 *     }
 *     
 *     public void sendWelcome(Player player) {
 *         // Get localized config for player's locale
 *         MyMessages msg = i18n.config(player, MyMessages.class);
 *         
 *         // Build and send message
 *         Component message = i18n.get(player, msg.welcome())
 *             .with("player", player.getName())
 *             .component();
 *         player.sendMessage(message);
 *     }
 * }
 * </pre>
 */
public class CubilocModule extends AbstractModule {
    
    private final I18n i18n;
    
    /**
     * Creates a new CubilocModule with the given I18n instance.
     * 
     * @param i18n the I18n instance
     */
    public CubilocModule(I18n i18n) {
        this.i18n = i18n;
    }
    
    /**
     * Creates a new CubilocModule with the given I18nProvider.
     * 
     * @param provider the I18nProvider instance
     * @deprecated Use {@link #CubilocModule(I18n)} instead
     */
    @Deprecated
    public CubilocModule(I18nProvider provider) {
        this.i18n = provider.i18n();
    }
    
    @Override
    protected void configure() {
        // Bindings are done via @Provides methods
    }
    
    /**
     * Provides the singleton I18n instance.
     */
    @Provides
    @Singleton
    I18n provideI18n() {
        return i18n;
    }
    
    /**
     * Provides the singleton I18nProvider instance for backward compatibility.
     * 
     * @deprecated Inject I18n directly instead
     */
    @Deprecated
    @Provides
    @Singleton
    I18nProvider provideI18nProvider() {
        return new I18nProvider(i18n);
    }
}
