package net.cubizor.cubiloc

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Locale

class MessageReferenceTest {

    private lateinit var i18n: I18n

    @BeforeEach
    fun setUp() {
        i18n = I18n(Locale.US)
        i18n.loadMessages("messages", File("src/test/resources"))
        i18n.loadColorSchemeFromString("dark", """{"name":"Dark","colors":{"PRIMARY":"#6200EE"}}""")
        i18n.defaultScheme("dark")
    }

    @Test
    fun `direct reference resolves`() {
        i18n.context("en_US").use {
            // serverStatus contains {@statusActive}
            val msg = i18n.message("serverStatus")
            assertThat(msg.asString()).contains("<success>Active</success>")
        }
    }

    @Test
    fun `conditional reference resolves true`() {
        i18n.context("en_US").use {
            val msg = i18n.message("dynamicStatus")
                .with("name" to "Server", "enabled" to true)
            assertThat(msg.asString()).contains("<success>Active</success>")
        }
    }

    @Test
    fun `conditional reference resolves false`() {
        i18n.context("en_US").use {
            val msg = i18n.message("dynamicStatus")
                .with("name" to "Server", "enabled" to false)
            assertThat(msg.asString()).contains("<error>Inactive</error>")
        }
    }

    @Test
    fun `nested key reference resolves`() {
        i18n.context("en_US").use {
            val msg = i18n.message("errors.notFound").with("item" to "sword")
            assertThat(msg.asString()).contains("sword")
            assertThat(msg.asString()).contains("not found")
        }
    }

    @Test
    fun `fallback reference when key missing`() {
        i18n.context("en_US").use {
            // Manually create a message with a missing reference and fallback
            val result = net.cubizor.cubiloc.message.MessageReference.resolve(
                "{@nonExistent|default_value}",
                mapOf("statusActive" to "<success>Active</success>"),
                emptyMap()
            )
            assertThat(result).isEqualTo("default_value")
        }
    }

    @Test
    fun `dotted placeholder with currency symbol`() {
        i18n.context("en_US").use {
            val msg = i18n.message("balance")
                .with("amount" to "100", "currency" to "TL")
            assertThat(msg.asString()).contains("100")
            assertThat(msg.asString()).contains("TL")
        }
    }
}
