/**
 * Dependency Injection support for Cubiloc I18n.
 * 
 * <p>This package provides classes for integrating Cubiloc with DI frameworks:</p>
 * <ul>
 *   <li>{@link net.cubizor.cubiloc.inject.I18nProvider} - Wrapper for I18n with convenient methods</li>
 *   <li>{@link net.cubizor.cubiloc.inject.I18nBuilder} - Builder for creating I18n with DI support</li>
 * </ul>
 * 
 * <p>For framework-specific modules:</p>
 * <ul>
 *   <li>{@link net.cubizor.cubiloc.inject.guice.CubilocModule} - Basic Guice module</li>
 *   <li>{@link net.cubizor.cubiloc.inject.guice.CubilocConfigModule} - Extended Guice module with config binding</li>
 * </ul>
 * 
 * <h2>Quick Start with Guice</h2>
 * <pre>
 * // 1. Build I18nProvider
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
 * // 2. Create Guice injector
 * Injector injector = Guice.createInjector(
 *     new CubilocModule(provider)
 * );
 * 
 * // 3. Inject in your classes
 * public class MyService {
 *     {@literal @}Inject
 *     private I18nProvider i18nProvider;
 *     
 *     public void sendMessage(Player player) {
 *         MyMessages msg = i18nProvider.config(player, MyMessages.class);
 *         player.sendMessage(i18nProvider.i18n().get(player, msg.welcome()).component());
 *     }
 * }
 * </pre>
 * 
 * @see net.cubizor.cubiloc.inject.I18nProvider
 * @see net.cubizor.cubiloc.inject.I18nBuilder
 * @see net.cubizor.cubiloc.inject.guice.CubilocModule
 */
package net.cubizor.cubiloc.inject;
