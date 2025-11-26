// NOTE: This is a standalone example demonstrating Guice DI integration.
// Add to your project and update package as needed.
package example.di.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import example.ExampleMessages;
import net.cubizor.cubiloc.I18n;
import net.cubizor.cubiloc.inject.I18nBuilder;
import net.cubizor.cubiloc.inject.guice.CubilocModule;
import net.cubizor.cubiloc.locale.LocaleProvider;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.util.Locale;

/**
 * Example demonstrating Cubiloc integration with Google Guice.
 * 
 * <p>Uses the new LocaleProvider-based API where I18n directly handles
 * player locale resolution - no I18nProvider needed!</p>
 */
public class GuiceExample {
    
    public static void main(String[] args) throws Exception {
        File dataFolder = new File("./test-data");
        
        // ========================================
        // Step 1: Build I18n with LocaleProvider
        // ========================================
        I18n i18n = I18nBuilder.create(Locale.forLanguageTag("tr-TR"))
            .localeProvider(new MockPlayerLocaleProvider())
            .register(ExampleMessages.class)
                .path("messages")
                .suffix(".yml")
                .unpack(true)
                .dataFolder(dataFolder)
                .done()
            .loadColorScheme("dark", "themes/dark.json")
            .loadColorScheme("light", "themes/light.json")
            .defaultScheme("dark")
            .build();
        
        // ========================================
        // Step 2: Create Guice Injector
        // ========================================
        Injector injector = Guice.createInjector(
            new CubilocModule(i18n),  // Pass I18n directly
            new AppModule()
        );
        
        // ========================================
        // Step 3: Use injected services
        // ========================================
        MessageService service = injector.getInstance(MessageService.class);
        MockPlayer player = new MockPlayer("Deichor", "tr_TR");
        
        System.out.println("=== Guice DI Example ===");
        service.sendWelcome(player);
        service.sendBalance(player, "5000", "TL");
    }
    
    // ========================================
    // Service with I18n Injection
    // ========================================
    
    public static class MessageService {
        private final I18n i18n;
        
        @Inject
        public MessageService(I18n i18n) {
            this.i18n = i18n;
        }
        
        public void sendWelcome(MockPlayer player) {
            // NEW: i18n.config(player, Class) uses LocaleProvider automatically
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
    // App Module & LocaleProvider
    // ========================================
    
    public static class AppModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(MessageService.class);
        }
    }
    
    public static class MockPlayerLocaleProvider implements LocaleProvider<MockPlayer> {
        @Override
        public boolean supports(Class<?> entityClass) {
            return MockPlayer.class.isAssignableFrom(entityClass);
        }
        
        @Override
        public Locale getLocale(MockPlayer entity) {
            return Locale.forLanguageTag(entity.getLocale().replace("_", "-"));
        }
    }
    
    public static class MockPlayer {
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
