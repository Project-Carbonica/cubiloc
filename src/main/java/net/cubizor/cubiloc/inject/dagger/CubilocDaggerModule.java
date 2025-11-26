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
 * // 1. Build I18nProvider
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
 * // 2. Create Dagger component
 * {@literal @}Singleton
 * {@literal @}Component(modules = {CubilocDaggerModule.class})
 * public interface AppComponent {
 *     I18n i18n();
 *     I18nProvider i18nProvider();
 *     void inject(MyPlugin plugin);
 * }
 * 
 * // 3. Build component
 * AppComponent component = DaggerAppComponent.builder()
 *     .cubilocDaggerModule(new CubilocDaggerModule(provider))
 *     .build();
 * </pre>
 * 
 * <p>In your classes:</p>
 * <pre>
 * public class MessageService {
 *     private final I18nProvider i18nProvider;
 *     
 *     {@literal @}Inject
 *     public MessageService(I18nProvider i18nProvider) {
 *         this.i18nProvider = i18nProvider;
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
@Module
public class CubilocDaggerModule {
    
    private final I18nProvider provider;
    
    /**
     * Creates a new CubilocDaggerModule with the given I18nProvider.
     * 
     * @param provider the I18nProvider instance
     */
    public CubilocDaggerModule(I18nProvider provider) {
        this.provider = provider;
    }
    
    /**
     * Creates a new CubilocDaggerModule with the given I18n instance.
     * 
     * @param i18n the I18n instance
     */
    public CubilocDaggerModule(I18n i18n) {
        this.provider = new I18nProvider(i18n);
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
