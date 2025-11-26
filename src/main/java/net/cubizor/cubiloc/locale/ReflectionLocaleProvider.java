package net.cubizor.cubiloc.locale;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Fallback locale provider that uses reflection to find getLocale() or locale() methods.
 * This provider supports any object that has a locale-returning method.
 * 
 * <p>Supported method signatures:</p>
 * <ul>
 *   <li>{@code Locale getLocale()}</li>
 *   <li>{@code Locale locale()}</li>
 *   <li>{@code String getLocale()} - will be parsed</li>
 *   <li>{@code String locale()} - will be parsed</li>
 * </ul>
 */
public class ReflectionLocaleProvider implements LocaleProvider<Object> {
    
    private final Locale fallbackLocale;
    
    public ReflectionLocaleProvider() {
        this.fallbackLocale = Locale.forLanguageTag("en-US");
    }
    
    public ReflectionLocaleProvider(Locale fallbackLocale) {
        this.fallbackLocale = fallbackLocale;
    }
    
    @Override
    public boolean supports(Class<?> type) {
        // This provider is a fallback, always returns true
        return true;
    }
    
    @Override
    public Locale getLocale(Object entity) {
        if (entity == null) {
            return fallbackLocale;
        }
        
        // Try getLocale() method
        Locale result = tryMethod(entity, "getLocale");
        if (result != null) {
            return result;
        }
        
        // Try locale() method (Bukkit Paper Player uses this)
        result = tryMethod(entity, "locale");
        if (result != null) {
            return result;
        }
        
        return fallbackLocale;
    }
    
    private Locale tryMethod(Object entity, String methodName) {
        try {
            Method method = entity.getClass().getMethod(methodName);
            Object result = method.invoke(entity);
            
            if (result instanceof Locale) {
                return (Locale) result;
            }
            
            if (result instanceof String) {
                String localeStr = ((String) result).replace("_", "-");
                return Locale.forLanguageTag(localeStr);
            }
        } catch (Exception ignored) {
            // Method not found or failed, continue
        }
        return null;
    }
}
