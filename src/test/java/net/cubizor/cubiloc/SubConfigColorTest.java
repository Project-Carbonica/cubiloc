package net.cubizor.cubiloc;

import net.cubizor.cubicolor.api.ColorScheme;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class SubConfigColorTest {

    private I18n i18n;
    private File testDataFolder;

    @BeforeEach
    void setUp() throws Exception {
        testDataFolder = new File("src/test/resources");

        i18n = new I18n(Locale.US);

        // Register message config
        i18n.register(TestMessages.class)
            .path("messages")
            .suffix(".yml")
            .dataFolder(testDataFolder)
            .load();

        // Load color schemes
        i18n.loadColorSchemeFromClasspath("dark", "themes/dark.json");
        i18n.defaultScheme("dark");
    }

    @Test
    void testColorInSubConfig() {
        TestMessages msg = i18n.config(TestMessages.class);
        
        // <error> in dark.json is #CF6679
        TextColor expectedErrorColor = TextColor.color(0xCF, 0x66, 0x79);

        // 1. Test top-level config color
        try (var ctx = i18n.context(Locale.US)) {
            Component welcome = msg.welcome.with("player", "Player").component();
            // <success> in dark.json is #4CAF50
            TextColor expectedSuccessColor = TextColor.color(0x4C, 0xAF, 0x50);
            assertThat(hasColor(welcome, expectedSuccessColor)).isTrue();
        }

        // 2. Test sub-config color
        try (var ctx = i18n.context(Locale.US)) {
            Component notFound = msg.errors.notFound.with("item", "Sword").component();
            assertThat(hasColor(notFound, expectedErrorColor)).isTrue();
        }
    }

    @Test
    void testColorInNestedSubConfig() {
        TestMessages msg = i18n.config(TestMessages.class);
        TextColor expectedWarningColor = TextColor.color(0xFF, 0xC1, 0x07); // <warning> #FFC107

        try (var ctx = i18n.context(Locale.US)) {
            Component enabled = msg.admin.maintenance.enabled.component();
            assertThat(hasColor(enabled, expectedWarningColor)).isTrue();
        }
    }

    private boolean hasColor(Component component, TextColor color) {
        if (color.equals(component.color())) return true;
        for (Component child : component.children()) {
            if (hasColor(child, color)) return true;
        }
        return false;
    }
}
