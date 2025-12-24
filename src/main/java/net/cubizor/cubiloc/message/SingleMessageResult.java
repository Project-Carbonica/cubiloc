package net.cubizor.cubiloc.message;

import eu.okaeri.placeholders.context.PlaceholderContext;
import eu.okaeri.placeholders.message.CompiledMessage;
import net.cubizor.cubicolor.api.ColorScheme;
import net.cubizor.cubicolor.text.MessageTheme;
import net.cubizor.cubiloc.color.ColorSchemeTagResolver;
import net.cubizor.cubiloc.color.MessageThemeTagResolver;
import net.cubizor.cubiloc.config.MessageConfig;
import net.cubizor.cubiloc.context.I18nContext;
import net.cubizor.cubiloc.context.I18nContextHolder;
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
    private MessageTheme messageTheme;
    private MessageConfig messageConfig;
    private boolean processed = false;
    private String processedValue;
    
    public SingleMessageResult(String rawValue) {
        this.rawValue = rawValue;
    }
    
    private SingleMessageResult(SingleMessageResult other) {
        this.rawValue = other.rawValue;
        this.placeholders.putAll(other.placeholders);
        this.colorScheme = other.colorScheme;
        this.messageTheme = other.messageTheme;
        this.messageConfig = other.messageConfig;
        this.processed = false; // Reset processed state for the copy
    }
    
    /**
     * Creates a SingleMessageResult from a String value.
     */
    public static SingleMessageResult of(String value) {
        return new SingleMessageResult(value);
    }
    
    /**
     * Adds a placeholder value.
     * Returns a new instance with the added placeholder.
     */
    public SingleMessageResult with(String key, Object value) {
        SingleMessageResult copy = new SingleMessageResult(this);
        copy.placeholders.put(key, value);
        return copy;
    }
    
    /**
     * Sets the ColorScheme for semantic color tags.
     * Returns a new instance with the new color scheme.
     */
    public SingleMessageResult withColorScheme(ColorScheme colorScheme) {
        SingleMessageResult copy = new SingleMessageResult(this);
        copy.colorScheme = colorScheme;
        return copy;
    }

    /**
     * Sets the MessageTheme for semantic style tags.
     * Returns a new instance with the new message theme.
     */
    public SingleMessageResult withMessageTheme(MessageTheme messageTheme) {
        SingleMessageResult copy = new SingleMessageResult(this);
        copy.messageTheme = messageTheme;
        return copy;
    }
    
    /**
     * Sets the MessageConfig for @lc placeholder resolution.
     * Internal use: Modifies the current instance.
     */
    public SingleMessageResult withConfig(MessageConfig config) {
        this.messageConfig = config;
        return this;
    }

    /**
     * Sets the context (locale, color scheme, message theme, config) from I18nContext.
     * Internal use: Modifies the current instance.
     */
    public SingleMessageResult withContext(I18nContext context) {
        if (context != null) {
            if (this.colorScheme == null && context.getColorScheme() != null) {
                this.colorScheme = context.getColorScheme();
            }
            if (this.messageTheme == null && context.getMessageTheme() != null) {
                this.messageTheme = context.getMessageTheme();
            }
        }
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
     * Automatically uses context if available.
     */
    public Component component() {
        // Auto-apply context if not already set
        applyContextIfNeeded();

        process();

        TagResolver themeResolver = TagResolver.empty();
        if (messageTheme != null) {
            themeResolver = MessageThemeTagResolver.of(messageTheme);
        } else if (colorScheme != null) {
            themeResolver = ColorSchemeTagResolver.of(colorScheme);
        }

        MiniMessage miniMessage = MiniMessage.builder()
            .tags(TagResolver.resolver(TagResolver.standard(), themeResolver))
            .postProcessor(component -> component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
            .build();

        return miniMessage.deserialize(processedValue);
    }

    /**
     * Applies context from ThreadLocal if not already set.
     */
    private void applyContextIfNeeded() {
        I18nContext context = I18nContextHolder.getOrNull();
        if (context != null) {
            withContext(context);
        }
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
