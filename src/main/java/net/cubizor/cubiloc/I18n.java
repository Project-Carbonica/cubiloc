package net.cubizor.cubiloc;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import net.cubizor.cubiloc.config.transformer.MessageSerdesPack;
import net.cubizor.cubicolor.api.ColorScheme;
import net.cubizor.cubicolor.exporter.MessageThemeJsonParser;
import net.cubizor.cubicolor.exporter.ThemeLoader;
import net.cubizor.cubicolor.text.MessageTheme;
import net.cubizor.cubiloc.config.MessageConfig;
import net.cubizor.cubiloc.context.I18nContext;
import net.cubizor.cubiloc.context.I18nContextHolder;
import net.cubizor.cubiloc.locale.DefaultLocaleProvider;
import net.cubizor.cubiloc.locale.LocaleProvider;
import net.cubizor.cubiloc.locale.ReflectionLocaleProvider;
import net.cubizor.cubiloc.message.ListMessageResult;
import net.cubizor.cubiloc.message.SingleMessageResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Main internationalization (i18n) system for managing message configurations.
 * Provides locale-based message retrieval with placeholder, component, and Cubicolor theme support.
 * 
 * Uses Cubicolor's native ThemeLoader for JSON-based color scheme loading.
 * 
 * Example usage:
 * <pre>
 * I18n i18n = new I18n();
 * 
 * // Register messages
 * i18n.register(MyMessages.class)
 *     .path("messages")
 *     .suffix(".yml")
 *     .defaultLocale("tr_TR")
 *     .unpack(true)
 *     .load();
 * 
 * // Load color schemes (JSON format)
 * i18n.loadColorScheme("dark", "themes/dark.json");
 * i18n.loadColorScheme("light", "themes/light.json");
 * 
 * // Get message with theme support
 * MessageResult result = i18n.get(player, messages -> messages.welcome);
 * Component component = result.with("player", playerName).component();
 * </pre>
 */
public class I18n {
    
    private final Map<String, Map<Class<? extends MessageConfig>, MessageConfig>> localeConfigs = new HashMap<>();
    private final Map<String, ColorScheme> colorSchemes = new HashMap<>();
    private final Map<String, MessageTheme> messageThemes = new HashMap<>();
    private final Map<Object, String> userSchemePreferences = new HashMap<>();
    private final List<LocaleProvider<?>> localeProviders = new ArrayList<>();
    private final ThemeLoader themeLoader;
    private final MessageThemeJsonParser messageThemeJsonParser;
    
    private Locale defaultLocale = Locale.forLanguageTag("tr-TR");
    private String defaultScheme = "dark";
    
    /**
     * Creates a new I18n instance with default locale "tr_TR"
     */
    public I18n() {
        this.themeLoader = new ThemeLoader();
        this.messageThemeJsonParser = new MessageThemeJsonParser();
        // Register default providers
        this.localeProviders.add(new DefaultLocaleProvider(defaultLocale));
        this.localeProviders.add(new ReflectionLocaleProvider(defaultLocale));
    }
    
    /**
     * Creates a new I18n instance with specified default locale
     */
    public I18n(String defaultLocale) {
        this.defaultLocale = parseLocale(defaultLocale);
        this.themeLoader = new ThemeLoader();
        this.messageThemeJsonParser = new MessageThemeJsonParser();
        // Register default providers
        this.localeProviders.add(new DefaultLocaleProvider(this.defaultLocale));
        this.localeProviders.add(new ReflectionLocaleProvider(this.defaultLocale));
    }
    
    /**
     * Creates a new I18n instance with specified default locale
     */
    public I18n(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        this.themeLoader = new ThemeLoader();
        this.messageThemeJsonParser = new MessageThemeJsonParser();
        // Register default providers
        this.localeProviders.add(new DefaultLocaleProvider(this.defaultLocale));
        this.localeProviders.add(new ReflectionLocaleProvider(this.defaultLocale));
        // Set default locale for context holder
        I18nContextHolder.setDefaultLocale(this.defaultLocale);
    }
    
