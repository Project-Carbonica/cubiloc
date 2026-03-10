package net.cubizor.cubiloc

import net.kyori.adventure.text.format.TextColor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

class ThemeTest {

    private lateinit var i18n: I18n

    @BeforeEach
    fun setUp() {
        i18n = I18n(Locale.US)
        i18n.loadColorSchemeFromClasspath("dark", "themes/dark.json")
        i18n.loadColorSchemeFromClasspath("light", "themes/light.json")
        i18n.defaultScheme("dark")
    }

    @Test
    fun `color scheme loads`() {
        assertThat(i18n.getColorScheme("dark")).isNotNull()
        assertThat(i18n.getColorScheme("light")).isNotNull()
    }

    @Test
    fun `component applies color from scheme`() {
        i18n.context("en_US").use {
            val result = net.cubizor.cubiloc.message.SingleMessageResult(
                rawValue = "<primary>Hello</primary>",
                globalPlaceholders = i18n.placeholders,
            )
            val component = result.component()
            assertThat(component).isNotNull()
            assertThat(hasAnyColor(component)).isTrue()
        }
    }

    @Test
    fun `theme switching in context`() {
        val dark = i18n.getColorScheme("dark")!!
        val light = i18n.getColorScheme("light")!!

        i18n.context("en_US", dark).use {
            val c1 = net.cubizor.cubiloc.message.SingleMessageResult(
                rawValue = "<primary>test</primary>",
                globalPlaceholders = i18n.placeholders,
            ).component()
            assertThat(c1).isNotNull()
        }

        i18n.context("en_US", light).use {
            val c2 = net.cubizor.cubiloc.message.SingleMessageResult(
                rawValue = "<primary>test</primary>",
                globalPlaceholders = i18n.placeholders,
            ).component()
            assertThat(c2).isNotNull()
        }
    }

    @Test
    fun `web generated theme with decorations loads`() {
        i18n.loadColorSchemeFromClasspath("web-dark", "web_generated/dark.json")
        i18n.loadColorSchemeFromClasspath("web-light", "web_generated/light.json")
        // If these are MessageTheme (contain "messages"), they go to messageThemes
        val webDarkTheme = i18n.getMessageTheme("web-dark")
        val webDarkScheme = i18n.getColorScheme("web-dark")
        // At least one should be loaded
        assertThat(webDarkTheme != null || webDarkScheme != null).isTrue()
    }

    private fun hasAnyColor(component: net.kyori.adventure.text.Component): Boolean {
        if (component.color() != null) return true
        return component.children().any { hasAnyColor(it) }
    }
}
