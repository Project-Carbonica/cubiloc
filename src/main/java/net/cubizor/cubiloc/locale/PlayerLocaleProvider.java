package net.cubizor.cubiloc.locale;

import java.util.Locale;

/**
 * Locale provider for Paper/Bukkit Player objects.
 * Uses the player's client locale setting.
 * 
 * <p>This provider uses reflection to support both Paper API's {@code Player.locale()}
 * and legacy Bukkit's locale methods without requiring Paper as a compile-time dependency.</p>
 * 
 * <p>Usage:</p>
 * <pre>
 * I18n i18n = new I18n("en-US");
 * i18n.registerLocaleProvider(new PlayerLocaleProvider());
 * 
 * // Now you can use Player objects directly
 * MyMessages messages = i18n.config(player, MyMessages.class);
 * </pre>
 */
public class PlayerLocaleProvider implements LocaleProvider<Object> {
    
    private static final String FORCED_LOCALE = System.getProperty("cubiloc.forcedLocale");
    
    private final Locale fallbackLocale;
    private final Class<?> playerClass;
    
    /**
     * Creates a PlayerLocaleProvider with the default fallback locale (en-US).
     */
    public PlayerLocaleProvider() {
        this(Locale.forLanguageTag("en-US"));
    }
    
    /**
     * Creates a PlayerLocaleProvider with a custom fallback locale.
     * 
     * @param fallbackLocale the locale to use when player locale is unavailable
     */
    public PlayerLocaleProvider(Locale fallbackLocale) {
        this.fallbackLocale = fallbackLocale;
        this.playerClass = findPlayerClass();
    }
    
    private Class<?> findPlayerClass() {
        // Try Paper first, then Bukkit
        String[] classNames = {
            "org.bukkit.entity.Player",
            "net.md_5.bungee.api.connection.ProxiedPlayer",
            "com.velocitypowered.api.proxy.Player"
        };
        
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ignored) {
            }
        }
        
        return null;
    }
    
    @Override
    public boolean supports(Class<?> type) {
        if (playerClass == null) {
            return false;
        }
        return playerClass.isAssignableFrom(type);
    }
    
    @Override
    public Locale getLocale(Object entity) {
        if (FORCED_LOCALE != null) {
            return Locale.forLanguageTag(FORCED_LOCALE);
        }
        
        if (entity == null) {
            return fallbackLocale;
        }
        
        // Try Paper's locale() method first
        Locale result = tryMethod(entity, "locale", Locale.class);
        if (result != null) {
            return result;
        }
        
        // Try getLocale() method
        result = tryMethod(entity, "getLocale", Locale.class);
        if (result != null) {
            return result;
        }
        
        // Try Velocity's getEffectiveLocale()
        result = tryMethod(entity, "getEffectiveLocale", Locale.class);
        if (result != null) {
            return result;
        }
        
        // Try legacy Bukkit getLocale() that returns String
        String localeStr = tryMethod(entity, "getLocale", String.class);
        if (localeStr != null && !localeStr.isEmpty()) {
            return Locale.forLanguageTag(localeStr.replace("_", "-"));
        }
        
        return fallbackLocale;
    }
    
    @SuppressWarnings("unchecked")
    private <T> T tryMethod(Object entity, String methodName, Class<T> returnType) {
        try {
            java.lang.reflect.Method method = entity.getClass().getMethod(methodName);
            Object result = method.invoke(entity);
            if (returnType.isInstance(result)) {
                return (T) result;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
