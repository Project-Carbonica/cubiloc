package net.cubizor.cubiloc.inject.dagger;

import dagger.Module;
import dagger.Provides;
import net.cubizor.cubiloc.I18n;
import net.cubizor.cubiloc.inject.I18nProvider;

import javax.inject.Singleton;

/**
 * Dagger 2 module for Cubiloc I18n dependency injection.
 * 
 * <p>Usage example:</p>
 * <pre>
 * // 1. Build I18n
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
 * // 2. Create Dagger component
 * {@literal @}Singleton
 * {@literal @}Component(modules = {CubilocDaggerModule.class})
 * public interface AppComponent {
 *     I18n i18n();
 *     void inject(MyPlugin plugin);
 * }
 * 
 * // 3. Build component
 * AppComponent component = DaggerAppComponent.builder()
 *     .cubilocDaggerModule(new CubilocDaggerModule(i18n))
 *     .build();
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
@Module
public class CubilocDaggerModule {
    
    private final I18n i18n;
    
    /**
     * Creates a new CubilocDaggerModule with the given I18n instance.
     * 
     * @param i18n the I18n instance
     */
    public CubilocDaggerModule(I18n i18n) {
        this.i18n = i18n;
    }
    
    /**
     * Creates a new CubilocDaggerModule with the given I18nProvider.
     * 
     * @param provider the I18nProvider instance
     * @deprecated Use {@link #CubilocDaggerModule(I18n)} instead
     */
    @Deprecated
    public CubilocDaggerModule(I18nProvider provider) {
        this.i18n = provider.i18n();
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
