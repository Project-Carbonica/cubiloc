package net.cubizor.cubiloc;

import net.cubizor.cubiloc.locale.LocaleProvider;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the Cubiloc I18n system.
 */
class I18nTest {
    
    private I18n i18n;
    private File testDataFolder;
    
    @BeforeEach
    void setUp() throws Exception {
        testDataFolder = new File("src/test/resources");
        
        i18n = new I18n(Locale.forLanguageTag("tr-TR"));
        
        // Register test locale provider
        i18n.registerLocaleProvider(new TestPlayerLocaleProvider());
        
        // Register message config
        i18n.register(TestMessages.class)
            .path("messages")
            .suffix(".yml")
            .dataFolder(testDataFolder)
            .load();
        
        // Load color schemes
        i18n.loadColorSchemeFromClasspath("dark", "themes/dark.json");
        i18n.loadColorSchemeFromClasspath("light", "themes/light.json");
        i18n.defaultScheme("dark");
    }
    
    @Test
    void testDefaultLocale() {
        assertThat(i18n.getDefaultLocale()).isEqualTo(Locale.forLanguageTag("tr-TR"));
    }
    
    @Test
    void testConfigRetrieval() {
        TestMessages msg = i18n.config("tr_TR", TestMessages.class);
        assertThat(msg).isNotNull();
        assertThat(msg.welcome.asString()).contains("geldin");
    }

    @Test
    void testEnglishLocale() {
        TestMessages msg = i18n.config("en_US", TestMessages.class);
        assertThat(msg).isNotNull();
        assertThat(msg.welcome.asString()).contains("Welcome");
    }

    @Test
    void testPlayerLocaleResolution() {
        TestPlayer turkishPlayer = new TestPlayer("Deichor", "tr_TR");
        TestPlayer englishPlayer = new TestPlayer("John", "en_US");

        TestMessages trMsg = i18n.config(turkishPlayer, TestMessages.class);
        TestMessages enMsg = i18n.config(englishPlayer, TestMessages.class);

        assertThat(trMsg.welcome.asString()).contains("geldin");
        assertThat(enMsg.welcome.asString()).contains("Welcome");
    }
    
    @Test
    void testMessageWithPlaceholder() {
        TestMessages msg = i18n.config("en_US", TestMessages.class);

        String result = msg.welcome
            .with("player", "TestPlayer")
            .asString();

        assertThat(result).contains("TestPlayer");
    }
    
    @Test
    void testComponentGeneration() {
        TestMessages msg = i18n.config("en_US", TestMessages.class);

        Component component = msg.welcome
            .with("player", "TestPlayer")
            .component();

        assertThat(component).isNotNull();
    }
    
    @Test
    void testListMessage() {
        TestMessages msg = i18n.config("en_US", TestMessages.class);

        List<String> lines = msg.helpMenu.asList();

        assertThat(lines).isNotEmpty();
        assertThat(lines.size()).isGreaterThan(1);
    }
    
    @Test
    void testNestedConfig() {
        TestMessages msg = i18n.config("en_US", TestMessages.class);

        assertThat(msg.errors).isNotNull();
        assertThat(msg.errors.notFound.asString()).isNotNull();
    }
    
    @Test
    void testColorSchemeLoading() {
        assertThat(i18n.getColorScheme("dark")).isNotNull();
        assertThat(i18n.getColorScheme("light")).isNotNull();
    }
    
    @Test
    void testUserSchemePreference() {
        TestPlayer player = new TestPlayer("Deichor", "tr_TR");

        i18n.setUserScheme(player, "light");
        assertThat(i18n.getColorSchemeForUser(player)).isEqualTo(i18n.getColorScheme("light"));

        i18n.clearUserScheme(player);
        assertThat(i18n.getColorSchemeForUser(player)).isEqualTo(i18n.getDefaultColorScheme());
    }

    // ==================== Context System Tests ====================

    @Test
    void testContextBasicUsage() {
        TestPlayer player = new TestPlayer("Deichor", "en_US");

        try (var ctx = i18n.context(player)) {
            TestMessages msg = i18n.config(player, TestMessages.class);

            // Message should use context automatically
            Component component = msg.welcome
                .with("player", "Deichor")
                .component();

            assertThat(component).isNotNull();
        }
    }

    @Test
    void testContextWithColorScheme() {
        TestPlayer player = new TestPlayer("Deichor", "en_US");
        i18n.setUserScheme(player, "light");

        try (var ctx = i18n.context(player)) {
            TestMessages msg = i18n.config(player, TestMessages.class);

            // Should use light color scheme from context
            String result = msg.welcome
                .with("player", "Test")
                .asString();

            assertThat(result).isNotNull();
        }
    }

    @Test
    void testContextAutoCleanup() {
        TestPlayer player = new TestPlayer("Deichor", "en_US");

        {
            var ctx = i18n.context(player);
            ctx.close();
        }

        // Context should be cleared after close
        // This test just verifies no memory leak occurs
    }

    @Test
    void testNestedContext() {
        TestPlayer player1 = new TestPlayer("Player1", "en_US");
        TestPlayer player2 = new TestPlayer("Player2", "tr_TR");

        try (var ctx1 = i18n.context(player1)) {
            TestMessages msg1 = i18n.config(player1, TestMessages.class);

            try (var ctx2 = i18n.context(player2)) {
                TestMessages msg2 = i18n.config(player2, TestMessages.class);

                // Inner context should be active
                assertThat(msg2.welcome.asString()).contains("geldin");
            }

            // Outer context should be restored
            assertThat(msg1.welcome.asString()).contains("Welcome");
        }
    }

    @Test
    void testContextIsolationAcrossThreads() throws InterruptedException {
        TestPlayer player1 = new TestPlayer("Player1", "en_US");
        TestPlayer player2 = new TestPlayer("Player2", "tr_TR");

        Thread thread1 = new Thread(() -> {
            try (var ctx = i18n.context(player1)) {
                TestMessages msg = i18n.config(player1, TestMessages.class);
                String result = msg.welcome.with("player", "P1").asString();
                assertThat(result).contains("Welcome");
            }
        });

        Thread thread2 = new Thread(() -> {
            try (var ctx = i18n.context(player2)) {
                TestMessages msg = i18n.config(player2, TestMessages.class);
                String result = msg.welcome.with("player", "P2").asString();
                assertThat(result).contains("geldin");
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
    }
    
    // ==================== Test Helpers ====================
    
    static class TestPlayerLocaleProvider implements LocaleProvider<TestPlayer> {
        @Override
        public boolean supports(Class<?> entityClass) {
            return TestPlayer.class.isAssignableFrom(entityClass);
        }
        
        @Override
        public Locale getLocale(TestPlayer entity) {
            return Locale.forLanguageTag(entity.getLocale().replace("_", "-"));
        }
    }
    
    static class TestPlayer {
        private final String name;
        private final String locale;
        
        public TestPlayer(String name, String locale) {
            this.name = name;
            this.locale = locale;
        }
        
        public String getName() { return name; }
        public String getLocale() { return locale; }
    }
}
