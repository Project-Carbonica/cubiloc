// NOTE: This file is a standalone example, not part of the main source.
// Move to src/main/java/your/package/example/ and update package if you want to compile it.
package example;

import net.cubizor.cubicolor.api.ColorScheme;
import net.cubizor.cubiloc.I18n;
import net.cubizor.cubiloc.message.SingleMessageResult;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.util.List;

/**
 * Example usage demonstrating the Cubiloc I18n system.
 * Shows the new clean API: i18n.get(locale, config.message()).with().component()
 */
public class ExampleUsage {
    
    public static void main(String[] args) throws Exception {
        File dataFolder = new File("./test-data");
        
        // Initialize I18n system
        I18n i18n = new I18n("tr_TR");
        
        // Register the message config
        i18n.register(ExampleMessages.class)
            .path("messages")
            .suffix(".yml")
            .defaultLocale("tr_TR")
            .unpack(true)
            .dataFolder(dataFolder)
            .load();
        
        // Load color schemes from JSON files (Cubicolor native format)
        i18n.loadColorSchemeFromClasspath("dark", "themes/dark.json");
        i18n.loadColorSchemeFromClasspath("light", "themes/light.json");
        i18n.defaultScheme("dark");
        
        System.out.println("=== Theme Info ===");
        System.out.println("Dark scheme loaded: " + (i18n.getColorScheme("dark") != null));
        System.out.println("Light scheme loaded: " + (i18n.getColorScheme("light") != null));
        
        // Get messages config - Clean API!
        ExampleMessages msg = i18n.config("tr_TR", ExampleMessages.class);
        
        // ========================================
        // NEW CLEAN API: i18n.get(locale, config.message())
        // ========================================
        
        // Example 1: Simple message with placeholder
        System.out.println("\n=== Example 1: Simple Message ===");
        String welcomeText = i18n.get("tr_TR", msg.welcome())
            .with("player", "Deichor")
            .asString();
        System.out.println(welcomeText);
        
        // Example 2: Convert to Adventure Component (with ColorScheme)
        System.out.println("\n=== Example 2: Adventure Component ===");
        Component welcomeComponent = i18n.get("tr_TR", msg.welcome())
            .with("player", "Deichor")
            .component();
        System.out.println("Component: " + welcomeComponent);
        
        // Example 3: Multi-line messages as List<String>
        System.out.println("\n=== Example 3: Multi-line Message (asList) ===");
        List<String> helpLines = i18n.get("tr_TR", msg.helpMenu()).asList();
        helpLines.forEach(System.out::println);
        
        // Example 3b: Multi-line messages as List<Component>
        System.out.println("\n=== Example 3b: Multi-line Message (components) ===");
        List<Component> helpComponents = i18n.get("tr_TR", msg.helpMenu()).components();
        System.out.println("Total components: " + helpComponents.size());
        for (int i = 0; i < helpComponents.size(); i++) {
            Component comp = helpComponents.get(i);
            String colorInfo = extractColor(comp);
            System.out.printf("  [%d] %s%n", i, colorInfo);
        }
        
        // Example 4: Message with multiple placeholders
        System.out.println("\n=== Example 4: Multiple Placeholders ===");
        String balanceText = i18n.get("tr_TR", msg.balance())
            .with("amount", "1,250.50")
            .with("currency", "TL")
            .asString();
        System.out.println(balanceText);
        // Example 5: Nested subconfig - SUPER CLEAN!
        System.out.println("\n=== Example 5: Nested Messages ===");
        String notFoundText = i18n.get("tr_TR", msg.errors().notFound())
            .with("item", "Diamond Sword")
            .asString();
        System.out.println(notFoundText);
        
        // Example 6: Deeply nested - STILL CLEAN!
        System.out.println("\n=== Example 6: Deeply Nested Messages ===");
        Component kickComponent = i18n.get("tr_TR", msg.admin().maintenance().kickMessage())
            .with("time", "30 minutes")
            .component();
        System.out.println("Kick component: " + kickComponent);
        
        // Example 7: User with different locale
        System.out.println("\n=== Example 7: Different Locale (User-based) ===");
        MockPlayer player = new MockPlayer("en_US", "light");
        i18n.setUserScheme(player, player.getThemeMode()); // Set user's theme preference
        
        ExampleMessages playerMsg = i18n.config(player, ExampleMessages.class);
        String playerWelcome = i18n.get(player, playerMsg.welcome())
            .with("player", "TestPlayer")
            .asString();
        System.out.println("Player locale: " + player.getLocale());
        System.out.println(playerWelcome);
        
        // Example 8: Switch ColorScheme
        System.out.println("\n=== Example 8: Dark vs Light Mode ===");
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
        
        // Example 9: Direct component without placeholders
        System.out.println("\n=== Example 9: Direct Component ===");
        Component serverNameComponent = i18n.get(msg.serverName()).component();
        System.out.println("Server name: " + serverNameComponent);
        
        // Example 10: Simplified usage with default locale
        System.out.println("\n=== Example 10: Default Locale ===");
        SingleMessageResult result = i18n.get(msg.balance())
            .with("amount", "5000")
            .with("currency", "USD");
        System.out.println("String: " + result.asString());
        System.out.println("Component: " + result.component());
        
        // Example 11: Test ALL color tags
        System.out.println("\n=== Example 11: All Color Tags Test ===");
        String[] colorTests = {
            "<primary>PRIMARY</primary>",
            "<secondary>SECONDARY</secondary>",
            "<tertiary>TERTIARY</tertiary>",
            "<accent>ACCENT</accent>",
            "<error>ERROR</error>",
            "<success>SUCCESS</success>",
            "<warning>WARNING</warning>",
            "<info>INFO</info>",
            "<text>TEXT</text>",
            "<text_secondary>TEXT_SECONDARY</text_secondary>",
            "<background>BACKGROUND</background>",
            "<surface>SURFACE</surface>",
            "<border>BORDER</border>",
            "<overlay>OVERLAY</overlay>"
        };
        
        for (String test : colorTests) {
            Component comp = i18n.get(test).component();
            // Extract color from component string representation
            String compStr = comp.toString();
            String colorPart = "";
            if (compStr.contains("color=TextColorImpl{value=\"")) {
                int start = compStr.indexOf("color=TextColorImpl{value=\"") + 27;
                int end = compStr.indexOf("\"", start);
                colorPart = compStr.substring(start, end);
            }
            System.out.printf("  %-20s -> %s%n", test.replaceAll("<[^>]+>", ""), colorPart);
        }
        
        // Example 12: @lc Placeholder - Locale Config References
        System.out.println("\n=== Example 12: @lc Placeholder (Locale Config References) ===");
        
        // Direct @lc reference: {@lc:statusActive} -> resolves to "<success>Active</success>"
        System.out.println("Direct @lc reference:");
        String serverStatusResult = i18n.get(msg.serverStatus())
            .withConfig(msg)  // Pass the config for @lc resolution
            .asString();
        System.out.println("  serverStatus: " + serverStatusResult);
        Component serverStatusComp = i18n.get(msg.serverStatus())
            .withConfig(msg)
            .component();
        System.out.println("  Component: " + extractColor(serverStatusComp));
        
        // Conditional @lc: {statusOnline,statusOffline@lc#online}
        System.out.println("\nConditional @lc (online=true):");
        String playerOnline = i18n.get(msg.playerStatus())
            .withConfig(msg)
            .with("player", "Deichor")
            .with("online", true)
            .asString();
        System.out.println("  playerStatus (online=true): " + playerOnline);
        
        System.out.println("\nConditional @lc (online=false):");
        String playerOffline = i18n.get(msg.playerStatus())
            .withConfig(msg)
            .with("player", "Deichor")
            .with("online", false)
            .asString();
        System.out.println("  playerStatus (online=false): " + playerOffline);
        
        // Dynamic status with enabled field
        System.out.println("\nDynamic @lc (enabled=true vs enabled=false):");
        String enabledTrue = i18n.get(msg.dynamicStatus())
            .withConfig(msg)
            .with("name", "Feature X")
            .with("enabled", true)
            .asString();
        System.out.println("  dynamicStatus (enabled=true): " + enabledTrue);
        
        String enabledFalse = i18n.get(msg.dynamicStatus())
            .withConfig(msg)
            .with("name", "Feature X")
            .with("enabled", false)
            .asString();
        System.out.println("  dynamicStatus (enabled=false): " + enabledFalse);
        
        // Show component conversion with colors
        System.out.println("\nWith Component (colors applied):");
        Component enabledComp = i18n.get(msg.dynamicStatus())
            .withConfig(msg)
            .with("name", "PvP Mode")
            .with("enabled", true)
            .component();
        System.out.println("  " + extractColor(enabledComp));

        System.out.println("\n=== All Examples Completed ===");
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
        
        public String getLocale() {
            return locale;
        }
        
        public String getThemeMode() {
            return themeMode;
        }
    }
    
    /**
     * Helper to extract color from a Component for display.
     */
    private static String extractColor(Component comp) {
        String compStr = comp.toString();
        // Extract content
        String content = "";
        if (compStr.contains("content=\"")) {
            int start = compStr.indexOf("content=\"") + 9;
            int end = compStr.indexOf("\"", start);
            content = compStr.substring(start, end);
        }
        // Extract color
        String color = "no-color";
        if (compStr.contains("color=TextColorImpl{value=\"")) {
            int start = compStr.indexOf("color=TextColorImpl{value=\"") + 27;
            int end = compStr.indexOf("\"", start);
            color = compStr.substring(start, end);
        } else if (compStr.contains("color=NamedTextColor{name=\"")) {
            int start = compStr.indexOf("color=NamedTextColor{name=\"") + 27;
            int end = compStr.indexOf("\"", start);
            color = compStr.substring(start, end);
        }
        return String.format("'%s' (color: %s)", content, color);
    }
}
