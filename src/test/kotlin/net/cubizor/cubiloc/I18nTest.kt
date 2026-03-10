package net.cubizor.cubiloc

import net.cubizor.cubiloc.locale.LocaleProvider
import net.kyori.adventure.text.Component
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Locale

class I18nTest {

    private lateinit var i18n: I18n

    @BeforeEach
    fun setUp() {
        i18n = I18n(Locale.forLanguageTag("tr-TR"))
        i18n.registerLocaleProvider(TestPlayerLocaleProvider())
        i18n.loadMessages("messages", File("src/test/resources"))
        i18n.loadColorSchemeFromClasspath("dark", "themes/dark.json")
        i18n.loadColorSchemeFromClasspath("light", "themes/light.json")
        i18n.defaultScheme("dark")
    }

    @Test
    fun `default locale is set correctly`() {
        assertThat(i18n.defaultLocale).isEqualTo(Locale.forLanguageTag("tr-TR"))
    }

    @Test
    fun `message retrieval uses context locale`() {
        val player = TestPlayer("Deichor", "tr_TR")
        i18n.context(player).use {
            val msg = i18n.message("welcome")
            assertThat(msg.asString()).contains("geldin")
        }
    }

    @Test
    fun `english locale returns english message`() {
        val player = TestPlayer("John", "en_US")
        i18n.context(player).use {
            val msg = i18n.message("welcome")
            assertThat(msg.asString()).contains("Welcome")
        }
    }

    @Test
    fun `placeholder resolution works`() {
        val player = TestPlayer("John", "en_US")
        i18n.context(player).use {
            val result = i18n.message("welcome").with("player" to "TestPlayer").asString()
            assertThat(result).contains("TestPlayer")
        }
    }

    @Test
    fun `component generation works`() {
        val player = TestPlayer("John", "en_US")
        i18n.context(player).use {
            val component: Component = i18n.message("welcome")
                .with("player" to "TestPlayer")
                .component()
            assertThat(component).isNotNull()
        }
    }

    @Test
    fun `list message returns multiple lines`() {
        val player = TestPlayer("John", "en_US")
        i18n.context(player).use {
            val lines = i18n.list("helpMenu").asList()
            assertThat(lines).isNotEmpty()
            assertThat(lines.size).isGreaterThan(1)
        }
    }

    @Test
    fun `nested config key resolves`() {
        val player = TestPlayer("John", "en_US")
        i18n.context(player).use {
            val msg = i18n.message("errors.notFound")
            assertThat(msg.asString()).isNotNull()
        }
    }

    @Test
    fun `color schemes load correctly`() {
        assertThat(i18n.getColorScheme("dark")).isNotNull()
        assertThat(i18n.getColorScheme("light")).isNotNull()
    }

    @Test
    fun `user scheme preference works`() {
        val player = TestPlayer("Deichor", "tr_TR")
        i18n.setUserScheme(player, "light")
        assertThat(i18n.getColorSchemeForUser(player)).isEqualTo(i18n.getColorScheme("light"))

        i18n.clearUserScheme(player)
        assertThat(i18n.getColorSchemeForUser(player)).isEqualTo(i18n.getDefaultColorScheme())
    }

    // ==================== Context Tests ====================

    @Test
    fun `context basic usage`() {
        val player = TestPlayer("Deichor", "en_US")
        i18n.context(player).use {
            val component = i18n.message("welcome")
                .with("player" to "Deichor")
                .component()
            assertThat(component).isNotNull()
        }
    }

    @Test
    fun `nested context restores previous`() {
        val player1 = TestPlayer("Player1", "en_US")
        val player2 = TestPlayer("Player2", "tr_TR")

        i18n.context(player1).use {
            i18n.context(player2).use {
                val msg = i18n.message("welcome")
                assertThat(msg.asString()).contains("geldin")
            }
            val msg = i18n.message("welcome")
            assertThat(msg.asString()).contains("Welcome")
        }
    }

    @Test
    fun `context isolation across threads`() {
        val player1 = TestPlayer("Player1", "en_US")
        val player2 = TestPlayer("Player2", "tr_TR")

        val t1 = Thread {
            i18n.context(player1).use {
                val result = i18n.message("welcome").with("player" to "P1").asString()
                assertThat(result).contains("Welcome")
            }
        }
        val t2 = Thread {
            i18n.context(player2).use {
                val result = i18n.message("welcome").with("player" to "P2").asString()
                assertThat(result).contains("geldin")
            }
        }

        t1.start(); t2.start()
        t1.join(); t2.join()
    }

    @Test
    fun `locale fallback when key missing in locale`() {
        // en_US has the key, if we request a locale that doesn't exist, fallback to default
        val player = TestPlayer("Player", "fr_FR")
        i18n.context(player).use {
            val msg = i18n.message("welcome")
            // Should fallback to tr_TR (default locale)
            assertThat(msg.asString()).contains("geldin")
        }
    }

    // ==================== Test Helpers ====================

    class TestPlayerLocaleProvider : LocaleProvider<TestPlayer> {
        override fun supports(type: Class<*>): Boolean = TestPlayer::class.java.isAssignableFrom(type)
        override fun getLocale(entity: TestPlayer): Locale =
            Locale.forLanguageTag(entity.locale.replace("_", "-"))
    }

    data class TestPlayer(val name: String, val locale: String)
}
