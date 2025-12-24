package net.cubizor.cubiloc;

import net.cubizor.cubiloc.config.MessageConfig;
import net.cubizor.cubiloc.message.ListMessageResult;
import net.cubizor.cubiloc.message.SingleMessageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceholderTest {

    private I18n i18n;
    private File testDataFolder;

    @BeforeEach
    void setUp() {
        testDataFolder = new File("src/test/resources/placeholders");
        
        i18n = new I18n(Locale.US);
        
        // Register test config
        i18n.register(PlaceholderTestMessages.class)
            .path("messages")
            .suffix(".yml")
            .dataFolder(testDataFolder)
            .load();
    }

    @Test
    void testDirectReference() {
        PlaceholderTestMessages msg = i18n.config("en_US", PlaceholderTestMessages.class);
        
        // Should resolve {@lc:prefix} to "System » "
        assertThat(msg.simpleRef.asString()).isEqualTo("System » Hello");
    }

    @Test
    void testStandardPlaceholders() {
        PlaceholderTestMessages msg = i18n.config("en_US", PlaceholderTestMessages.class);
        
        // Should resolve {player}
        assertThat(msg.standard.with("player", "Deichor").asString())
            .isEqualTo("Hello Deichor");
    }

    @Test
    void testMixedPlaceholders() {
        PlaceholderTestMessages msg = i18n.config("en_US", PlaceholderTestMessages.class);
        
        // Should resolve {@lc:prefix} AND {player}
        assertThat(msg.mixed.with("player", "Deichor").asString())
            .isEqualTo("System » Welcome Deichor");
    }

    @Test
    void testNestedConfigReference() {
        PlaceholderTestMessages msg = i18n.config("en_US", PlaceholderTestMessages.class);
        
        // {@lc:error.notFound} should resolve to value in sub-config
        assertThat(msg.nestedRef.asString()).isEqualTo("Error: Not Found");
    }

    @Test
    void testReferenceFromSubConfig() {
        PlaceholderTestMessages msg = i18n.config("en_US", PlaceholderTestMessages.class);
        
        // Sub-config message referencing root prefix {@lc:prefix}
        assertThat(msg.error.accessRoot.asString()).isEqualTo("System » Critical Error");
    }

    @Test
    void testConditionalPlaceholder() {
        PlaceholderTestMessages msg = i18n.config("en_US", PlaceholderTestMessages.class);
        
        // {statusActive,statusInactive@lc#is_online}
        
        // True case
        assertThat(msg.conditional.with("is_online", true).asString())
            .isEqualTo("Status: Online");
            
        // False case
        assertThat(msg.conditional.with("is_online", false).asString())
            .isEqualTo("Status: Offline");
    }
    
    @Test
    void testConditionalPlaceholderWithStrings() {
        PlaceholderTestMessages msg = i18n.config("en_US", PlaceholderTestMessages.class);
        
        // "true", "yes", "on" should be true
        assertThat(msg.conditional.with("is_online", "yes").asString())
            .isEqualTo("Status: Online");
            
        // "false", "no", null should be false
        assertThat(msg.conditional.with("is_online", "no").asString())
            .isEqualTo("Status: Offline");
    }

    @Test
    void testFallback() {
        PlaceholderTestMessages msg = i18n.config("en_US", PlaceholderTestMessages.class);
        
        // {@lc:missing.path|DefaultValue}
        assertThat(msg.fallback.asString()).isEqualTo("Current: DefaultValue");
    }

    @Test
    void testListMessagePlaceholders() {
        PlaceholderTestMessages msg = i18n.config("en_US", PlaceholderTestMessages.class);
        
        List<String> lines = msg.listRef.with("player", "Deichor").asList();
        
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).isEqualTo("Header: System » "); // Resolved {@lc:prefix}
        assertThat(lines.get(1)).isEqualTo("User: Deichor"); // Resolved {player}
    }

    @Test
    void testGlobalPlaceholder() {
        // Register a global placeholder
        // Note: PlaceholderResolver accepts (object, args, context)
        i18n.getPlaceholders().registerPlaceholder(String.class, "reverse", (s, args, ctx) -> new StringBuilder(s).reverse().toString());
        
        PlaceholderTestMessages msg = i18n.config("en_US", PlaceholderTestMessages.class);
        
        // Use the global placeholder
        SingleMessageResult result = SingleMessageResult.of("Reversed: {val.reverse}")
                                    .withPlaceholders(i18n.getPlaceholders())
                                    .with("val", "abc");
        
        assertThat(result.asString()).isEqualTo("Reversed: cba");
    }

    // ==================== Test Configuration Class ====================

    public static class PlaceholderTestMessages extends MessageConfig {
        public SingleMessageResult prefix = SingleMessageResult.of("System » ");
        
        public SingleMessageResult statusActive = SingleMessageResult.of("Online");
        public SingleMessageResult statusInactive = SingleMessageResult.of("Offline");

        // 1. Direct Reference
        public SingleMessageResult simpleRef = SingleMessageResult.of("{@lc:prefix}Hello");
        
        // 2. Standard Placeholder
        public SingleMessageResult standard = SingleMessageResult.of("Hello {player}");
        
        // 3. Mixed
        public SingleMessageResult mixed = SingleMessageResult.of("{@lc:prefix}Welcome {player}");
        
        // 4. Nested Path Reference
        public SingleMessageResult nestedRef = SingleMessageResult.of("Error: {@lc:error.notFound}");
        
        // 5. Conditional
        public SingleMessageResult conditional = SingleMessageResult.of("Status: {statusActive,statusInactive@lc#is_online}");
        
        // 6. Fallback
        public SingleMessageResult fallback = SingleMessageResult.of("Current: {@lc:missing.key|DefaultValue}");

        // 7. List Message
        public ListMessageResult listRef = ListMessageResult.of(Arrays.asList(
            "Header: {@lc:prefix}",
            "User: {player}"
        ));

        public ErrorMessages error = new ErrorMessages();

        public static class ErrorMessages extends MessageConfig {
            public SingleMessageResult notFound = SingleMessageResult.of("Not Found");
            
            // Reference to root from sub-config
            public SingleMessageResult accessRoot = SingleMessageResult.of("{@lc:prefix}Critical Error");
        }
    }
}
