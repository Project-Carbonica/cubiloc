package net.cubizor.cubiloc.message;

import eu.okaeri.placeholders.context.PlaceholderContext;
import eu.okaeri.placeholders.message.CompiledMessage;
import net.cubizor.cubicolor.api.ColorScheme;
import net.cubizor.cubiloc.color.ColorSchemeTagResolver;
import net.cubizor.cubiloc.config.MessageConfig;
import net.cubizor.cubiloc.placeholder.LocaleConfigPlaceholder;
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
 * Message result for multi-line List&lt;String&gt; messages.
 * Provides {@link #components()} for converting to List&lt;Component&gt;
 * and {@link #component()} for a single joined Component.
 * 
 * Supports @lc (locale-config) placeholders:
 * <ul>
 *   <li>{@code {@lc:path.to.message}} - Reference another message from the config</li>
 *   <li>{@code {trueValue,falseValue@lc#field}} - Conditional with config values</li>
 * </ul>
 */
public class ListMessageResult {
    
    private final List<String> rawValue;
    private final Map<String, Object> placeholders = new HashMap<>();
    private ColorScheme colorScheme;
    private MessageConfig messageConfig;
    private boolean processed = false;
    private List<String> processedValue;
    
    ListMessageResult(List<String> rawValue) {
        this.rawValue = new ArrayList<>(rawValue);
    }
    
    /**
     * Creates a ListMessageResult from a List&lt;String&gt; value.
     */
    public static ListMessageResult of(List<String> value) {
        return new ListMessageResult(value);
    }
    
    /**
     * Adds a placeholder value.
     */
    public ListMessageResult with(String key, Object value) {
        placeholders.put(key, value);
        return this;
    }
    
    /**
     * Sets the ColorScheme for semantic color tags.
     */
    public ListMessageResult withColorScheme(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
        return this;
    }
    
    /**
     * Sets the MessageConfig for @lc placeholder resolution.
     */
    public ListMessageResult withConfig(MessageConfig config) {
        this.messageConfig = config;
        return this;
    }
    
    private void process() {
        if (processed) return;
        
        processedValue = new ArrayList<>();
        for (String line : rawValue) {
            String value = line;
            
            // First, resolve @lc placeholders if config is available
            if (messageConfig != null) {
                value = LocaleConfigPlaceholder.process(value, messageConfig, placeholders);
            }
            
            // Then, apply okaeri-placeholders
            CompiledMessage compiled = CompiledMessage.of(value);
            PlaceholderContext lineContext = PlaceholderContext.of(compiled);
            
            for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
                lineContext.with(entry.getKey(), entry.getValue());
            }
            
            processedValue.add(lineContext.apply());
        }
        processed = true;
    }
    
    /**
     * Converts to List of Adventure Components - one Component per line.
     * This is the primary method for multi-line messages.
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
        for (String line : processedValue) {
            result.add(miniMessage.deserialize(line));
        }
        return result;
    }
    
    /**
     * Converts to a single Adventure Component with lines joined by newlines.
     */
    public Component component() {
        process();
        
        TagResolver resolver = colorScheme != null 
            ? ColorSchemeTagResolver.of(colorScheme)
            : TagResolver.empty();
        
        MiniMessage miniMessage = MiniMessage.builder()
            .tags(TagResolver.resolver(TagResolver.standard(), resolver))
            .postProcessor(component -> component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
            .build();
        
        if (processedValue.isEmpty()) {
            return Component.empty();
        }
        
        Component result = miniMessage.deserialize(processedValue.get(0));
        for (int i = 1; i < processedValue.size(); i++) {
            result = result.append(Component.newline())
                           .append(miniMessage.deserialize(processedValue.get(i)));
        }
        return result;
    }
    
    /**
     * Converts to a single Adventure Component with custom deserializer.
     */
    public Component component(Function<String, Component> deserializer) {
        process();
        
        if (processedValue.isEmpty()) {
            return Component.empty();
        }
        
        Component result = deserializer.apply(processedValue.get(0));
        for (int i = 1; i < processedValue.size(); i++) {
            result = result.append(Component.newline())
                           .append(deserializer.apply(processedValue.get(i)));
        }
        return result;
    }
    
    /**
     * Converts to Adventure Component using legacy color codes (&amp;).
     */
    public Component componentLegacy() {
        return component(LegacyComponentSerializer.legacyAmpersand()::deserialize);
    }
    
    /**
     * Returns the processed messages as a single String joined by newlines.
     */
    public String asString() {
        process();
        return String.join("\n", processedValue);
    }
    
    /**
     * Returns the processed messages as List&lt;String&gt;.
     */
    public List<String> asList() {
        process();
        return new ArrayList<>(processedValue);
    }
    
    /**
     * Returns the raw unprocessed value.
     */
    public List<String> raw() {
        return new ArrayList<>(rawValue);
    }
}
