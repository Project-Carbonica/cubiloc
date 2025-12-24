package net.cubizor.cubiloc.color;

import net.cubizor.cubicolor.api.Color;
import net.cubizor.cubicolor.text.MessageRole;
import net.cubizor.cubicolor.text.MessageTheme;
import net.cubizor.cubicolor.text.TextDecoration;
import net.cubizor.cubicolor.text.TextStyle;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * MiniMessage tag resolver that provides MessageTheme-based style tags.
 * Supports both colors and decorations (bold, italic, etc.).
 */
public class MessageThemeTagResolver {

    private MessageThemeTagResolver() {
        // Utility class
    }

    /**
     * Creates a TagResolver for the given MessageTheme.
     *
     * @param theme the MessageTheme to resolve styles from
     * @return a TagResolver that resolves message role tags
     */
    public static TagResolver of(MessageTheme theme) {
        List<TagResolver> resolvers = new ArrayList<>();
        
        for (MessageRole role : MessageRole.values()) {
            resolvers.add(createTag(role.name().toLowerCase(Locale.ROOT), theme, role));
        }
        
        return TagResolver.resolver(resolvers);
    }

    private static TagResolver createTag(String tagName, MessageTheme theme, MessageRole role) {
        return TagResolver.resolver(tagName, (args, ctx) -> {
            TextStyle style = theme.getStyle(role).orElse(null);
            if (style == null) {
                return Tag.styling();
            }

            Color color = style.getColor();
            TextColor textColor = TextColor.color(color.getRed(), color.getGreen(), color.getBlue());
            
            // Apply color and all decorations
            List<net.kyori.adventure.text.format.TextDecoration> adventureDecorations = new ArrayList<>();
            for (TextDecoration dec : style.getDecorations()) {
                switch (dec) {
                    case BOLD -> adventureDecorations.add(net.kyori.adventure.text.format.TextDecoration.BOLD);
                    case ITALIC -> adventureDecorations.add(net.kyori.adventure.text.format.TextDecoration.ITALIC);
                    case UNDERLINED -> adventureDecorations.add(net.kyori.adventure.text.format.TextDecoration.UNDERLINED);
                    case STRIKETHROUGH -> adventureDecorations.add(net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH);
                    case OBFUSCATED -> adventureDecorations.add(net.kyori.adventure.text.format.TextDecoration.OBFUSCATED);
                }
            }

            return Tag.styling(builder -> {
                builder.color(textColor);
                for (net.kyori.adventure.text.format.TextDecoration ad : adventureDecorations) {
                    builder.decoration(ad, true);
                }
            });
        });
    }
}
