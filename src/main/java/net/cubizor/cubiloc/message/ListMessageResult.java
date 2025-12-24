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
    private MessageTheme messageTheme;
    private MessageConfig messageConfig;
    private boolean processed = false;
    private List<String> processedValue;
    
    public ListMessageResult(List<String> rawValue) {
        this.rawValue = new ArrayList<>(rawValue);
    }
    
    private ListMessageResult(ListMessageResult other) {
        this.rawValue = new ArrayList<>(other.rawValue);
        this.placeholders.putAll(other.placeholders);
        this.colorScheme = other.colorScheme;
        this.messageTheme = other.messageTheme;
        this.messageConfig = other.messageConfig;
        this.processed = false; // Reset processed state
    }
    
    /**
     * Creates a ListMessageResult from a List&lt;String&gt; value.
     */
    public static ListMessageResult of(List<String> value) {
        return new ListMessageResult(value);
    }
    
    /**
     * Adds a placeholder value.
     * Returns a new instance with the added placeholder.
     */
    public ListMessageResult with(String key, Object value) {
        ListMessageResult copy = new ListMessageResult(this);
        copy.placeholders.put(key, value);
        return copy;
    }
    
    /**
     * Sets the ColorScheme for semantic color tags.
     * Returns a new instance with the new color scheme.
     */
    public ListMessageResult withColorScheme(ColorScheme colorScheme) {
        ListMessageResult copy = new ListMessageResult(this);
        copy.colorScheme = colorScheme;
        return copy;
    }

    /**
     * Sets the MessageTheme for semantic style tags.
     * Returns a new instance with the new message theme.
     */
    public ListMessageResult withMessageTheme(MessageTheme messageTheme) {
        ListMessageResult copy = new ListMessageResult(this);
        copy.messageTheme = messageTheme;
        return copy;
    }
    
    /**
     * Sets the MessageConfig for @lc placeholder resolution.
     * Internal use: Modifies the current instance.
     */
    public ListMessageResult withConfig(MessageConfig config) {
        this.messageConfig = config;
        return this;
    }

    /**
     * Sets the context (locale, color scheme, message theme, config) from I18nContext.
     * Internal use: Modifies the current instance.
     */
    public ListMessageResult withContext(I18nContext context) {
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
     * Automatically uses context if available.
     */
    public List<Component> components() {
        process();
        
        // Resolve theme/scheme from current context or stored values
        I18nContext context = I18nContextHolder.getOrNull();
        MessageTheme themeToUse = (context != null && context.getMessageTheme() != null) ? context.getMessageTheme() : messageTheme;
        ColorScheme schemeToUse = (context != null && context.getColorScheme() != null) ? context.getColorScheme() : colorScheme;

        TagResolver themeResolver = TagResolver.empty();
        if (themeToUse != null) {
            themeResolver = MessageThemeTagResolver.of(themeToUse);
        } else if (schemeToUse != null) {
            themeResolver = ColorSchemeTagResolver.of(schemeToUse);
        }
        
        MiniMessage miniMessage = MiniMessage.builder()
            .tags(TagResolver.resolver(TagResolver.standard(), themeResolver))
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
     * Automatically uses context if available.
     */
    public Component component() {
        process();
        
        // Resolve theme/scheme from current context or stored values
        I18nContext context = I18nContextHolder.getOrNull();
        MessageTheme themeToUse = (context != null && context.getMessageTheme() != null) ? context.getMessageTheme() : messageTheme;
        ColorScheme schemeToUse = (context != null && context.getColorScheme() != null) ? context.getColorScheme() : colorScheme;

        TagResolver themeResolver = TagResolver.empty();
        if (themeToUse != null) {
            themeResolver = MessageThemeTagResolver.of(themeToUse);
        } else if (schemeToUse != null) {
            themeResolver = ColorSchemeTagResolver.of(schemeToUse);
        }
        
        MiniMessage miniMessage = MiniMessage.builder()
            .tags(TagResolver.resolver(TagResolver.standard(), themeResolver))
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
