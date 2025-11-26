package net.cubizor.cubiloc.locale;

import java.util.Locale;

/**
 * Provider interface for resolving locale from various sources.
 * Implement this interface to support custom locale resolution for different platforms.
 * 
 * <p>Example for Bukkit/Paper:</p>
 * <pre>
 * public class PlayerLocaleProvider implements LocaleProvider&lt;Player&gt; {
 *     {@literal @}Override
 *     public boolean supports(Class&lt;?&gt; type) {
 *         return Player.class.isAssignableFrom(type);
 *     }
 *     
 *     {@literal @}Override
 *     public Locale getLocale(Player entity) {
 *         return entity.locale();
 *     }
 * }
 * </pre>
 * 
 * @param <T> the type of entity this provider supports
 */
public interface LocaleProvider<T> {
    
    /**
     * Checks if this provider supports the given type.
     * 
     * @param type the class to check
     * @return true if this provider can handle the type
     */
    boolean supports(Class<?> type);
    
    /**
     * Gets the locale for the given entity.
     * 
     * @param entity the entity to get locale for
     * @return the locale, or null to use default
     */
    Locale getLocale(T entity);
}