    // ==================== Locale Provider Management ====================
    
    /**
     * Registers a locale provider. Providers are checked in order of registration.
     * 
     * @param provider the locale provider to register
     * @return this I18n instance for method chaining
     */
    public I18n registerLocaleProvider(LocaleProvider<?> provider) {
        // Add at the beginning to have priority over default providers
        this.localeProviders.add(0, provider);
        return this;
    }
    
    /**
     * Clears all locale providers and optionally re-adds default providers.
     * 
     * @param keepDefaults whether to keep default providers
     * @return this I18n instance for method chaining
     */
    public I18n clearLocaleProviders(boolean keepDefaults) {
        this.localeProviders.clear();
        if (keepDefaults) {
            this.localeProviders.add(new DefaultLocaleProvider(defaultLocale));
            this.localeProviders.add(new ReflectionLocaleProvider(defaultLocale));
        }
        return this;
    }
    
    /**
     * Gets the default locale.
     * 
     * @return the default locale
     */
    public Locale getDefaultLocale() {
        return defaultLocale;
    }
    
    /**
     * Sets the default locale.
     *
     * @param locale the default locale
     * @return this I18n instance for method chaining
     */
    public I18n setDefaultLocale(Locale locale) {
        this.defaultLocale = locale;
        I18nContextHolder.setDefaultLocale(locale);
        return this;
    }
    
    /**
     * Sets the default locale from a string.
     *
     * @param locale the locale string (e.g., "en_US" or "en-US")
     * @return this I18n instance for method chaining
     */
    public I18n setDefaultLocale(String locale) {
        this.defaultLocale = parseLocale(locale);
        I18nContextHolder.setDefaultLocale(this.defaultLocale);
        return this;
    }
    
    // ==================== ColorScheme Management ====================
    
    /**
     * Loads a ColorScheme from a JSON file and registers it with a key.
     * 
     * @param key unique identifier for this scheme (e.g., "dark", "light")
     * @param filePath path to the JSON file
     * @return this I18n instance for method chaining
     * @throws IOException if file cannot be read
     */
    public I18n loadColorScheme(String key, Path filePath) throws IOException {
        String content = Files.readString(filePath);
        return loadColorSchemeFromString(key, content);
    }
    
    /**
     * Loads a ColorScheme from a JSON file and registers it with a key.
     * 
     * @param key unique identifier for this scheme
     * @param file the JSON file
     * @return this I18n instance for method chaining
     * @throws IOException if file cannot be read
     */
    public I18n loadColorScheme(String key, File file) throws IOException {
        return loadColorScheme(key, file.toPath());
    }
    
