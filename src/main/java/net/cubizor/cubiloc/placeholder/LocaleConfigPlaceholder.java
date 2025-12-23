package net.cubizor.cubiloc.placeholder;

import eu.okaeri.placeholders.context.PlaceholderContext;
import eu.okaeri.placeholders.message.CompiledMessage;
import net.cubizor.cubiloc.config.MessageConfig;
import net.cubizor.cubiloc.message.ListMessageResult;
import net.cubizor.cubiloc.message.SingleMessageResult;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Locale-Config placeholder processor for Cubiloc.
 * Provides {@code @lc:path.to.message} syntax for referencing other messages in the same config.
 * 
 * <h2>Syntax Patterns:</h2>
 * <ul>
 *   <li>{@code {@lc:path.to.message}} - Direct reference to another message</li>
 *   <li>{@code {value1,value2@lc#field}} - Boolean/conditional with locale-config values</li>
 *   <li>{@code {@lc:path.to.message|fallback}} - Reference with fallback</li>
 * </ul>
 * 
 * <h2>Examples:</h2>
 * <pre>
 * # In your MessageConfig:
 * status_active: "&lt;success&gt;Active&lt;/success&gt;"
 * status_inactive: "&lt;error&gt;Inactive&lt;/error&gt;"
 * 
 * # Usage in messages:
 * player_status: "Status: {@lc:status_active}"
 * dynamic_status: "Status: {status_active,status_inactive@lc#is_online}"
 * </pre>
 * 
 * <h2>Nested Path Support:</h2>
 * <pre>
 * # For nested configs like errors.not_found:
 * error_message: "{@lc:errors.not_found}"
 * </pre>
 */
public class LocaleConfigPlaceholder {
    
    // Pattern for direct @lc reference: {@lc:path.to.message} or {@lc:path.to.message|fallback}
    private static final Pattern DIRECT_LC_PATTERN = Pattern.compile(
        "\\{@lc:([a-zA-Z0-9_.]+)(?:\\|([^}]*))?\\}"
    );
    
    // Pattern for conditional @lc: {value1,value2@lc#field} 
    private static final Pattern CONDITIONAL_LC_PATTERN = Pattern.compile(
        "\\{([a-zA-Z0-9_.]+),([a-zA-Z0-9_.]+)@lc#([a-zA-Z0-9_.]+)\\}"
    );
    
    private LocaleConfigPlaceholder() {
        // Utility class
    }
    
    /**
     * Processes a message string and resolves all @lc references.
     * This should be called BEFORE okaeri-placeholders processing.
     * 
     * @param message the message to process
     * @param config the MessageConfig to resolve references from
     * @param placeholders current placeholder values (for conditional resolution)
     * @return the processed message with @lc references resolved
     */
    public static String process(String message, MessageConfig config, Map<String, Object> placeholders) {
        if (message == null || config == null) {
            return message;
        }
        
        String result = message;
        
        // Process conditional @lc patterns first: {value1,value2@lc#field}
        result = processConditionalPatterns(result, config, placeholders);
        
        // Then process direct @lc references: {@lc:path}
        result = processDirectPatterns(result, config);
        
        return result;
    }
    
    /**
     * Processes direct @lc references like {@lc:path.to.message}
     */
    private static String processDirectPatterns(String message, MessageConfig config) {
        Matcher matcher = DIRECT_LC_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String path = matcher.group(1);
            String fallback = matcher.group(2);
            
            String resolved = resolveConfigPath(config, path);
            if (resolved == null && fallback != null) {
                resolved = fallback;
            }
            if (resolved == null) {
                resolved = "{@lc:" + path + "}"; // Keep original if not found
            }
            
            matcher.appendReplacement(sb, Matcher.quoteReplacement(resolved));
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
    
    /**
     * Processes conditional @lc patterns like {value1,value2@lc#field}
     */
    private static String processConditionalPatterns(String message, MessageConfig config, Map<String, Object> placeholders) {
        Matcher matcher = CONDITIONAL_LC_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String truePath = matcher.group(1);
            String falsePath = matcher.group(2);
            String fieldName = matcher.group(3);
            
            // Get the field value from placeholders
            Object fieldValue = getNestedValue(placeholders, fieldName);
            boolean condition = toBoolean(fieldValue);
            
            // Resolve the appropriate config path
            String selectedPath = condition ? truePath : falsePath;
            String resolved = resolveConfigPath(config, selectedPath);
            
            if (resolved == null) {
                resolved = selectedPath; // Use path as literal if not found
            }
            
            matcher.appendReplacement(sb, Matcher.quoteReplacement(resolved));
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
    
    /**
     * Resolves a dot-separated path in the MessageConfig.
     * Supports paths like "status_active" or "errors.not_found" or "admin.maintenance.enabled"
     */
    private static String resolveConfigPath(MessageConfig config, String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        
        String[] parts = path.split("\\.");
        Object current = config;
        
        try {
            for (String part : parts) {
                if (current == null) {
                    return null;
                }
                current = getFieldValue(current, part);
            }
            
            // Convert result to string
            if (current instanceof String) {
                return (String) current;
            } else if (current instanceof List) {
                return String.join("\n", (List<String>) current);
            } else if (current instanceof SingleMessageResult) {
                return ((SingleMessageResult) current).raw();
            } else if (current instanceof ListMessageResult) {
                return String.join("\n", ((ListMessageResult) current).raw());
            } else if (current != null) {
                return current.toString();
            }
        } catch (Exception e) {
            // Field not found or access error
            return null;
        }
        
        return null;
    }
    
    /**
     * Gets a field value from an object using reflection.
     * First tries getter method (fieldName()), then direct field access.
     */
    private static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null || fieldName == null) {
            return null;
        }
        
        Class<?> clazz = obj.getClass();
        
        // Try fluent getter first (e.g., welcome() instead of getWelcome())
        try {
            java.lang.reflect.Method method = clazz.getMethod(fieldName);
            return method.invoke(obj);
        } catch (NoSuchMethodException e) {
            // Try standard getter
            try {
                String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                java.lang.reflect.Method getter = clazz.getMethod(getterName);
                return getter.invoke(obj);
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
        
        // Try direct field access
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                java.lang.reflect.Field field = currentClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception ignored) {
                // Field not found in this class, try superclass
            }
            currentClass = currentClass.getSuperclass();
        }
        
        return null;
    }
    
    /**
     * Gets a nested value from a map using dot notation.
     */
    @SuppressWarnings("unchecked")
    private static Object getNestedValue(Map<String, Object> map, String path) {
        if (map == null || path == null) {
            return null;
        }
        
        String[] parts = path.split("\\.");
        Object current = map;
        
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }
        
        return current;
    }
    
    /**
     * Converts a value to boolean for conditional checks.
     */
    private static boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        }
        if (value instanceof String) {
            String str = ((String) value).toLowerCase();
            return "true".equals(str) || "yes".equals(str) || "1".equals(str) || 
                   "on".equals(str) || "active".equals(str) || "enabled".equals(str);
        }
        return true; // Non-null object defaults to true
    }
}
