package net.cubizor.cubiloc;

import eu.okaeri.configs.OkaeriConfig;
import net.cubizor.cubiloc.config.MessageConfig;
import net.cubizor.cubiloc.message.SingleMessageResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class CubiconomyReproductionTest {

    private I18n i18n;

    public static class CubiconomyMessage extends MessageConfig {
        public SingleMessageResult prefix = new SingleMessageResult("<primary>Cubiconomy »</primary> ");
        public CommandMessage commandMessage = new CommandMessage();

        public static class CommandMessage extends OkaeriConfig {
            public SingleMessageResult balanceOther = new SingleMessageResult("{@lc:prefix}<primary>{player}'s balance is {balance} {currency.symbol}.</primary>");
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        i18n = new I18n();
        i18n.setDefaultLocale(Locale.US);

        // Load color scheme - MUST contain name
        i18n.loadColorSchemeFromString("dark", "{\"name\":\"Dark Theme\", \"colors\":{\"PRIMARY\":\"#6200EE\"}}");
        i18n.defaultScheme("dark");
    }

    @Test
    void reproduceColorAndPlaceholderIssue() {
        CubiconomyMessage msg = new CubiconomyMessage();
        
        // Manually trigger injection
        i18n.injectConfigIntoFields(msg, msg);

        TextColor primaryColor = TextColor.color(0x62, 0x00, 0xEE);

        // Simulate the call mentioned by user
        Component component = msg.commandMessage.balanceOther
            .with("player", "Deichor")
            .with("balance", "100")
            .with("currency.symbol", "tl")
            .component();

        String serialized = MiniMessage.miniMessage().serialize(component);
        System.out.println("Serialized: " + serialized);

        // Check for placeholder resolution (dots in keys)
        assertThat(serialized).contains("tl");
        assertThat(serialized).doesNotContain("{currency.symbol}");
        assertThat(serialized).doesNotContain("missing:currency.symbol");
        
        // Check for color resolution (default scheme should be used)
        assertThat(hasColor(component, primaryColor)).isTrue();
    }

    public static void main(String[] args) throws Exception {
        CubiconomyReproductionTest test = new CubiconomyReproductionTest();
        test.setUp();
        test.reproduceColorAndPlaceholderIssue();
        System.out.println("SUCCESS: Reproduction test passed!");
    }

    private boolean hasColor(Component component, TextColor color) {
        if (color.equals(component.color())) return true;
        for (Component child : component.children()) {
            if (hasColor(child, color)) return true;
        }
        return false;
    }
}