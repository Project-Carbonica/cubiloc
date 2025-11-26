// NOTE: This file is a standalone example, not part of the main source.
// Move to src/main/java/your/package/example/ and update package if you want to compile it.
package example;

import net.cubizor.cubicolor.api.ColorScheme;
import net.cubizor.cubiloc.I18n;
import net.cubizor.cubiloc.inject.I18nBuilder;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Example usage demonstrating the Cubiloc I18n system.
 * Shows the new LocaleProvider-based API: i18n.config(player, Class).
 */
public class ExampleUsage {
    
    public static void main(String[] args) throws Exception {
        File dataFolder = new File("./test-data");
        
        // ========================================
        // NEW API: Use I18nBuilder with LocaleProvider
        // ========================================
        I18n i18n = I18nBuilder.create(Locale.forLanguageTag("tr-TR"))
            // Register custom locale provider for MockPlayer
            .localeProvider(new MockLocaleProvider())
            // Register message config
            .register(ExampleMessages.class)
                .path("messages")
                .suffix(".yml")
                .unpack(true)
                .dataFolder(dataFolder)
                .done()
            // Load color schemes
            .loadColorScheme("dark", "themes/dark.json")
            .loadColorScheme("light", "themes/light.json")
            .defaultScheme("dark")
            .build();
        
        System.out.println("=== Theme Info ===");
        System.out.println("Dark scheme loaded: " + (i18n.getColorScheme("dark") != null));
        System.out.println("Light scheme loaded: " + (i18n.getColorScheme("light") != null));
        
        // Get messages config for default locale
        ExampleMessages msg = i18n.config("tr_TR", ExampleMessages.class);
        
        // ========================================
        // Example 1: Simple message with placeholder
        // ========================================
        System.out.println("\n=== Example 1: Simple Message ===");
        String welcomeText = i18n.get("tr_TR", msg.welcome())
            .with("player", "Deichor")
            .asString();
        System.out.println(welcomeText);
        
        // ========================================
        // Example 2: Adventure Component
        // ========================================
        System.out.println("\n=== Example 2: Adventure Component ===");
        Component welcomeComponent = i18n.get("tr_TR", msg.welcome())
            .with("player", "Deichor")
            .component();
        System.out.println("Component: " + welcomeComponent);
        
        // ========================================
        // Example 3: Multi-line messages
        // ========================================
        System.out.println("\n=== Example 3: Multi-line Message ===");
        List<String> helpLines = i18n.get("tr_TR", msg.helpMenu()).asList();
        helpLines.forEach(System.out::println);
        
        // ========================================
        // Example 4: Player-based locale (NEW!)
        // ========================================
        System.out.println("\n=== Example 4: Player-based Locale (NEW API) ===");
        MockPlayer player = new MockPlayer("en_US", "light");
        i18n.setUserScheme(player, player.getThemeMode());
        
        // NEW: i18n.config(player, Class) uses LocaleProvider to resolve locale
        ExampleMessages playerMsg = i18n.config(player, ExampleMessages.class);
        String playerWelcome = i18n.get(player, playerMsg.welcome())
            .with("player", player.getName())
            .asString();
        System.out.println("Player: " + player.getName());
        System.out.println("Locale: " + player.getLocale());
        System.out.println("Message: " + playerWelcome);
        
        // ========================================
        // Example 5: Nested messages
        // ========================================
        System.out.println("\n=== Example 5: Nested Messages ===");
        String notFoundText = i18n.get("tr_TR", msg.errors().notFound())
            .with("item", "Diamond Sword")
            .asString();
        System.out.println(notFoundText);
        
        // ========================================
        // Example 6: Deeply nested messages
        // ========================================
        System.out.println("\n=== Example 6: Deeply Nested Messages ===");
        Component kickComponent = i18n.get("tr_TR", msg.admin().maintenance().kickMessage())
            .with("time", "30 minutes")
            .component();
        System.out.println("Kick component: " + kickComponent);
        
        // ========================================
        // Example 7: Theme switching
        // ========================================
        System.out.println("\n=== Example 7: Dark vs Light Mode ===");
        ColorScheme darkScheme = i18n.getColorScheme("dark");
        ColorScheme lightScheme = i18n.getColorScheme("light");
        
        System.out.println("Dark mode:");
        Component darkComponent = i18n.get(msg.serverName())
            .withColorScheme(darkScheme).component();
        System.out.println("  " + darkComponent);
        
        System.out.println("Light mode:");
        Component lightComponent = i18n.get(msg.serverName())
            .withColorScheme(lightScheme)
            .component();
        System.out.println("  " + lightComponent);
        
        // ========================================
        // Example 8: @lc Placeholders
        // ========================================
        System.out.println("\n=== Example 8: @lc Placeholder ===");
        
        // Conditional @lc: {statusOnline,statusOffline@lc#online}
        String playerOnline = i18n.get(msg.playerStatus())
            .withConfig(msg)
            .with("player", "Deichor")
            .with("online", true)
            .asString();
        System.out.println("  online=true:  " + playerOnline);
        
        String playerOffline = i18n.get(msg.playerStatus())
            .withConfig(msg)
            .with("player", "Deichor")
            .with("online", false)
            .asString();
        System.out.println("  online=false: " + playerOffline);

        System.out.println("\n=== All Examples Completed ===");
    }
    
    /**
     * Mock LocaleProvider for demonstration.
     * In real usage, use PlayerLocaleProvider for Paper/Bukkit.
     */
    static class MockLocaleProvider implements net.cubizor.cubiloc.locale.LocaleProvider<MockPlayer> {
        @Override
        public boolean supports(Class<?> entityClass) {
            return MockPlayer.class.isAssignableFrom(entityClass);
        }
        
        @Override
        public Locale getLocale(MockPlayer entity) {
            return Locale.forLanguageTag(entity.getLocale().replace("_", "-"));
        }
    }
    
    /**
     * Mock player class to demonstrate locale provider functionality.
     */
    static class MockPlayer {
        private final String locale;
        private final String themeMode;
        
        public MockPlayer(String locale, String themeMode) {
            this.locale = locale;
            this.themeMode = themeMode;
        }
        
        public String getName() {
            return "TestPlayer";
        }
        
        public String getLocale() {
            return locale;
        }
        
        public String getThemeMode() {
            return themeMode;
        }
    }
}
