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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Message result for single-line String messages.
 * Provides {@link #component()} for converting to Adventure Component.
 * 
 * Supports @lc (locale-config) placeholders:
 * <ul>
 *   <li>{@code {@lc:path.to.message}} - Reference another message from the config</li>
 *   <li>{@code {trueValue,falseValue@lc#field}} - Conditional with config values</li>
 * </ul>
 */
public class SingleMessageResult {
    
    private final String rawValue;
    private final Map<String, Object> placeholders = new HashMap<>();
    private ColorScheme colorScheme;
    private MessageConfig messageConfig;
    private boolean processed = false;
    private String processedValue;
    
    SingleMessageResult(String rawValue) {
        this.rawValue = rawValue;
    }
    
    /**
     * Creates a SingleMessageResult from a String value.
     */
    public static SingleMessageResult of(String value) {
        return new SingleMessageResult(value);
    }
    
    /**
     * Adds a placeholder value.
     */
    public SingleMessageResult with(String key, Object value) {
        placeholders.put(key, value);
        return this;
    }
    
    /**
     * Sets the ColorScheme for semantic color tags.
     */
    public SingleMessageResult withColorScheme(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
        return this;
    }
    
    /**
     * Sets the MessageConfig for @lc placeholder resolution.
     */
    public SingleMessageResult withConfig(MessageConfig config) {
        this.messageConfig = config;
        return this;
    }
    
    private void process() {
        if (processed) return;
        
        String value = rawValue;
        
        // First, resolve @lc placeholders if config is available
        if (messageConfig != null) {
            value = LocaleConfigPlaceholder.process(value, messageConfig, placeholders);
        }
        
        // Then, apply okaeri-placeholders
        CompiledMessage compiled = CompiledMessage.of(value);
        PlaceholderContext context = PlaceholderContext.of(compiled);
        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            context.with(entry.getKey(), entry.getValue());
        }
        processedValue = context.apply();
        
        processed = true;
    }
    
    /**
     * Converts to Adventure Component with MiniMessage and ColorScheme support.
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
        
        return miniMessage.deserialize(processedValue);
    }
    
    /**
     * Converts to Adventure Component with custom deserializer.
     */
    public Component component(Function<String, Component> deserializer) {
        process();
        return deserializer.apply(processedValue);
    }
    
    /**
     * Converts to Adventure Component with additional TagResolver.
     */
    public Component component(TagResolver additionalResolver) {
        process();
        
        TagResolver colorResolver = colorScheme != null 
            ? ColorSchemeTagResolver.of(colorScheme)
            : TagResolver.empty();
        
        MiniMessage miniMessage = MiniMessage.builder()
            .tags(TagResolver.resolver(TagResolver.standard(), colorResolver, additionalResolver))
            .build();
        
        return miniMessage.deserialize(processedValue);
    }
    
    /**
     * Converts to Adventure Component using legacy color codes (&amp;).
     */
    public Component componentLegacy() {
        return component(LegacyComponentSerializer.legacyAmpersand()::deserialize);
    }
    
    /**
     * Returns the processed message as String.
     */
    public String asString() {
        process();
        return processedValue;
    }
    
    /**
     * Returns the raw unprocessed value.
     */
    public String raw() {
        return rawValue;
    }
}
