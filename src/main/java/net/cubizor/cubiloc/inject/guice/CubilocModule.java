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
 * I18nProvider provider = I18nBuilder.create("tr_TR")
 *     .register(MyMessages.class)
 *         .path("messages")
 *         .suffix(".yml")
 *         .unpack(true)
 *         .dataFolder(dataFolder)
 *         .done()
 *     .loadColorScheme("dark", "themes/dark.json")
 *     .defaultScheme("dark")
 *     .buildProvider();
 * 
 * // Create Guice injector with I18n module
 * Injector injector = Guice.createInjector(
 *     new CubilocModule(provider),
 *     new YourOtherModules()
 * );
 * 
 * // Now you can inject I18n, I18nProvider anywhere
 * </pre>
 * 
 * <p>In your classes:</p>
 * <pre>
 * public class MessageService {
 *     private final I18nProvider i18nProvider;
 *     private final MyMessages messages;
 *     
 *     {@literal @}Inject
 *     public MessageService(I18nProvider i18nProvider) {
 *         this.i18nProvider = i18nProvider;
 *         this.messages = i18nProvider.config(MyMessages.class);
 *     }
 *     
 *     public void sendWelcome(Player player) {
 *         MyMessages msg = i18nProvider.config(player, MyMessages.class);
 *         Component message = i18nProvider.i18n().get(player, msg.welcome())
 *             .with("player", player.getName())
 *             .component();
 *         player.sendMessage(message);
 *     }
 * }
 * </pre>
 */
public class CubilocModule extends AbstractModule {
    
    private final I18nProvider provider;
    
    /**
     * Creates a new CubilocModule with the given I18nProvider.
     * 
     * @param provider the I18nProvider instance
     */
    public CubilocModule(I18nProvider provider) {
        this.provider = provider;
    }
    
    /**
     * Creates a new CubilocModule with the given I18n instance.
     * 
     * @param i18n the I18n instance
     */
    public CubilocModule(I18n i18n) {
        this.provider = new I18nProvider(i18n);
    }
    
    @Override
    protected void configure() {
        // Bindings are done via @Provides methods
    }
    
    /**
     * Provides the singleton I18nProvider instance.
     */
    @Provides
    @Singleton
    I18nProvider provideI18nProvider() {
        return provider;
    }
    
    /**
     * Provides the singleton I18n instance.
     */
    @Provides
    @Singleton
    I18n provideI18n() {
        return provider.i18n();
    }
}
