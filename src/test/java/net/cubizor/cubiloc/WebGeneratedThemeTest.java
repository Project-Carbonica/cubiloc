package net.cubizor.cubiloc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class WebGeneratedThemeTest {

    private I18n i18n;

    @BeforeEach
    void setUp() {
        i18n = new I18n(Locale.forLanguageTag("tr-TR"));
    }

    @Test
    void testLoadFromWebGenerated() throws IOException {
        // Load from classpath (src/test/resources/web_generated/)
        i18n.loadColorSchemeFromClasspath("dark", "web_generated/dark.json");
        i18n.loadColorSchemeFromClasspath("light", "web_generated/light.json");

        // Verify it was loaded as MessageTheme
        assertThat(i18n.getMessageTheme("dark")).isNotNull();
        assertThat(i18n.getMessageTheme("light")).isNotNull();

        // Verify specific color and decoration
        var darkTheme = i18n.getMessageTheme("dark");
        var errorStyle = darkTheme.getError().orElseThrow();
        
        assertThat(errorStyle.getColor().toHex()).isEqualToIgnoringCase("#ee4444");
        assertThat(errorStyle.isBold()).isTrue();
    }

    @Test
    void testBulkLoading() throws IOException {
        // Load all from web_generated
        i18n.loadThemesFromClasspath("web_generated");

        assertThat(i18n.getMessageTheme("dark")).isNotNull();
        assertThat(i18n.getMessageTheme("light")).isNotNull();
    }

    @Test
    void testComponentWithDecorations() throws IOException {
        i18n.loadColorSchemeFromClasspath("dark", "web_generated/dark.json");
        i18n.defaultScheme("dark");

        // Test component generation with implicit theme from context
        try (var ctx = i18n.context(null)) {
            var result = i18n.get("<error>Test Error</error>");
            var component = result.component();
            
            assertThat(component).isNotNull();
            // In dark.json, ERROR is #ee4444 and BOLD
            String serialized = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(component);
            assertThat(serialized).contains("<bold>");
            assertThat(serialized.toLowerCase(java.util.Locale.ROOT)).contains("#ee4444");
        }
    }

    @Test
    void testAllDecorations() {
        // Create a custom theme JSON with all possible decorations
        String json = "{\n" +
                "  \"name\": \"all-decorations\",\n" +
                "  \"messages\": {\n" +
                "    \"PRIMARY\": { \"color\": \"#FF0000\", \"decorations\": [\"BOLD\", \"ITALIC\"] },\n" +
                "    \"SECONDARY\": { \"color\": \"#00FF00\", \"decorations\": [\"UNDERLINED\"] },\n" +
                "    \"ERROR\": { \"color\": \"#0000FF\", \"decorations\": [\"STRIKETHROUGH\", \"OBFUSCATED\"] }\n" +
                "  }\n" +
                "}";
        
        i18n.loadColorSchemeFromString("all", json);
        i18n.defaultScheme("all");

        try (var ctx = i18n.context(null)) {
            // Test Primary (Bold + Italic)
            var p = i18n.get("<primary>BoldItalic</primary>").component();
            String sP = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(p);
            assertThat(sP).contains("<bold>").contains("<italic>");

            // Test Secondary (Underlined)
            var s = i18n.get("<secondary>Undered</secondary>").component();
            String sS = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(s);
            assertThat(sS).contains("<underlined>");

            // Test Error (Strikethrough + Obfuscated)
            var e = i18n.get("<error>Chaos</error>").component();
            String sE = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(e);
            assertThat(sE).contains("<strikethrough>").contains("<obfuscated>");
        }
    }

    @Test
    void testThemeSwitchingInContext() {
        String darkJson = "{ \"name\": \"d\", \"messages\": { \"PRIMARY\": { \"color\": \"#000000\", \"decorations\": [\"BOLD\"] } } }";
        String lightJson = "{ \"name\": \"l\", \"messages\": { \"PRIMARY\": { \"color\": \"#FFFFFF\", \"decorations\": [\"ITALIC\"] } } }";
        
        i18n.loadColorSchemeFromString("d", darkJson);
        i18n.loadColorSchemeFromString("l", lightJson);

        // Dark context
        try (var ctx = i18n.context(null, i18n.getMessageTheme("d"))) {
            var comp = i18n.get("<primary>Text</primary>").component();
            String ser = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(comp);
            assertThat(ser).contains("<bold>").doesNotContain("<italic>");
        }

        // Light context
        try (var ctx = i18n.context(null, i18n.getMessageTheme("l"))) {
            var comp = i18n.get("<primary>Text</primary>").component();
            String ser = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(comp);
            assertThat(ser).contains("<italic>").doesNotContain("<bold>");
        }
    }
}
