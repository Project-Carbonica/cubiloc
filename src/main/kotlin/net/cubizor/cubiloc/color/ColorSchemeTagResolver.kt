package net.cubizor.cubiloc.color

import net.cubizor.cubicolor.api.ColorRole
import net.cubizor.cubicolor.api.ColorScheme
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object ColorSchemeTagResolver {

    private val ROLES = mapOf(
        "primary" to ColorRole.PRIMARY,
        "secondary" to ColorRole.SECONDARY,
        "tertiary" to ColorRole.TERTIARY,
        "accent" to ColorRole.ACCENT,
        "background" to ColorRole.BACKGROUND,
        "surface" to ColorRole.SURFACE,
        "error" to ColorRole.ERROR,
        "success" to ColorRole.SUCCESS,
        "warning" to ColorRole.WARNING,
        "info" to ColorRole.INFO,
        "text" to ColorRole.TEXT,
        "text_secondary" to ColorRole.TEXT_SECONDARY,
        "border" to ColorRole.BORDER,
        "overlay" to ColorRole.OVERLAY,
    )

    fun of(scheme: ColorScheme): TagResolver = TagResolver.resolver(
        ROLES.map { (name, role) ->
            TagResolver.resolver(name) { _, _ ->
                val color = scheme.getColor(role).orElse(null)
                    ?: return@resolver Tag.styling()
                Tag.styling(TextColor.color(color.red, color.green, color.blue))
            }
        }
    )
}
