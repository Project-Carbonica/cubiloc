package net.cubizor.cubiloc.message;

import eu.okaeri.placeholders.context.PlaceholderContext;
import eu.okaeri.placeholders.message.CompiledMessage;
import net.cubizor.cubicolor.api.ColorScheme;
import net.cubizor.cubiloc.color.ColorSchemeTagResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents a message result that can be processed with placeholders and converted to Adventure components.
 * This class wraps messages (single or multi-line) and provides fluent API for placeholder replacement
 * and component conversion.
 * 
 * Supports Cubicolor ColorScheme for semantic color tags like:
 * &lt;primary&gt;, &lt;secondary&gt;, &lt;error&gt;, &lt;success&gt;, etc.
 */
public class MessageResult {
    
    private final Object rawValue;
    private final PlaceholderContext context;
    private final Map<String, Object> placeholders = new HashMap<>();
    private ColorScheme colorScheme;
    private boolean processed = false;
    private Object processedValue;
    
    private MessageResult(Object rawValue) {
        this.rawValue = rawValue;
        
        if (rawValue instanceof String) {
            CompiledMessage compiled = CompiledMessage.of((String) rawValue);
            this.context = PlaceholderContext.of(compiled);
        } else if (rawValue instanceof List) {
            // For lists, we'll handle placeholders per-item
            this.context = null;
        } else {
            this.context = null;
        }
    }
    
    /**
     * Creates a MessageResult from a raw value (String or List<String>)
     */
    public static MessageResult of(Object value) {
        return new MessageResult(value);
    }
    
    /**
     * Adds a placeholder value to the context.
     * Supports okaeri-placeholders' .with() functionality.
     * 
     * @param key the placeholder key
     * @param value the placeholder value
     * @return this MessageResult for method chaining
     */
    public MessageResult with(String key, Object value) {
        placeholders.put(key, value);
        if (this.context != null) {
            this.context.with(key, value);
        }
        return this;
    }
    
    /**
     * Sets the ColorScheme to use for color tag resolution.
     * Enables semantic color tags like &lt;primary&gt;, &lt;error&gt;, &lt;success&gt;, etc.
     * 
     * @param colorScheme the ColorScheme to use
     * @return this MessageResult for method chaining
     */
    public MessageResult withColorScheme(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
        return this;
    }
    
    /**
     * Processes the message with all added placeholders.
     * This is called automatically when needed, but can be called explicitly.
     * 
     * @return this MessageResult for method chaining
     */
    private MessageResult process() {
        if (processed) {
            return this;
        }
        
        if (rawValue instanceof String) {
            processedValue = context != null ? context.apply() : rawValue;
        } else if (rawValue instanceof List) {
            List<String> originalList = (List<String>) rawValue;
            List<String> processedList = new ArrayList<>();
            
            for (String line : originalList) {
                CompiledMessage compiled = CompiledMessage.of(line);
                PlaceholderContext lineContext = PlaceholderContext.of(compiled);
                
                // Copy all placeholders to this line's context
                for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
                    lineContext.with(entry.getKey(), entry.getValue());
                }
                
                processedList.add(lineContext.apply());
            }
            processedValue = processedList;
        } else {
            processedValue = rawValue;
        }
        
