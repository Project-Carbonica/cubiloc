/**
 * Google Guice integration for Cubiloc I18n.
 * 
 * <p>This package provides Guice modules for DI integration:</p>
 * <ul>
 *   <li>{@link net.cubizor.cubiloc.inject.guice.CubilocModule} - Basic module for I18n binding</li>
 *   <li>{@link net.cubizor.cubiloc.inject.guice.CubilocConfigModule} - Extended module with MessageConfig binding</li>
 * </ul>
 * 
 * <h2>Basic Usage</h2>
 * <pre>
 * // Create provider
 * I18nProvider provider = I18nBuilder.create("tr_TR")
 *     .register(MyMessages.class)
 *         .path("messages")
 *         .unpack(true)
 *         .dataFolder(dataFolder)
 *         .done()
 *     .buildProvider();
 * 
 * // Basic module - inject I18n and I18nProvider
 * Injector injector = Guice.createInjector(new CubilocModule(provider));
 * </pre>
 * 
 * <h2>Advanced Usage with Config Binding</h2>
 * <pre>
 * // Extended module - also inject MessageConfig classes directly
 * CubilocConfigModule module = CubilocConfigModule.builder(provider)
 *     .bindConfig(MyMessages.class)           // default locale
 *     .bindConfig("tr_TR", MyMessages.class)  // @Named("tr_TR")
 *     .bindConfig("en_US", MyMessages.class)  // @Named("en_US")
 *     .build();
 * 
 * Injector injector = Guice.createInjector(module);
 * 
 * // Now you can inject configs directly
 * public class MyService {
 *     {@literal @}Inject
 *     public MyService(MyMessages messages) {
 *         // messages is the default locale config
 *     }
 * }
 * 
 * // Or with @Named for specific locale
 * public class LocalizedService {
 *     {@literal @}Inject
 *     public LocalizedService({@literal @}Named("tr_TR") MyMessages turkishMessages) {
 *         // turkishMessages is the Turkish locale config
 *     }
 * }
 * </pre>
 * 
 * @see net.cubizor.cubiloc.inject.guice.CubilocModule
 * @see net.cubizor.cubiloc.inject.guice.CubilocConfigModule
 */
package net.cubizor.cubiloc.inject.guice;
