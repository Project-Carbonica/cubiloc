package net.cubizor.cubiloc.inject;

import net.cubizor.cubicolor.api.ColorScheme;
import net.cubizor.cubiloc.I18n;
import net.cubizor.cubiloc.config.MessageConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating I18n instances with dependency injection support.
 * Use this class to configure and create an I18nProvider for DI frameworks.
 * 
 * <p>Example usage:</p>
 * <pre>
 * I18nProvider provider = I18nBuilder.create("tr_TR")
 *     .register(MyMessages.class)
 *         .path("messages")
 *         .suffix(".yml")
 *         .unpack(true)
 *         .dataFolder(dataFolder)
 *         .done()
 *     .loadColorScheme("dark", "themes/dark.json")
 *     .loadColorScheme("light", "themes/light.json")
 *     .defaultScheme("dark")
 *     .buildProvider();
 * </pre>
 * 
 * <p>For Guice:</p>
 * <pre>
 * public class I18nModule extends AbstractModule {
 *     private final I18nProvider provider;
 *     
 *     public I18nModule(I18nProvider provider) {
 *         this.provider = provider;
 *     }
 *     
 *     {@literal @}Override
 *     protected void configure() {
 *         bind(I18nProvider.class).toInstance(provider);
 *         bind(I18n.class).toInstance(provider.i18n());
 *     }
 * }
 * </pre>
 */
public class I18nBuilder {
    
    private final I18n i18n;
    private final List<ConfigRegistrationBuilder<?>> registrations = new ArrayList<>();
    private final List<ColorSchemeLoader> colorSchemes = new ArrayList<>();
    private String defaultScheme;
    
    private I18nBuilder(String defaultLocale) {
        this.i18n = new I18n(defaultLocale);
    }
    
    /**
     * Creates a new I18nBuilder with the specified default locale.
     * 
     * @param defaultLocale the default locale (e.g., "tr_TR", "en_US")
     * @return the builder
     */
    public static I18nBuilder create(String defaultLocale) {
        return new I18nBuilder(defaultLocale);
    }
    
    /**
     * Creates a new I18nBuilder with default locale "tr_TR".
     * 
     * @return the builder
     */
    public static I18nBuilder create() {
        return new I18nBuilder("tr_TR");
    }
    
    /**
     * Begins registration of a MessageConfig class.
     * 
     * @param configClass the MessageConfig class to register
     * @return a ConfigRegistrationBuilder for configuration
     */
    public <T extends MessageConfig> ConfigRegistrationBuilder<T> register(Class<T> configClass) {
        ConfigRegistrationBuilder<T> registration = new ConfigRegistrationBuilder<>(this, configClass);
        registrations.add(registration);
        return registration;
    }
    
    /**
     * Loads a ColorScheme from classpath resources.
     * 
     * @param key unique identifier for this scheme (e.g., "dark", "light")
     * @param resourcePath path to resource (e.g., "themes/dark.json")
     * @return this builder
     */
    public I18nBuilder loadColorScheme(String key, String resourcePath) {
        colorSchemes.add(new ColorSchemeLoader(key, resourcePath, ColorSchemeLoader.Type.CLASSPATH));
        return this;
    }
    
    /**
     * Loads a ColorScheme from a file.
     * 
     * @param key unique identifier for this scheme
     * @param file the JSON file
     * @return this builder
     */
    public I18nBuilder loadColorScheme(String key, File file) {
        colorSchemes.add(new ColorSchemeLoader(key, file.getAbsolutePath(), ColorSchemeLoader.Type.FILE));
        return this;
    }
    
    /**
     * Registers a pre-built ColorScheme.
     * 
     * @param key unique identifier for this scheme
     * @param scheme the ColorScheme to register
     * @return this builder
     */
    public I18nBuilder registerColorScheme(String key, ColorScheme scheme) {
        colorSchemes.add(new ColorSchemeLoader(key, scheme));
        return this;
    }
    
    /**
     * Sets the default color scheme key.
     * 
     * @param schemeKey the default scheme key
     * @return this builder
     */
    public I18nBuilder defaultScheme(String schemeKey) {
        this.defaultScheme = schemeKey;
        return this;
    }
    