    /**
     * Loads a ColorScheme from classpath resources.
     * 
     * @param key unique identifier for this scheme
     * @param resourcePath path to resource (e.g., "themes/dark.json")
     * @return this I18n instance for method chaining
     * @throws IOException if resource cannot be found or read
     */
    public I18n loadColorSchemeFromClasspath(String key, String resourcePath) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) throw new IOException("Resource not found: " + resourcePath);
            String content = new String(in.readAllBytes());
            return loadColorSchemeFromString(key, content);
        }
    }
    
    /**
     * Loads a ColorScheme from a JSON string.
     * Automatically detects if it's a MessageTheme (rich format) or a simple ColorScheme.
     * 
     * @param key unique identifier for this scheme
     * @param json the JSON string
     * @return this I18n instance for method chaining
     */
    public I18n loadColorSchemeFromString(String key, String json) {
        if (json.contains("\"messages\"")) {
            MessageTheme theme = messageThemeJsonParser.parse(json);
            messageThemes.put(key, theme);
        } else {
            ColorScheme scheme = themeLoader.loadColorSchemeFromString(json);
            colorSchemes.put(key, scheme);
        }
        return this;
    }
    
    /**
     * Registers a pre-built ColorScheme with a key.
     * 
     * @param key unique identifier for this scheme
     * @param scheme the ColorScheme to register
     * @return this I18n instance for method chaining
     */
    public I18n registerColorScheme(String key, ColorScheme scheme) {
        colorSchemes.put(key, scheme);
        return this;
    }

    /**
     * Registers a pre-built MessageTheme with a key.
     * 
     * @param key unique identifier for this theme
     * @param theme the MessageTheme to register
     * @return this I18n instance for method chaining
     */
    public I18n registerMessageTheme(String key, MessageTheme theme) {
        messageThemes.put(key, theme);
        return this;
    }

    /**
     * Loads all color schemes and message themes from a directory.
     * 
     * @param directory the directory containing .json files
     * @return this I18n instance for method chaining
     * @throws IOException if directory cannot be read
     */
    public I18n loadThemes(File directory) throws IOException {
        if (!directory.exists() || !directory.isDirectory()) return this;
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                String key = file.getName().replace(".json", "");
                loadColorScheme(key, file);
            }
        }
        return this;
    }

    /**
     * Loads all color schemes and message themes from a classpath resource path.
     * 
     * @param resourcePath the path in classpath (e.g., "themes")
     * @return this I18n instance for method chaining
     * @throws IOException if resources cannot be read
     */
    public I18n loadThemesFromClasspath(String resourcePath) throws IOException {
        try {
            ConfigRegistration<?> dummy = new ConfigRegistration<>(this, MessageConfig.class);
            java.util.Set<String> resourceFiles = dummy.findResourceFiles(resourcePath);
            for (String resourceFile : resourceFiles) {
                if (resourceFile.endsWith(".json")) {
                    String fileName = resourceFile.substring(resourceFile.lastIndexOf('/') + 1);
                    String key = fileName.replace(".json", "");
                    loadColorSchemeFromClasspath(key, resourceFile);
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to load themes from classpath: " + resourcePath, e);
        }
        return this;
    }
    
    /**
     * Gets a registered ColorScheme by key.
     * 
     * @param key the key
     * @return the ColorScheme, or null if not found
     */
    public ColorScheme getColorScheme(String key) {
        return colorSchemes.get(key);
    }

    /**
     * Gets a registered MessageTheme by key.
     * 
     * @param key the key
     * @return the MessageTheme, or null if not found
     */
    public MessageTheme getMessageTheme(String key) {
        return messageThemes.get(key);
    }
    
    /**
     * Gets the default ColorScheme.
     * 
     * @return the default ColorScheme
     */
    public ColorScheme getDefaultColorScheme() {
        return colorSchemes.get(defaultScheme);
    }

    /**
     * Gets the default MessageTheme.
     * 
     * @return the default MessageTheme
     */
    public MessageTheme getDefaultMessageTheme() {
        return messageThemes.get(defaultScheme);
    }
    
    /**
     * Gets the ColorScheme for a user, using their preference or default.
     * 
     * @param user the user object
     * @return the ColorScheme for the user
     */
    public ColorScheme getColorSchemeForUser(Object user) {
        String key = userSchemePreferences.getOrDefault(user, defaultScheme);
        ColorScheme scheme = colorSchemes.get(key);
        return scheme != null ? scheme : getDefaultColorScheme();
    }

    /**
     * Gets the MessageTheme for a user, using their preference or default.
     * 
     * @param user the user object
     * @return the MessageTheme for the user
     */
    public MessageTheme getMessageThemeForUser(Object user) {
        String key = userSchemePreferences.getOrDefault(user, defaultScheme);
        MessageTheme theme = messageThemes.get(key);
        return theme != null ? theme : getDefaultMessageTheme();
    }
    
    /**
     * Sets the color scheme preference for a user.
     * 
     * @param user the user object
     * @param schemeKey the scheme key (e.g., "dark", "light")
     * @return this I18n instance for method chaining
     */
    public I18n setUserScheme(Object user, String schemeKey) {
        userSchemePreferences.put(user, schemeKey);
        return this;
    }
    
    /**
     * Clears the color scheme preference for a user.
     * 
     * @param user the user object
     * @return this I18n instance for method chaining
     */
    public I18n clearUserScheme(Object user) {
        userSchemePreferences.remove(user);
        return this;
    }
    
    /**
     * Sets the default color scheme key.
     * 
     * @param schemeKey the default scheme key
     * @return this I18n instance for method chaining
     */
    public I18n defaultScheme(String schemeKey) {
        this.defaultScheme = schemeKey;
        return this;
    }
    
    /**
     * Gets the underlying ThemeLoader for advanced operations.
     * 
     * @return the ThemeLoader
     */
    public ThemeLoader getThemeLoader() {
        return themeLoader;
    }
    
    // ==================== Context Management ====================

    /**
     * Creates a new I18nContext for the given receiver (e.g., player).
     * This context should be used with try-with-resources for automatic cleanup.
     *
     * <p>The context enables zero-parameter message retrieval by storing
     * the receiver's locale and color scheme in ThreadLocal storage.
     *
     * <pre>
     * try (var ctx = i18n.context(player)) {
     *     MyMessages msg = i18n.config(MyMessages.class);
     *
     *     // No need to pass player - context is used automatically!
     *     Component c = msg.welcome().with("player", "Deichor").component();
     * }
     * </pre>
     *
     * <p><strong>IMPORTANT:</strong> Always use try-with-resources to prevent
     * memory leaks in thread pools!
     *
     * @param receiver the receiver object (e.g., player) that provides locale info
     * @return an AutoCloseable I18nContext
     */
    public I18nContext context(Object receiver) {
        Locale locale = resolveLocaleObject(receiver);
        ColorScheme colorScheme = getColorSchemeForUser(receiver);
        MessageTheme messageTheme = getMessageThemeForUser(receiver);

        return new I18nContext(receiver, locale, colorScheme, messageTheme);
    }

    /**
     * Creates a context with specific locale and color scheme.
     *
     * @param locale the locale for this context
     * @param colorScheme the color scheme for this context (may be null)
     * @return an AutoCloseable I18nContext
     */
    public I18nContext context(Locale locale, ColorScheme colorScheme) {
        return new I18nContext(null, locale, colorScheme, null);
    }

    /**
     * Creates a context with specific locale and message theme.
     *
     * @param locale the locale for this context
     * @param messageTheme the message theme for this context (may be null)
     * @return an AutoCloseable I18nContext
     */
    public I18nContext context(Locale locale, MessageTheme messageTheme) {
        return new I18nContext(null, locale, null, messageTheme);
    }

    /**
     * Creates a context with specific locale using default color scheme.
     *
     * @param locale the locale for this context
     * @return an AutoCloseable I18nContext
     */
    public I18nContext context(Locale locale) {
        return new I18nContext(null, locale, getDefaultColorScheme(), getDefaultMessageTheme());
    }

    // ==================== Message Registration ====================
    
    /**
     * Begins registration of a MessageConfig class.
     * 
     * @param configClass the MessageConfig class to register
     * @return a ConfigRegistration builder for configuration
     */
    public <T extends MessageConfig> ConfigRegistration<T> register(Class<T> configClass) {
        return new ConfigRegistration<>(this, configClass);
    }
    
    // ==================== Message Retrieval ====================
    
    /**
     * Gets a single-line String message.
     * Returns {@link SingleMessageResult} which provides {@link SingleMessageResult#component()}.
     * 
     * <pre>
     * i18n.get(player, msg.welcome()).with("player", "Deichor").component();
     * </pre>
     * 
     * @param localeProvider the locale provider (String locale, or object with getLocale())
     * @param message the String message from config
     * @return SingleMessageResult for further processing
     */
    public SingleMessageResult get(Object localeProvider, String message) {
        SingleMessageResult result = SingleMessageResult.of(message != null ? message : "");
        
        MessageTheme messageTheme = getMessageThemeForUser(localeProvider);
        if (messageTheme != null) {
            result.withMessageTheme(messageTheme);
        } else {
            ColorScheme colorScheme = getColorSchemeForUser(localeProvider);
            if (colorScheme != null) {
                result.withColorScheme(colorScheme);
            }
        }
        
        return result;
    }
    
    /**
     * Gets a multi-line List&lt;String&gt; message.
     * Returns {@link ListMessageResult} which provides {@link ListMessageResult#components()}.
     * 
     * <pre>
     * List&lt;Component&gt; lines = i18n.get(player, msg.helpMenu()).components();
     * </pre>
     * 
     * @param localeProvider the locale provider (String locale, or object with getLocale())
     * @param messages the List&lt;String&gt; message from config
     * @return ListMessageResult for further processing
     */
    public ListMessageResult get(Object localeProvider, List<String> messages) {
        ListMessageResult result = ListMessageResult.of(messages != null ? messages : List.of());
        
        MessageTheme messageTheme = getMessageThemeForUser(localeProvider);
        if (messageTheme != null) {
            result.withMessageTheme(messageTheme);
        } else {
            ColorScheme colorScheme = getColorSchemeForUser(localeProvider);
            if (colorScheme != null) {
                result.withColorScheme(colorScheme);
            }
        }
        
        return result;
    }
    
    /**
     * Gets a single-line String message using default locale.
     * 
     * @param message the String message from config
     * @return SingleMessageResult for further processing
     */
    public SingleMessageResult get(String message) {
        SingleMessageResult result = SingleMessageResult.of(message != null ? message : "");
        
        MessageTheme messageTheme = getDefaultMessageTheme();
        if (messageTheme != null) {
            result.withMessageTheme(messageTheme);
        } else {
            ColorScheme colorScheme = getDefaultColorScheme();
            if (colorScheme != null) {
                result.withColorScheme(colorScheme);
            }
        }
        
        return result;
    }
    
    /**
     * Gets a multi-line List&lt;String&gt; message using default locale.
     * 
     * @param messages the List&lt;String&gt; message from config
     * @return ListMessageResult for further processing
     */
    public ListMessageResult get(List<String> messages) {
        ListMessageResult result = ListMessageResult.of(messages != null ? messages : List.of());
        
        MessageTheme messageTheme = getDefaultMessageTheme();
        if (messageTheme != null) {
            result.withMessageTheme(messageTheme);
        } else {
            ColorScheme colorScheme = getDefaultColorScheme();
            if (colorScheme != null) {
                result.withColorScheme(colorScheme);
            }
        }
        
        return result;
    }
    
    /**
     * Gets the MessageConfig instance for a specific locale.
     * Use this when you need to access the config directly.
     * 
     * @param locale the locale identifier
     * @param configClass the MessageConfig class
     * @return the MessageConfig instance for the locale
     */
    @SuppressWarnings("unchecked")
    public <T extends MessageConfig> T config(String locale, Class<T> configClass) {
        Map<Class<? extends MessageConfig>, MessageConfig> localeMap = localeConfigs.get(locale);
        
        if (localeMap == null || !localeMap.containsKey(configClass)) {
            localeMap = localeConfigs.get(formatLocale(defaultLocale));
        }
        
        if (localeMap != null && localeMap.containsKey(configClass)) {
            return (T) localeMap.get(configClass);
        }
        
        throw new IllegalStateException("MessageConfig not registered: " + configClass.getName());
    }
    
    /**
     * Gets the MessageConfig instance for a specific locale.
     * 
     * @param locale the locale
     * @param configClass the MessageConfig class
     * @return the MessageConfig instance for the locale
     */
    public <T extends MessageConfig> T config(Locale locale, Class<T> configClass) {
        return config(formatLocale(locale), configClass);
    }
    
    /**
     * Gets the MessageConfig instance for a user's locale.
     * This is the primary method for getting localized messages for players.
     * 
     * @param localeProvider an object that provides locale (e.g., player)
     * @param configClass the MessageConfig class
     * @return the MessageConfig instance for the user's locale
     */
    public <T extends MessageConfig> T config(Object localeProvider, Class<T> configClass) {
        String locale = resolveLocale(localeProvider);
        return config(locale, configClass);
    }
    
    /**
     * Gets the default locale MessageConfig instance.
     * 
     * @param configClass the MessageConfig class
     * @return the MessageConfig instance for the default locale
     */
    public <T extends MessageConfig> T config(Class<T> configClass) {
        return config(formatLocale(defaultLocale), configClass);
    }
    
    /**
     * Resolves the locale from a locale provider object.
     * Uses registered LocaleProviders to resolve the locale.
     * 
     * @param localeProvider the object providing locale information
     * @return the resolved locale string (e.g., "en_US")
     */
    @SuppressWarnings("unchecked")
    protected String resolveLocale(Object localeProvider) {
        if (localeProvider == null) {
            return formatLocale(defaultLocale);
        }
        
        // Find a matching provider
        for (LocaleProvider<?> provider : localeProviders) {
            if (provider.supports(localeProvider.getClass())) {
                Locale locale = ((LocaleProvider<Object>) provider).getLocale(localeProvider);
                if (locale != null) {
                    return formatLocale(locale);
                }
            }
        }
        
        return formatLocale(defaultLocale);
    }
    
    /**
     * Resolves the locale as a Locale object from a locale provider.
     * 
     * @param localeProvider the object providing locale information
     * @return the resolved Locale
     */
    @SuppressWarnings("unchecked")
    public Locale resolveLocaleObject(Object localeProvider) {
        if (localeProvider == null) {
            return defaultLocale;
        }
        
        // Find a matching provider
        for (LocaleProvider<?> provider : localeProviders) {
            if (provider.supports(localeProvider.getClass())) {
                Locale locale = ((LocaleProvider<Object>) provider).getLocale(localeProvider);
                if (locale != null) {
                    return locale;
                }
            }
        }
        
        return defaultLocale;
    }
    
    /**
     * Formats a Locale to the string format used internally (e.g., "en_US").
     */
    private String formatLocale(Locale locale) {
        if (locale.getCountry().isEmpty()) {
            return locale.getLanguage();
        }
        return locale.getLanguage() + "_" + locale.getCountry();
    }
    
    /**
     * Parses a locale string to a Locale object.
     * Supports both "en_US" and "en-US" formats.
     */
    private Locale parseLocale(String localeStr) {
        if (localeStr == null || localeStr.isEmpty()) {
            return defaultLocale;
        }
        return Locale.forLanguageTag(localeStr.replace("_", "-"));
    }
    
    // ==================== Config Registration Builder ====================
    
    /**
     * Builder class for registering MessageConfig classes with the I18n system.
     */
    public static class ConfigRegistration<T extends MessageConfig> {
        private final I18n i18n;
        private final Class<T> configClass;
        private String path = "messages";
        private String suffix = ".yml";
        private String defaultLocaleStr = "tr_TR";
        private boolean unpack = false;
        private File dataFolder;
        private ClassLoader resourceLoader;
        
        private ConfigRegistration(I18n i18n, Class<T> configClass) {
            this.i18n = i18n;
            this.configClass = configClass;
            this.defaultLocaleStr = i18n.formatLocale(i18n.defaultLocale);
            this.resourceLoader = configClass.getClassLoader();
        }
        
        /**
         * Sets the path where message files are located
         */
        public ConfigRegistration<T> path(String path) {
            this.path = path;
            return this;
        }
        
        /**
         * Sets the file suffix (e.g., ".yml", ".yaml")
         */
        public ConfigRegistration<T> suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }
        
        /**
         * Sets the default locale
         */
        public ConfigRegistration<T> defaultLocale(String defaultLocale) {
            this.defaultLocaleStr = defaultLocale;
            this.i18n.setDefaultLocale(defaultLocale);
            return this;
        }
        
        /**
         * Sets the default locale
         */
        public ConfigRegistration<T> defaultLocale(Locale defaultLocale) {
            this.defaultLocaleStr = i18n.formatLocale(defaultLocale);
            this.i18n.setDefaultLocale(defaultLocale);
            return this;
        }
        
        /**
         * Sets whether to unpack resources from jar to file system
         */
        public ConfigRegistration<T> unpack(boolean unpack) {
            this.unpack = unpack;
            return this;
        }
        
        /**
         * Sets the data folder for file operations
         */
        public ConfigRegistration<T> dataFolder(File dataFolder) {
            this.dataFolder = dataFolder;
            return this;
        }
        
        /**
         * Sets the ClassLoader for resource loading
         */
        public ConfigRegistration<T> resourceLoader(ClassLoader loader) {
            this.resourceLoader = loader;
            return this;
        }
        
        /**
         * Loads the configuration with all specified settings
         */
        public I18n load() {
            if (dataFolder == null) {
                dataFolder = new File(".");
            }
            
            File targetDir = new File(dataFolder, path);
            targetDir.mkdirs();
            
            // Unpack resources if requested
            if (unpack) {
                unpackResources(targetDir);
            }
            
            // Load all locale files from target directory
            File[] files = targetDir.listFiles((dir, name) -> name.endsWith(suffix));
            if (files != null) {
                for (File file : files) {
                    loadLocaleFile(file);
                }
            }
            
            // Ensure default locale is loaded
            File defaultFile = new File(targetDir, defaultLocaleStr + suffix);
            if (!defaultFile.exists()) {
                try {
                    T config = ConfigManager.create(configClass, (it) -> {
                        it.withConfigurer(new YamlSnakeYamlConfigurer(), new MessageSerdesPack());
                        it.withBindFile(defaultFile);
                    });
                    config.save();
                    loadLocaleFile(defaultFile);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create default locale file: " + defaultFile, e);
                }
            }
            
            return i18n;
        }
        
        private void unpackResources(File targetDir) {
            String resourcePath = path.replace(File.separator, "/");
            
            // Try to find all resource files in the path
            try {
                // Method 1: Try to list resources from classpath
                java.util.Set<String> resourceFiles = findResourceFiles(resourcePath);
                
                for (String resourceFile : resourceFiles) {
                    if (resourceFile.endsWith(suffix)) {
                        try (InputStream in = resourceLoader.getResourceAsStream(resourceFile)) {
                            if (in != null) {
                                String fileName = resourceFile.substring(resourceFile.lastIndexOf('/') + 1);
                                Path targetFile = new File(targetDir, fileName).toPath();
                                if (!Files.exists(targetFile)) {
                                    Files.copy(in, targetFile);
                                }
                            }
                        } catch (IOException e) {
                            // Ignore individual file errors
                        }
                    }
                }
            } catch (Exception e) {
                // Fallback: try default locale only
                String resourceFile = resourcePath + "/" + defaultLocaleStr + suffix;
                try (InputStream in = resourceLoader.getResourceAsStream(resourceFile)) {
                    if (in != null) {
                        Path targetFile = new File(targetDir, defaultLocaleStr + suffix).toPath();
                        if (!Files.exists(targetFile)) {
                            Files.copy(in, targetFile);
                        }
                    }
                } catch (IOException ex) {
                    // Ignore
                }
            }
        }
        
        /**
         * Finds all resource files in the given path.
         * Works with both file system and JAR resources.
         */
        private java.util.Set<String> findResourceFiles(String resourcePath) throws Exception {
            java.util.Set<String> files = new java.util.HashSet<>();
            
            // Normalize path
            if (!resourcePath.endsWith("/")) {
                resourcePath = resourcePath + "/";
            }
            
            // Get the resource URL
            java.net.URL resourceUrl = resourceLoader.getResource(resourcePath);
            if (resourceUrl == null) {
                return files;
            }
            
            String protocol = resourceUrl.getProtocol();
            
            if ("file".equals(protocol)) {
                // Running from file system (IDE or exploded JAR)
                File dir = new File(resourceUrl.toURI());
                if (dir.isDirectory()) {
                    File[] children = dir.listFiles();
                    if (children != null) {
                        for (File child : children) {
                            if (child.isFile()) {
                                files.add(resourcePath + child.getName());
                            }
                        }
                    }
                }
            } else if ("jar".equals(protocol)) {
                // Running from JAR
                String jarPath = resourceUrl.getPath();
                // Format: file:/path/to/jar.jar!/resource/path/
                int bangIndex = jarPath.indexOf('!');
                if (bangIndex > 0) {
                    String jarFilePath = jarPath.substring(5, bangIndex); // Remove "file:"
                    try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jarFilePath)) {
                        java.util.Enumeration<java.util.jar.JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            java.util.jar.JarEntry entry = entries.nextElement();
                            String entryName = entry.getName();
                            if (entryName.startsWith(resourcePath) && !entry.isDirectory()) {
                                files.add(entryName);
                            }
                        }
                    }
                }
            }
            
            return files;
        }
        
        private void loadLocaleFile(File file) {
            try {
                String locale = file.getName().replace(suffix, "");

                T config = ConfigManager.create(configClass, (it) -> {
                    it.withConfigurer(new YamlSnakeYamlConfigurer(), new MessageSerdesPack());
                    it.withBindFile(file);
                });
                config.load();  // Load the config from file

                i18n.injectConfigIntoFields(config, config);

                // If this is the default locale and the class is a singleton (e.g. Kotlin object),
                // update the singleton instance to match the loaded config.
                if (locale.equals(defaultLocaleStr)) {
                    updateSingleton(config);
                }

                i18n.localeConfigs
                    .computeIfAbsent(locale, k -> new HashMap<>())
                    .put(configClass, config);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load locale file: " + file, e);
            }
        }

        // Package-private for testing
        void updateSingleton(T loadedConfig) {
            try {
                // Check for INSTANCE field (standard for Kotlin objects)
                java.lang.reflect.Field instanceField = configClass.getDeclaredField("INSTANCE");
                instanceField.setAccessible(true);
                Object singleton = instanceField.get(null);

                if (singleton != null) {
                    copyFields(loadedConfig, (T) singleton);
                }
            } catch (NoSuchFieldException e) {
                // Not a singleton / Kotlin object
            } catch (Exception e) {
                // Ignore other errors
            }
        }

        // Package-private for testing
        void copyFields(T source, T target) {
            Class<?> clazz = source.getClass();
            // Traverse up to MessageConfig, but not MessageConfig itself or OkaeriConfig
            while (clazz != null && clazz != MessageConfig.class && clazz != Object.class) {
                for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                    try {
                        // Skip static fields (like INSTANCE itself)
                        if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }

                        field.setAccessible(true);
                        Object value = field.get(source);
                        
                        // Only copy known message types and sub-configs
                        if (value instanceof SingleMessageResult || 
                            value instanceof ListMessageResult || 
                            value instanceof MessageConfig) {
                            field.set(target, value);
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
    }

    /**
     * Injects the root config instance into all SingleMessageResult and ListMessageResult fields
     * throughout the entire configuration hierarchy.
     */
    void injectConfigIntoFields(MessageConfig root, MessageConfig current) {
        if (current == null || root == null) return;

        Class<?> clazz = current.getClass();
        // Traverse up to MessageConfig, but not MessageConfig itself or OkaeriConfig
                while (clazz != null && clazz != MessageConfig.class && clazz != Object.class) {
                    for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                        try {
                            // Skip static fields ONLY if they are self-referencing (e.g. Singleton INSTANCE)
                            // This allows Kotlin object properties (which are static) to be processed.
                            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType() == clazz) {
                                continue;
                            }
        
                            field.setAccessible(true);
                            Object value = field.get(current);
        
                            if (value instanceof SingleMessageResult) {
                                ((SingleMessageResult) value).withConfig(root);
                            } else if (value instanceof ListMessageResult) {
                        ((ListMessageResult) value).withConfig(root);
                    } else if (value instanceof MessageConfig) {
                        // Recursively inject root into sub-configs
                        injectConfigIntoFields(root, (MessageConfig) value);
                    }
                } catch (Exception e) {
                    // Ignore errors during injection
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
