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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Message result for multi-line List<String> messages.
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
        this.processed = false;
    }
    
    public static ListMessageResult of(List<String> value) {
        return new ListMessageResult(value);
    }
    
    public ListMessageResult with(String key, Object value) {
        ListMessageResult copy = new ListMessageResult(this);
        copy.placeholders.put(key, value);
        return copy;
    }
    
    public ListMessageResult withColorScheme(ColorScheme colorScheme) {
        ListMessageResult copy = new ListMessageResult(this);
        copy.colorScheme = colorScheme;
        return copy;
    }

    public ListMessageResult withMessageTheme(MessageTheme messageTheme) {
        ListMessageResult copy = new ListMessageResult(this);
        copy.messageTheme = messageTheme;
        return copy;
    }
    
    public ListMessageResult withConfig(MessageConfig config) {
        this.messageConfig = config;
        return this;
    }

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
            if (messageConfig != null) {
                value = LocaleConfigPlaceholder.process(value, messageConfig, placeholders);
            }
            CompiledMessage compiled = CompiledMessage.of(value);
            PlaceholderContext context = Placeholders.create().contextOf(compiled);
            
            Map<String, Object> expandedPlaceholders = expandMap(placeholders);
            for (Map.Entry<String, Object> entry : expandedPlaceholders.entrySet()) {
                context.with(entry.getKey(), entry.getValue());
            }

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
            processedValue.add(context.apply());
        }
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
    
    public List<Component> components() {
        process();
        I18nContext context = I18nContextHolder.get();
        MessageTheme themeToUse = (messageTheme != null) ? messageTheme : context.getMessageTheme();
        ColorScheme schemeToUse = (colorScheme != null) ? colorScheme : context.getColorScheme();

        TagResolver themeResolver = TagResolver.empty();
        if (themeToUse != null) themeResolver = MessageThemeTagResolver.of(themeToUse);
        else if (schemeToUse != null) themeResolver = ColorSchemeTagResolver.of(schemeToUse);
        
        MiniMessage miniMessage = MiniMessage.builder()
            .tags(TagResolver.resolver(TagResolver.standard(), themeResolver))
            .postProcessor(c -> c.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
            .build();
        
        List<Component> result = new ArrayList<>();
        for (String line : processedValue) result.add(miniMessage.deserialize(line));
        return result;
    }

    public Component component() {
        process();
        I18nContext context = I18nContextHolder.get();
        MessageTheme themeToUse = (messageTheme != null) ? messageTheme : context.getMessageTheme();
        ColorScheme schemeToUse = (colorScheme != null) ? colorScheme : context.getColorScheme();

        TagResolver themeResolver = TagResolver.empty();
        if (themeToUse != null) themeResolver = MessageThemeTagResolver.of(themeToUse);
        else if (schemeToUse != null) themeResolver = ColorSchemeTagResolver.of(schemeToUse);
        
        MiniMessage miniMessage = MiniMessage.builder()
            .tags(TagResolver.resolver(TagResolver.standard(), themeResolver))
            .postProcessor(c -> c.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
            .build();
        
        if (processedValue.isEmpty()) return Component.empty();
        Component result = miniMessage.deserialize(processedValue.get(0));
        for (int i = 1; i < processedValue.size(); i++) {
            result = result.append(Component.newline()).append(miniMessage.deserialize(processedValue.get(i)));
        }
        return result;
    }
    
    public Component component(Function<String, Component> deserializer) {
        process();
        if (processedValue.isEmpty()) return Component.empty();
        Component result = deserializer.apply(processedValue.get(0));
        for (int i = 1; i < processedValue.size(); i++) {
            result = result.append(Component.newline()).append(deserializer.apply(processedValue.get(i)));
        }
        return result;
    }
    
    public Component componentLegacy() {
        return component(LegacyComponentSerializer.legacyAmpersand()::deserialize);
    }
    
    public String asString() {
        process();
        return String.join("\n", processedValue);
    }
    
    public List<String> asList() {
        process();
        return new ArrayList<>(processedValue);
    }

    public List<String> raw() {
        return new ArrayList<>(rawValue);
    }
}