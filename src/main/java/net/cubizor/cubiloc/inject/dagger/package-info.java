/**
 * Dagger 2 integration for Cubiloc I18n.
 * 
 * <p>This package provides Dagger 2 modules for DI integration:</p>
 * <ul>
 *   <li>{@link net.cubizor.cubiloc.inject.dagger.CubilocDaggerModule} - Dagger module for I18n binding</li>
 * </ul>
 * 
 * <h2>Setup</h2>
 * <p>Add Dagger 2 dependency to your project:</p>
 * <pre>
 * // Gradle Kotlin DSL
 * dependencies {
 *     implementation("com.google.dagger:dagger:2.51.1")
 *     annotationProcessor("com.google.dagger:dagger-compiler:2.51.1")
 * }
 * </pre>
 * 
 * <h2>Basic Usage</h2>
 * <pre>
 * // 1. Create I18nProvider
 * I18nProvider provider = I18nBuilder.create("tr_TR")
 *     .register(MyMessages.class)
 *         .path("messages")
 *         .unpack(true)
 *         .dataFolder(dataFolder)
 *         .done()
 *     .loadColorScheme("dark", "themes/dark.json")
 *     .defaultScheme("dark")
 *     .buildProvider();
 * 
 * // 2. Define your Dagger Component
 * {@literal @}Singleton
 * {@literal @}Component(modules = {CubilocDaggerModule.class, YourOtherModules.class})
 * public interface AppComponent {
 *     I18n i18n();
 *     I18nProvider i18nProvider();
 *     
 *     void inject(MyPlugin plugin);
 *     void inject(MyService service);
 * }
 * 
 * // 3. Build and use
 * AppComponent component = DaggerAppComponent.builder()
 *     .cubilocDaggerModule(new CubilocDaggerModule(provider))
 *     .build();
 * 
 * component.inject(this);
 * </pre>
 * 
 * <h2>Injecting in Classes</h2>
 * <pre>
 * public class MyService {
 *     private final I18nProvider i18nProvider;
 *     
 *     {@literal @}Inject
 *     public MyService(I18nProvider i18nProvider) {
 *         this.i18nProvider = i18nProvider;
 *     }
 *     
 *     public Component getWelcomeMessage(Player player) {
 *         MyMessages msg = i18nProvider.config(player, MyMessages.class);
 *         return i18nProvider.i18n().get(player, msg.welcome())
 *             .with("player", player.getName())
 *             .component();
 *     }
 * }
 * </pre>
 * 
 * @see net.cubizor.cubiloc.inject.dagger.CubilocDaggerModule
 */
package net.cubizor.cubiloc.inject.dagger;
