package net.cubizor.cubiloc.message;

import eu.okaeri.placeholders.Placeholders;
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
 */
public class SingleMessageResult {
    
    private final String rawValue;
    private final Map<String, Object> placeholders = new HashMap<>();
    private Placeholders globalPlaceholders;
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
        this.globalPlaceholders = other.globalPlaceholders;
        this.colorScheme = other.colorScheme;
        this.messageTheme = other.messageTheme;
        this.messageConfig = other.messageConfig;
        this.processed = false;
    }
    
    public static SingleMessageResult of(String value) {
        return new SingleMessageResult(value);
    }
    
    public SingleMessageResult with(String key, Object value) {
        SingleMessageResult copy = new SingleMessageResult(this);
        copy.placeholders.put(key, value);
        return copy;
    }
    
    public SingleMessageResult withColorScheme(ColorScheme colorScheme) {
        SingleMessageResult copy = new SingleMessageResult(this);
        copy.colorScheme = colorScheme;
        return copy;
    }

    public SingleMessageResult withMessageTheme(MessageTheme messageTheme) {
        SingleMessageResult copy = new SingleMessageResult(this);
        copy.messageTheme = messageTheme;
        return copy;
    }
    
    public SingleMessageResult withConfig(MessageConfig config) {
        this.messageConfig = config;
        return this;
    }

    public SingleMessageResult withPlaceholders(Placeholders placeholders) {
        this.globalPlaceholders = placeholders;
        return this;
    }

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
        if (messageConfig != null) {
            value = LocaleConfigPlaceholder.process(value, messageConfig, placeholders);
        }
        
        CompiledMessage compiled = CompiledMessage.of(value);
        Placeholders placeholdersInstance = (globalPlaceholders != null) ? globalPlaceholders : Placeholders.create();
        PlaceholderContext context = placeholdersInstance.contextOf(compiled);
        
        Map<String, Object> expandedPlaceholders = expandMap(placeholders);
        for (Map.Entry<String, Object> entry : expandedPlaceholders.entrySet()) {
            context.with(entry.getKey(), entry.getValue());
        }

        // Support for remaining fallback needs and nested maps
        context.getPlaceholders().fallbackResolver((parent, field, ctx) -> {
            String name = field.unsafe().getName();
            if (parent instanceof Map) {
                return ((Map<?, ?>) parent).get(name);
            }
            if (placeholders.containsKey(name)) {
                return placeholders.get(name);
            }
            return null;
        });
        
        processedValue = context.apply();
        processed = true;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> expandMap(Map<String, Object> source) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.contains(".")) {
                String[] parts = key.split("\\.");
                Map<String, Object> current = result;
                for (int i = 0; i < parts.length - 1; i++) {
                    Object next = current.computeIfAbsent(parts[i], k -> new HashMap<String, Object>());
                    if (next instanceof Map) {
                        current = (Map<String, Object>) next;
                    } else {
                        // Conflict: a.b exists but a is already a value. Skip or overwrite?
                        // For Cubiloc, we'll overwrite with a map to support the dotted path.
                        Map<String, Object> newMap = new HashMap<>();
                        current.put(parts[i], newMap);
                        current = newMap;
                    }
                }
                current.put(parts[parts.length - 1], value);
            } else {
                result.put(key, value);
            }
        }
        return result;
    }
    
    public Component component() {
        process();

        I18nContext context = I18nContextHolder.get();
        MessageTheme themeToUse = (messageTheme != null) ? messageTheme : context.getMessageTheme();
        ColorScheme schemeToUse = (colorScheme != null) ? colorScheme : context.getColorScheme();

        TagResolver themeResolver = TagResolver.empty();
        if (themeToUse != null) {
            themeResolver = MessageThemeTagResolver.of(themeToUse);
        } else if (schemeToUse != null) {
            themeResolver = ColorSchemeTagResolver.of(schemeToUse);
        }

        MiniMessage miniMessage = MiniMessage.builder()
            .tags(TagResolver.resolver(TagResolver.standard(), themeResolver))
            .postProcessor(c -> c.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
            .build();

        return miniMessage.deserialize(processedValue);
    }

    public Component component(Function<String, Component> deserializer) {
        process();
        return deserializer.apply(processedValue);
    }
    
    public Component component(TagResolver additionalResolver) {
        process();
        
        I18nContext context = I18nContextHolder.get();
        MessageTheme themeToUse = (messageTheme != null) ? messageTheme : context.getMessageTheme();
        ColorScheme schemeToUse = (colorScheme != null) ? colorScheme : context.getColorScheme();

        TagResolver themeResolver = TagResolver.empty();
        if (themeToUse != null) {
            themeResolver = MessageThemeTagResolver.of(themeToUse);
        } else if (schemeToUse != null) {
            themeResolver = ColorSchemeTagResolver.of(schemeToUse);
        }
        
        MiniMessage miniMessage = MiniMessage.builder()
            .tags(TagResolver.resolver(TagResolver.standard(), themeResolver, additionalResolver))
            .build();
        
        return miniMessage.deserialize(processedValue);
    }
    
    public Component componentLegacy() {
        return component(LegacyComponentSerializer.legacyAmpersand()::deserialize);
    }
    
    public String asString() {
        process();
        return processedValue;
    }
    
    public String raw() {
        return rawValue;
    }
}
