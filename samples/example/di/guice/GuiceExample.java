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
import net.cubizor.cubiloc.inject.I18nProvider;
import net.cubizor.cubiloc.inject.guice.CubilocModule;
import net.kyori.adventure.text.Component;

import java.io.File;

/**
 * Example demonstrating Cubiloc integration with Google Guice.
 * 
 * <p>This example shows:</p>
 * <ul>
 *   <li>How to set up I18n with I18nBuilder</li>
 *   <li>How to create a Guice module with CubilocModule</li>
 *   <li>How to inject I18n and I18nProvider into services</li>
 * </ul>
 */
public class GuiceExample {
    
    public static void main(String[] args) throws Exception {
        File dataFolder = new File("./test-data");
        
        // ========================================
        // Step 1: Build I18nProvider using I18nBuilder
        // ========================================
        I18nProvider provider = I18nBuilder.create("tr_TR")
            .register(ExampleMessages.class)
                .path("messages")
                .suffix(".yml")
                .unpack(true)
                .dataFolder(dataFolder)
                .done()
            .loadColorScheme("dark", "themes/dark.json")
            .loadColorScheme("light", "themes/light.json")
            .defaultScheme("dark")
            .buildProvider();
        
        // ========================================
        // Step 2: Create Guice Injector with CubilocModule
        // ========================================
        Injector injector = Guice.createInjector(
            new CubilocModule(provider),
            new AppModule()
        );
        
        // ========================================
        // Step 3: Get services with injected I18n
        // ========================================
        MessageService messageService = injector.getInstance(MessageService.class);
        
        // Use the service
        MockPlayer player = new MockPlayer("Deichor", "tr_TR");
        
        System.out.println("=== Guice DI Example ===");
        messageService.sendWelcome(player);
        messageService.sendBalance(player, "5000", "TL");
        messageService.sendError(player, "test-item");
    }
    
    /**
     * Example service that receives I18nProvider via dependency injection.
     */
    public static class MessageService {
        
        private final I18nProvider i18nProvider;
        
        @Inject
        public MessageService(I18nProvider i18nProvider) {
            this.i18nProvider = i18nProvider;
        }
        
        public void sendWelcome(MockPlayer player) {
            ExampleMessages msg = i18nProvider.config(player, ExampleMessages.class);
            Component message = i18nProvider.i18n().get(player, msg.welcome())
                .with("player", player.getName())
                .component();
            
            System.out.println("Welcome: " + message);
        }
        
        public void sendBalance(MockPlayer player, String amount, String currency) {
            ExampleMessages msg = i18nProvider.config(player, ExampleMessages.class);
            String text = i18nProvider.i18n().get(player, msg.balance())
                .with("amount", amount)
                .with("currency", currency)
                .asString();
            
            System.out.println("Balance: " + text);
        }
        
        public void sendError(MockPlayer player, String item) {
            ExampleMessages msg = i18nProvider.config(player, ExampleMessages.class);
            Component error = i18nProvider.i18n().get(player, msg.errors().notFound())
                .with("item", item)
                .component();
            
            System.out.println("Error: " + error);
        }
    }
    
    /**
     * Example service that directly injects I18n.
     */
    public static class AnotherService {
        
        private final I18n i18n;
        
        @Inject
        public AnotherService(I18n i18n) {
            this.i18n = i18n;
        }
        
        public void doSomething() {
            ExampleMessages msg = i18n.config(ExampleMessages.class);
            System.out.println("Server: " + i18n.get(msg.serverName()).asString());
        }
    }
    
    /**
     * Your application's other modules.
     */
    public static class AppModule extends AbstractModule {
        @Override
        protected void configure() {
            // Bind your other services here
            bind(MessageService.class);
            bind(AnotherService.class);
        }
    }
    
    /**
     * Mock player for testing.
     */
    public static class MockPlayer {
        private final String name;
        private final String locale;
        
        public MockPlayer(String name, String locale) {
            this.name = name;
            this.locale = locale;
        }
        
        public String getName() {
            return name;
        }
        
        public String getLocale() {
            return locale;
        }
    }
}