    /**
     * Builds and returns the configured I18n instance.
     * 
     * @return the configured I18n instance
     */
    public I18n build() {
        // Load all message configs
        for (ConfigRegistrationBuilder<?> registration : registrations) {
            registration.apply(i18n);
        }
        
        // Load all color schemes
        for (ColorSchemeLoader loader : colorSchemes) {
            try {
                loader.load(i18n);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load color scheme: " + loader.key, e);
            }
        }
        
        // Set default scheme
        if (defaultScheme != null) {
            i18n.defaultScheme(defaultScheme);
        }
        
        return i18n;
    }
    
    /**
     * Builds and returns an I18nProvider wrapping the configured I18n instance.
     * Use this for dependency injection.
     * 
     * @return the I18nProvider
     */
    public I18nProvider buildProvider() {
        return new I18nProvider(build());
    }
    
    /**
     * Builder for MessageConfig registration within I18nBuilder.
     */
    public static class ConfigRegistrationBuilder<T extends MessageConfig> {
        private final I18nBuilder parent;
        private final Class<T> configClass;
        private String path = "messages";
        private String suffix = ".yml";
        private String defaultLocale;
        private boolean unpack = false;
        private File dataFolder;
        private ClassLoader resourceLoader;
        
        ConfigRegistrationBuilder(I18nBuilder parent, Class<T> configClass) {
            this.parent = parent;
            this.configClass = configClass;
            this.resourceLoader = configClass.getClassLoader();
        }
        
        /**
         * Sets the path where message files are located.
         */
        public ConfigRegistrationBuilder<T> path(String path) {
            this.path = path;
            return this;
        }
        
        /**
         * Sets the file suffix (e.g., ".yml", ".yaml").
         */
        public ConfigRegistrationBuilder<T> suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }
        
        /**
         * Sets the default locale for this config.
         */
        public ConfigRegistrationBuilder<T> defaultLocale(String defaultLocale) {
            this.defaultLocale = defaultLocale;
            return this;
        }
        
        /**
         * Sets whether to unpack resources from jar to file system.
         */
        public ConfigRegistrationBuilder<T> unpack(boolean unpack) {
            this.unpack = unpack;
            return this;
        }
        
        /**
         * Sets the data folder for file operations.
         */
        public ConfigRegistrationBuilder<T> dataFolder(File dataFolder) {
            this.dataFolder = dataFolder;
            return this;
        }
        
        /**
         * Sets the ClassLoader for resource loading.
         */
        public ConfigRegistrationBuilder<T> resourceLoader(ClassLoader loader) {
            this.resourceLoader = loader;
            return this;
        }
        
        /**
         * Completes this registration and returns to the parent builder.
         */
        public I18nBuilder done() {
            return parent;
        }
        
        /**
         * Applies this registration to the I18n instance.
         */
        void apply(I18n i18n) {
            I18n.ConfigRegistration<T> registration = i18n.register(configClass);
            
            registration.path(path).suffix(suffix).unpack(unpack);
            
            if (defaultLocale != null) {
                registration.defaultLocale(defaultLocale);
            }
            if (dataFolder != null) {
                registration.dataFolder(dataFolder);
            }
            if (resourceLoader != null) {
                registration.resourceLoader(resourceLoader);
            }
            
            registration.load();
        }
    }
    
    /**
     * Internal class for loading color schemes.
     */
    private static class ColorSchemeLoader {
        enum Type { CLASSPATH, FILE, INSTANCE }
        
        final String key;
        final String path;
        final Type type;
        final ColorScheme scheme;
        
        ColorSchemeLoader(String key, String path, Type type) {
            this.key = key;
            this.path = path;
            this.type = type;
            this.scheme = null;
        }
        
        ColorSchemeLoader(String key, ColorScheme scheme) {
            this.key = key;
            this.path = null;
            this.type = Type.INSTANCE;
            this.scheme = scheme;
        }
        
        void load(I18n i18n) throws IOException {
            switch (type) {
                case CLASSPATH:
                    i18n.loadColorSchemeFromClasspath(key, path);
                    break;
                case FILE:
                    i18n.loadColorScheme(key, new File(path));
                    break;
                case INSTANCE:
                    i18n.registerColorScheme(key, scheme);
                    break;
            }
        }
    }
}
