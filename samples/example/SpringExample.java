// NOTE: This file is a standalone example demonstrating Spring DI integration.
// Add Spring dependency and update package as needed.
package example;

import net.cubizor.cubiloc.I18n;
import net.cubizor.cubiloc.locale.LocaleProvider;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.util.Locale;

/**
 * Example demonstrating Cubiloc integration with Spring Framework.
 * 
 * <p>I18n is a simple class that can be easily configured as a Spring Bean.</p>
 * 
 * <h2>Spring Configuration</h2>
 * <pre>
 * {@literal @}Configuration
 * public class I18nConfig {
 *     
 *     {@literal @}Bean
 *     public I18n i18n() {
 *         I18n i18n = new I18n(Locale.forLanguageTag("tr-TR"));
 *         
 *         // Register locale providers
 *         i18n.registerLocaleProvider(new PlayerLocaleProvider());
 *         
 *         // Register message configs
 *         i18n.register(MyMessages.class)
 *             .path("messages")
 *             .suffix(".yml")
 *             .unpack(true)
 *             .dataFolder(new File("./data"))
 *             .load();
 *         
 *         // Load color schemes
 *         i18n.loadColorSchemeFromClasspath("dark", "themes/dark.json");
 *         i18n.loadColorSchemeFromClasspath("light", "themes/light.json");
 *         i18n.defaultScheme("dark");
 *         
 *         return i18n;
 *     }
 * }
 * </pre>
 * 
 * <h2>Using in Services</h2>
 * <pre>
 * {@literal @}Service
 * public class MessageService {
 *     private final I18n i18n;
 *     
 *     // Constructor injection (recommended)
 *     public MessageService(I18n i18n) {
 *         this.i18n = i18n;
 *     }
 *     
 *     public void sendWelcome(Player player) {
 *         MyMessages msg = i18n.config(player, MyMessages.class);
 *         Component message = i18n.get(player, msg.welcome())
 *             .with("player", player.getName())
 *             .component();
 *         player.sendMessage(message);
 *     }
 * }
 * </pre>
 */
public class SpringExample {
    
    public static void main(String[] args) throws Exception {
        // Simulate Spring bean creation
        I18n i18n = createI18nBean();
        
        // Simulate service usage
        MessageService service = new MessageService(i18n);
        
        MockPlayer player = new MockPlayer("Deichor", "tr_TR");
        service.sendWelcome(player);
        service.sendBalance(player, "5000", "TL");
    }
    
    // ========================================
    // Simulated Spring @Bean configuration
    // ========================================
    
    static I18n createI18nBean() throws Exception {
        File dataFolder = new File("./test-data");
        
        I18n i18n = new I18n(Locale.forLanguageTag("tr-TR"));
        
        // Register locale provider
        i18n.registerLocaleProvider(new MockPlayerLocaleProvider());
        
        // Register message config
        i18n.register(ExampleMessages.class)
            .path("messages")
            .suffix(".yml")
            .unpack(true)
            .dataFolder(dataFolder)
            .load();
        
        // Load color schemes
        i18n.loadColorSchemeFromClasspath("dark", "themes/dark.json");
        i18n.loadColorSchemeFromClasspath("light", "themes/light.json");
        i18n.defaultScheme("dark");
        
        return i18n;
    }
    
    // ========================================
    // Simulated Spring @Service
    // ========================================
    
    static class MessageService {
        private final I18n i18n;
        
        // @Autowired via constructor (Spring style)
        public MessageService(I18n i18n) {
            this.i18n = i18n;
        }
        
        public void sendWelcome(MockPlayer player) {
            ExampleMessages msg = i18n.config(player, ExampleMessages.class);
            Component message = i18n.get(player, msg.welcome())
                .with("player", player.getName())
                .component();
            System.out.println("Welcome: " + message);
        }
        
        public void sendBalance(MockPlayer player, String amount, String currency) {
            ExampleMessages msg = i18n.config(player, ExampleMessages.class);
            String text = i18n.get(player, msg.balance())
                .with("amount", amount)
                .with("currency", currency)
                .asString();
            System.out.println("Balance: " + text);
        }
    }
    
    // ========================================
    // LocaleProvider
    // ========================================
    
    static class MockPlayerLocaleProvider implements LocaleProvider<MockPlayer> {
        @Override
        public boolean supports(Class<?> entityClass) {
            return MockPlayer.class.isAssignableFrom(entityClass);
        }
        
        @Override
        public Locale getLocale(MockPlayer entity) {
            return Locale.forLanguageTag(entity.getLocale().replace("_", "-"));
        }
    }
    
    // ========================================
    // Mock Player
    // ========================================
    
    static class MockPlayer {
        private final String name;
        private final String locale;
        
        public MockPlayer(String name, String locale) {
            this.name = name;
            this.locale = locale;
        }
        
        public String getName() { return name; }
        public String getLocale() { return locale; }
    }
}
