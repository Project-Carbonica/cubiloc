package net.cubizor.cubiloc.inject.guice;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import net.cubizor.cubiloc.I18n;
import net.cubizor.cubiloc.config.MessageConfig;
import net.cubizor.cubiloc.inject.I18nProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Extended Guice module that allows direct injection of MessageConfig classes.
 * 
 * <p>This module provides two features:</p>
 * <ul>
 *   <li>Inject {@code I18n} and {@code I18nProvider} as singletons</li>
 *   <li>Register MessageConfig classes for direct injection</li>
 * </ul>
 * 
 * <p>Usage example:</p>
 * <pre>
 * // Create the module with config bindings
 * CubilocConfigModule module = CubilocConfigModule.builder(provider)
 *     .bindConfig(MyMessages.class)  // default locale binding
 *     .bindConfig("tr_TR", MyMessages.class)  // locale-specific binding
 *     .build();
 * 
 * Injector injector = Guice.createInjector(module);
 * </pre>
 * 
 * <p>Injecting configs:</p>
 * <pre>
 * public class MyService {
 *     private final MyMessages messages;
 *     
 *     {@literal @}Inject
 *     public MyService(MyMessages messages) {
 *         this.messages = messages;
 *     }
 * }
 * 
 * // With locale-specific injection using @Named
 * public class LocalizedService {
 *     private final MyMessages turkishMessages;
 *     private final MyMessages englishMessages;
 *     
 *     {@literal @}Inject
 *     public LocalizedService(
 *         {@literal @}Named("tr_TR") MyMessages turkishMessages,
 *         {@literal @}Named("en_US") MyMessages englishMessages
 *     ) {
 *         this.turkishMessages = turkishMessages;
 *         this.englishMessages = englishMessages;
 *     }
 * }
 * </pre>
 */
public class CubilocConfigModule extends AbstractModule {
    
    private final I18nProvider provider;
    private final Map<Class<? extends MessageConfig>, String> configBindings;
    private final Map<LocaleConfigKey<?>, String> localeConfigBindings;
    
    private CubilocConfigModule(Builder builder) {
        this.provider = builder.provider;
        this.configBindings = new HashMap<>(builder.configBindings);
        this.localeConfigBindings = new HashMap<>(builder.localeConfigBindings);
    }
    
    /**
     * Creates a new builder for CubilocConfigModule.
     * 
     * @param provider the I18nProvider instance
     * @return the builder
     */
    public static Builder builder(I18nProvider provider) {
        return new Builder(provider);
    }
    
    /**
     * Creates a new builder for CubilocConfigModule.
     * 
     * @param i18n the I18n instance
     * @return the builder
     */
    public static Builder builder(I18n i18n) {
        return new Builder(new I18nProvider(i18n));
    }
    
    @Override
    protected void configure() {
        // Bind I18n and I18nProvider
        bind(I18nProvider.class).toInstance(provider);
        bind(I18n.class).toInstance(provider.i18n());
        
        // Bind default locale configs
        for (Map.Entry<Class<? extends MessageConfig>, String> entry : configBindings.entrySet()) {
            bindConfigClass(entry.getKey());
        }
        
        // Bind locale-specific configs with @Named annotation
        for (Map.Entry<LocaleConfigKey<?>, String> entry : localeConfigBindings.entrySet()) {
            bindLocaleConfigClass(entry.getKey().configClass, entry.getKey().locale);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T extends MessageConfig> void bindConfigClass(Class<T> configClass) {
        bind(configClass).toProvider(() -> provider.config(configClass));
    }
    
    @SuppressWarnings("unchecked")
    private <T extends MessageConfig> void bindLocaleConfigClass(Class<T> configClass, String locale) {
        bind(configClass)
            .annotatedWith(Names.named(locale))
            .toProvider(() -> provider.config(locale, configClass));
    }
    
    /**
     * Builder for CubilocConfigModule.
     */
    public static class Builder {
        private final I18nProvider provider;
        private final Map<Class<? extends MessageConfig>, String> configBindings = new HashMap<>();
        private final Map<LocaleConfigKey<?>, String> localeConfigBindings = new HashMap<>();
        
        Builder(I18nProvider provider) {
            this.provider = provider;
        }
        
        /**
         * Binds a MessageConfig class for injection using the default locale.
         * 
         * @param configClass the MessageConfig class to bind
         * @return this builder
         */
        public <T extends MessageConfig> Builder bindConfig(Class<T> configClass) {
            configBindings.put(configClass, "default");
            return this;
        }
        
        /**
         * Binds a MessageConfig class for injection with a specific locale.
         * Use {@code @Named("locale")} to inject the locale-specific config.
         * 
         * @param locale the locale identifier
         * @param configClass the MessageConfig class to bind
         * @return this builder
         */
        public <T extends MessageConfig> Builder bindConfig(String locale, Class<T> configClass) {
            localeConfigBindings.put(new LocaleConfigKey<>(locale, configClass), locale);
            return this;
        }
        
        /**
         * Builds the CubilocConfigModule.
         * 
         * @return the configured module
         */
        public CubilocConfigModule build() {
            return new CubilocConfigModule(this);
        }
    }
    
    /**
     * Key class for locale-specific config bindings.
     */
    private static class LocaleConfigKey<T extends MessageConfig> {
        final String locale;
        final Class<T> configClass;
        
        LocaleConfigKey(String locale, Class<T> configClass) {
            this.locale = locale;
            this.configClass = configClass;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocaleConfigKey<?> that = (LocaleConfigKey<?>) o;
            return locale.equals(that.locale) && configClass.equals(that.configClass);
        }
        
        @Override
        public int hashCode() {
            return 31 * locale.hashCode() + configClass.hashCode();
        }
    }
}
