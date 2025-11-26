package net.cubizor.cubiloc;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import net.cubizor.cubicolor.api.ColorScheme;
import net.cubizor.cubicolor.exporter.ThemeLoader;
import net.cubizor.cubiloc.config.MessageConfig;
import net.cubizor.cubiloc.message.ListMessageResult;
import net.cubizor.cubiloc.message.MessageResult;
import net.cubizor.cubiloc.message.SingleMessageResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
    private final Map<Object, String> userSchemePreferences = new HashMap<>();
    private final ThemeLoader themeLoader;
    
    private String defaultLocale = "tr_TR";
    private String defaultScheme = "dark";
    
    /**
     * Creates a new I18n instance with default locale "tr_TR"
     */
    public I18n() {
        this.themeLoader = new ThemeLoader();
    }
    
    /**
     * Creates a new I18n instance with specified default locale
     */
    public I18n(String defaultLocale) {
        this.defaultLocale = defaultLocale;
        this.themeLoader = new ThemeLoader();
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
        ColorScheme scheme = themeLoader.loadColorScheme(filePath);
        colorSchemes.put(key, scheme);
        return this;
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
        ColorScheme scheme = themeLoader.loadColorScheme(file);
        colorSchemes.put(key, scheme);
        return this;
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
        ColorScheme scheme = themeLoader.loadColorSchemeFromClasspath(resourcePath);
        colorSchemes.put(key, scheme);
        return this;
    }
    
    /**
     * Loads a ColorScheme from a JSON string.
     * 
     * @param key unique identifier for this scheme
     * @param json the JSON string
     * @return this I18n instance for method chaining
     */
    public I18n loadColorSchemeFromString(String key, String json) {
        ColorScheme scheme = themeLoader.loadColorSchemeFromString(json);
        colorSchemes.put(key, scheme);
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
     * Gets a registered ColorScheme by key.
     * 
     * @param key the key
     * @return the ColorScheme, or null if not found
     */
    public ColorScheme getColorScheme(String key) {
        return colorSchemes.get(key);
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
        ColorScheme colorScheme = getColorSchemeForUser(localeProvider);
        if (colorScheme != null) {
            result.withColorScheme(colorScheme);
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
        ColorScheme colorScheme = getColorSchemeForUser(localeProvider);
        if (colorScheme != null) {
            result.withColorScheme(colorScheme);
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
        ColorScheme colorScheme = getDefaultColorScheme();
        if (colorScheme != null) {
            result.withColorScheme(colorScheme);
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
        ColorScheme colorScheme = getDefaultColorScheme();
        if (colorScheme != null) {
            result.withColorScheme(colorScheme);
        }
        return result;
    }
    
    /**
     * @deprecated Use type-specific {@link #get(Object, String)} or {@link #get(Object, List)} instead
     */
    @Deprecated
    public MessageResult get(Object localeProvider, Object messageValue) {
        MessageResult result = MessageResult.of(messageValue != null ? messageValue : "");
        ColorScheme colorScheme = getColorSchemeForUser(localeProvider);
        if (colorScheme != null) {
            result.withColorScheme(colorScheme);
        }
        return result;
    }
    
    /**
     * @deprecated Use type-specific {@link #get(String)} or {@link #get(List)} instead
     */
    @Deprecated
    public MessageResult getAny(Object messageValue) {
        MessageResult result = MessageResult.of(messageValue != null ? messageValue : "");
        ColorScheme colorScheme = getDefaultColorScheme();
        if (colorScheme != null) {
            result.withColorScheme(colorScheme);
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
            localeMap = localeConfigs.get(defaultLocale);
        }
        
        if (localeMap != null && localeMap.containsKey(configClass)) {
            return (T) localeMap.get(configClass);
        }
        
        throw new IllegalStateException("MessageConfig not registered: " + configClass.getName());
    }
    
    /**
     * Gets the MessageConfig instance for a user's locale.
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
        return config(defaultLocale, configClass);
    }
    
    /**
     * @deprecated Use {@link #get(Object, Object)} instead
     */
    @Deprecated
    public <T extends MessageConfig> T messages(String locale, Class<T> configClass) {
        return config(locale, configClass);
    }
    
    /**
     * @deprecated Use {@link #get(Object, Object)} instead
     */
    @Deprecated
    public <T extends MessageConfig> T messages(Object localeProvider, Class<T> configClass) {
        return config(localeProvider, configClass);
    }
    
    /**
     * @deprecated Use {@link #get(Object, Object)} instead
     */
    @Deprecated
    public <T extends MessageConfig> T messages(Class<T> configClass) {
        return config(configClass);
    }
    
    /**
     * @deprecated Use type-safe get methods instead
     */
    @Deprecated
    public MessageResult msg(Object messageValue) {
        return getAny(messageValue);
    }
    
    /**
     * @deprecated Use type-safe get methods instead
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public MessageResult msg(Object messageValue, Object user) {
        if (messageValue instanceof String) {
            return MessageResult.of(get(user, (String) messageValue).asString());
        } else if (messageValue instanceof List) {
            return MessageResult.of(get(user, (List<String>) messageValue).asList());
        }
        return getAny(messageValue);
    }
    
    /**
     * @deprecated Use {@link #get(Object, Object)} instead
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public <T extends MessageConfig> MessageResult get(String locale, Function<T, Object> messageGetter) {
        // Try to find the config for this locale
        Map<Class<? extends MessageConfig>, MessageConfig> localeMap = localeConfigs.get(locale);
        
        // Fall back to default locale if not found
        if (localeMap == null || localeMap.isEmpty()) {
            localeMap = localeConfigs.get(defaultLocale);
        }
        
        // If still nothing, return empty result
        if (localeMap == null || localeMap.isEmpty()) {
            return MessageResult.of("");
        }
        
        // Get the first config
        MessageConfig config = localeMap.values().iterator().next();
        Object value = messageGetter.apply((T) config);
        
        // Create result with automatic ColorScheme if available
        MessageResult result = MessageResult.of(value != null ? value : "");
        ColorScheme colorScheme = getDefaultColorScheme();
        if (colorScheme != null) {
            result.withColorScheme(colorScheme);
        }
        
        return result;
    }
    
    /**
     * Gets a message value for a specific user with their theme preference.
     * 
     * @param user the user object (used for both locale and theme resolution)
     * @param messageGetter a function to extract the message from the config
     * @return MessageResult for further processing with user's ColorScheme applied
     */
    @SuppressWarnings("unchecked")
    public <T extends MessageConfig> MessageResult getForUser(Object user, Function<T, Object> messageGetter) {
        String locale = resolveLocale(user);
        MessageResult result = get(locale, messageGetter);
        
        // Apply user's preferred color scheme
        ColorScheme colorScheme = getColorSchemeForUser(user);
        if (colorScheme != null) {
            result.withColorScheme(colorScheme);
        }
        
        return result;
    }
    
    /**
     * Resolves the locale from a locale provider object.
     * Override this method for custom locale resolution logic.
     * 
     * @param localeProvider the object providing locale information
     * @return the resolved locale string
     */
    protected String resolveLocale(Object localeProvider) {
        if (localeProvider == null) {
            return defaultLocale;
        }
        
        if (localeProvider instanceof String) {
            return (String) localeProvider;
        }
        
        if (localeProvider instanceof Locale) {
            Locale locale = (Locale) localeProvider;
            return locale.getLanguage() + "_" + locale.getCountry();
        }
        
        // Try to find a getLocale() method
        try {
            java.lang.reflect.Method method = localeProvider.getClass().getMethod("getLocale");
            Object result = method.invoke(localeProvider);
            if (result != null) {
                return result.toString();
            }
        } catch (Exception ignored) {
        }
        
        return defaultLocale;
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
        private String defaultLocale = "tr_TR";
        private boolean unpack = false;
        private File dataFolder;
        private ClassLoader resourceLoader;
        
        private ConfigRegistration(I18n i18n, Class<T> configClass) {
            this.i18n = i18n;
            this.configClass = configClass;
            this.defaultLocale = i18n.defaultLocale;
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
            this.defaultLocale = defaultLocale;
            this.i18n.defaultLocale = defaultLocale;
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
            File defaultFile = new File(targetDir, defaultLocale + suffix);
            if (!defaultFile.exists()) {
                try {
                    T config = ConfigManager.create(configClass);
                    config.withConfigurer(new YamlSnakeYamlConfigurer());
                    config.withBindFile(defaultFile);
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
                String resourceFile = resourcePath + "/" + defaultLocale + suffix;
                try (InputStream in = resourceLoader.getResourceAsStream(resourceFile)) {
                    if (in != null) {
                        Path targetFile = new File(targetDir, defaultLocale + suffix).toPath();
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
                
                T config = ConfigManager.create(configClass);
                config.withConfigurer(new YamlSnakeYamlConfigurer());
                config.withBindFile(file);
                
                i18n.localeConfigs
                    .computeIfAbsent(locale, k -> new HashMap<>())
                    .put(configClass, config);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to load locale file: " + file, e);
            }
        }
    }
}
