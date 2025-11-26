package net.cubizor.cubiloc.locale;

import java.util.Locale;

/**
 * Default locale provider that handles common types.
 * Supports String locale codes and java.util.Locale objects.
 */
public class DefaultLocaleProvider implements LocaleProvider<Object> {
    
    private final Locale fallbackLocale;
    
    public DefaultLocaleProvider() {
        this.fallbackLocale = Locale.forLanguageTag("en-US");
    }
    
    public DefaultLocaleProvider(Locale fallbackLocale) {
        this.fallbackLocale = fallbackLocale;
    }
    
    @Override
    public boolean supports(Class<?> type) {
        return String.class.isAssignableFrom(type) 
            || Locale.class.isAssignableFrom(type);
    }
    
    @Override
    public Locale getLocale(Object entity) {
        if (entity == null) {
            return fallbackLocale;
        }
        
        if (entity instanceof Locale) {
            return (Locale) entity;
        }
        
        if (entity instanceof String) {
            String localeStr = (String) entity;
            // Handle both "en_US" and "en-US" formats
            localeStr = localeStr.replace("_", "-");
            return Locale.forLanguageTag(localeStr);
        }
        
        return fallbackLocale;
    }
}
