package net.cubizor.cubiloc.color;

import net.cubizor.cubicolor.api.Color;
import net.cubizor.cubicolor.api.ColorRole;
import net.cubizor.cubicolor.api.ColorScheme;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * MiniMessage tag resolver that provides ColorScheme-based color tags.
 * 
 * Supports all ColorRole tags:
 * <ul>
 *   <li>&lt;primary&gt; - PRIMARY color (Ana renk)</li>
 *   <li>&lt;secondary&gt; - SECONDARY color (İkincil renk)</li>
 *   <li>&lt;tertiary&gt; - TERTIARY color (Üçüncül renk)</li>
 *   <li>&lt;accent&gt; - ACCENT color (Vurgu rengi)</li>
 *   <li>&lt;error&gt; - ERROR color (Hata rengi)</li>
 *   <li>&lt;success&gt; - SUCCESS color (Başarı rengi)</li>
 *   <li>&lt;warning&gt; - WARNING color (Uyarı rengi)</li>
 *   <li>&lt;info&gt; - INFO color (Bilgi rengi)</li>
 *   <li>&lt;text&gt; - TEXT color (Metin rengi)</li>
 *   <li>&lt;text_secondary&gt; - TEXT_SECONDARY color (İkincil metin rengi)</li>
 *   <li>&lt;background&gt; - BACKGROUND color (Arka plan rengi)</li>
 *   <li>&lt;surface&gt; - SURFACE color (Yüzey rengi)</li>
 *   <li>&lt;border&gt; - BORDER color (Kenarlık rengi)</li>
 *   <li>&lt;overlay&gt; - OVERLAY color (Kaplama rengi)</li>
 * </ul>
 * 
 * Example usage:
 * <pre>{@code
 * ColorScheme scheme = ...; // your color scheme
 * TagResolver resolver = ColorSchemeTagResolver.of(scheme);
 * Component message = MiniMessage.miniMessage().deserialize(
 *     "<primary>Hello</primary> <secondary>World</secondary>",
 *     resolver
 * );
 * }</pre>
 */
public class ColorSchemeTagResolver {

    private ColorSchemeTagResolver() {
        // Utility class
    }

    /**
     * Creates a TagResolver for the given ColorScheme.
     * Includes tags for all color roles, with undefined roles rendering as no-op (pass-through).
     *
     * @param scheme the ColorScheme to resolve colors from
     * @return a TagResolver that resolves color role tags
     */
    public static TagResolver of(ColorScheme scheme) {
        return TagResolver.resolver(
            createTag("primary", scheme, ColorRole.PRIMARY),
            createTag("secondary", scheme, ColorRole.SECONDARY),
            createTag("tertiary", scheme, ColorRole.TERTIARY),
            createTag("accent", scheme, ColorRole.ACCENT),
            createTag("background", scheme, ColorRole.BACKGROUND),
            createTag("surface", scheme, ColorRole.SURFACE),
            createTag("error", scheme, ColorRole.ERROR),
            createTag("success", scheme, ColorRole.SUCCESS),
            createTag("warning", scheme, ColorRole.WARNING),
            createTag("info", scheme, ColorRole.INFO),
            createTag("text", scheme, ColorRole.TEXT),
            createTag("text_secondary", scheme, ColorRole.TEXT_SECONDARY),
            createTag("border", scheme, ColorRole.BORDER),
            createTag("overlay", scheme, ColorRole.OVERLAY)
        );
    }

    /**
     * Creates a single tag resolver for a specific color role.
     * If the color is not defined, creates a no-op tag that passes through content without styling.
     */
    private static TagResolver createTag(String tagName, ColorScheme scheme, ColorRole role) {
        return TagResolver.resolver(tagName, (args, ctx) -> {
            Color color = scheme.getColor(role).orElse(null);
            if (color == null) {
                // No color defined - return empty styling (pass-through)
                return Tag.styling();
            }
            TextColor textColor = TextColor.color(color.getRed(), color.getGreen(), color.getBlue());
            return Tag.styling(textColor);
        });
    }
}