        processed = true;
        return this;
    }
    
    /**
     * Converts the message to a Kyori Adventure Component using MiniMessage format.
     * Automatically processes placeholders before conversion.
     * If a ColorScheme is set, color tags like &lt;primary&gt;, &lt;error&gt;, etc. will be resolved.
     * For multi-line messages, joins them with newlines. Use {@link #components()} for List&lt;Component&gt;.
     * 
     * @return the Adventure Component
     */
    public Component component() {
        process();
        
        // Build tag resolver with optional ColorScheme support
        TagResolver resolver = colorScheme != null 
            ? ColorSchemeTagResolver.of(colorScheme)
            : TagResolver.empty();
        
        MiniMessage miniMessage = MiniMessage.builder()
            .tags(TagResolver.resolver(TagResolver.standard(), resolver))
            .postProcessor(component -> component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
            .build();
        
        if (processedValue instanceof String) {
            return miniMessage.deserialize((String) processedValue);
        } else if (processedValue instanceof List) {
            List<String> lines = (List<String>) processedValue;
            if (lines.isEmpty()) {
                return Component.empty();
            }
            
            Component result = miniMessage.deserialize(lines.get(0));
            for (int i = 1; i < lines.size(); i++) {
                result = result.append(Component.newline())
                               .append(miniMessage.deserialize(lines.get(i)));
            }
            return result;
        }
        
        return Component.text(String.valueOf(processedValue));
    }
    
    /**
     * Converts the message to a List of Kyori Adventure Components.
     * Each line becomes a separate Component - ideal for multi-line messages.
     * If a ColorScheme is set, color tags like &lt;primary&gt;, &lt;error&gt;, etc. will be resolved.
     * 
     * @return List of Adventure Components, one per line
     */
    public List<Component> components() {
        process();
        
        TagResolver resolver = colorScheme != null 
            ? ColorSchemeTagResolver.of(colorScheme)
            : TagResolver.empty();
        
        MiniMessage miniMessage = MiniMessage.builder()
            .tags(TagResolver.resolver(TagResolver.standard(), resolver))
            .postProcessor(component -> component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
            .build();
        
        List<Component> result = new ArrayList<>();
        
        if (processedValue instanceof String) {
            result.add(miniMessage.deserialize((String) processedValue));
        } else if (processedValue instanceof List) {
            for (String line : (List<String>) processedValue) {
                result.add(miniMessage.deserialize(line));
            }
        } else {
            result.add(Component.text(String.valueOf(processedValue)));
        }
        
        return result;
    }

    /**
     * Converts the message to a Kyori Adventure Component using a custom deserializer.
     * Automatically processes placeholders before conversion.
     * Note: Custom deserializers bypass ColorScheme tag resolution. Use component() for ColorScheme support.
     * 
     * @param deserializer the function to convert string to Component
     * @return the Adventure Component
     */
    public Component component(Function<String, Component> deserializer) {
        process();
        
        if (processedValue instanceof String) {
            return deserializer.apply((String) processedValue);
        } else if (processedValue instanceof List) {
            List<String> lines = (List<String>) processedValue;
            if (lines.isEmpty()) {
                return Component.empty();
            }
            
            Component result = deserializer.apply(lines.get(0));
            for (int i = 1; i < lines.size(); i++) {
                result = result.append(Component.newline())
                               .append(deserializer.apply(lines.get(i)));
            }
            return result;
        }
        
        return Component.text(String.valueOf(processedValue));
    }

    /**
     * Converts the message to a Kyori Adventure Component using MiniMessage with custom TagResolver.
     * This allows combining ColorScheme tags with additional custom tags.
     * 
     * @param additionalResolver additional TagResolver to combine with ColorScheme tags
     * @return the Adventure Component
     */
    public Component component(TagResolver additionalResolver) {
        process();
        
        TagResolver colorResolver = colorScheme != null 
            ? ColorSchemeTagResolver.of(colorScheme)
            : TagResolver.empty();
        
        MiniMessage miniMessage = MiniMessage.builder()
            .tags(TagResolver.resolver(TagResolver.standard(), colorResolver, additionalResolver))
            .build();
        
        if (processedValue instanceof String) {
            return miniMessage.deserialize((String) processedValue);
        } else if (processedValue instanceof List) {
            List<String> lines = (List<String>) processedValue;
            if (lines.isEmpty()) {
                return Component.empty();
            }
            
            Component result = miniMessage.deserialize(lines.get(0));
            for (int i = 1; i < lines.size(); i++) {
                result = result.append(Component.newline())
                               .append(miniMessage.deserialize(lines.get(i)));
            }
            return result;
        }
        
        return Component.text(String.valueOf(processedValue));
    }
    
    /**
     * Converts the message to a Kyori Adventure Component using legacy color codes (&).
     * 
     * @return the Adventure Component
     */
    public Component componentLegacy() {
        return component(LegacyComponentSerializer.legacyAmpersand()::deserialize);
    }
    
    /**
     * Returns the processed message as a String.
     * For multi-line messages, joins them with newlines.
     * 
     * @return the processed string
     */
    public String asString() {
        process();
        
        if (processedValue instanceof String) {
            return (String) processedValue;
        } else if (processedValue instanceof List) {
            return String.join("\n", (List<String>) processedValue);
        }
        
        return String.valueOf(processedValue);
    }
    
    /**
     * Returns the processed message as a List of Strings.
     * For single-line messages, returns a singleton list.
     * 
     * @return the processed list
     */
    public List<String> asList() {
        process();
        
        if (processedValue instanceof List) {
            return new ArrayList<>((List<String>) processedValue);
        } else if (processedValue instanceof String) {
            List<String> result = new ArrayList<>();
            result.add((String) processedValue);
            return result;
        }
        
        List<String> result = new ArrayList<>();
        result.add(String.valueOf(processedValue));
        return result;
    }
    
    /**
     * Returns the raw unprocessed value.
     * 
     * @return the raw value
     */
    public Object raw() {
        return rawValue;
    }
}
