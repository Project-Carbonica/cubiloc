package net.cubizor.cubiloc;

import net.cubizor.cubiloc.config.MessageConfig;
import net.cubizor.cubiloc.message.SingleMessageResult;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class LocaleConfigInheritanceTest {

    public static class TestConfig extends MessageConfig {
        public SingleMessageResult prefix = SingleMessageResult.of("Prefix: ");
        public SingleMessageResult message = SingleMessageResult.of("{@lc:prefix}Message");
        
        public SubConfig sub = new SubConfig();
        
        public static class SubConfig extends MessageConfig {
            public SingleMessageResult subMessage = SingleMessageResult.of("{@lc:prefix}SubMessage");
        }
    }

    @Test
    public void testPrefixResolution() {
        I18n i18n = new I18n();
        // Register and load (we simulate load by manually creating and injecting)
        TestConfig config = new TestConfig();
        
        // Manual injection as I18n.load() does
        i18n.injectConfigIntoFields(config, config);
        
        // Test direct access
        assertThat(config.message.raw()).isEqualTo("{@lc:prefix}Message");
        assertThat(config.message.asString()).isEqualTo("Prefix: Message");
        
        // Test sub-config access
        assertThat(config.sub.subMessage.raw()).isEqualTo("{@lc:prefix}SubMessage");
        assertThat(config.sub.subMessage.asString()).isEqualTo("Prefix: SubMessage");
    }
    
    public static class KotlinStyleConfig extends MessageConfig {
        private SingleMessageResult prefix = SingleMessageResult.of("K-Prefix: ");
        public SingleMessageResult message = SingleMessageResult.of("{@lc:prefix}Message");

        public SingleMessageResult getPrefix() {
            return prefix;
        }
    }

    @Test
    public void testKotlinStylePrefixResolution() {
        I18n i18n = new I18n();
        KotlinStyleConfig config = new KotlinStyleConfig();
        
        i18n.injectConfigIntoFields(config, config);
        
        assertThat(config.message.asString()).isEqualTo("K-Prefix: Message");
    }
    
    public static class BaseConfig extends MessageConfig {
        public SingleMessageResult prefix = SingleMessageResult.of("BasePrefix: ");
    }

    public static class InheritedConfig extends BaseConfig {
        public SingleMessageResult message = SingleMessageResult.of("{@lc:prefix}Inherited");
    }

    @Test
    public void testInheritanceResolution() {
        I18n i18n = new I18n();
        InheritedConfig config = new InheritedConfig();
        
        i18n.injectConfigIntoFields(config, config);
        
        assertThat(config.message.asString()).isEqualTo("BasePrefix: Inherited");
    }

    // Deep inheritance classes
    public static class Level1 extends MessageConfig {
        public SingleMessageResult rootPrefix = SingleMessageResult.of("Root: ");
    }
    public static class Level2 extends Level1 {}
    public static class Level3 extends Level2 {
        public SingleMessageResult middlePrefix = SingleMessageResult.of("Middle: ");
    }
    public static class Level4 extends Level3 {}
    public static class Level5 extends Level4 {
        public SingleMessageResult deepMessage = SingleMessageResult.of("{@lc:rootPrefix}{@lc:middlePrefix}Deep");
    }

    @Test
    public void testDeepInheritanceResolution() {
        I18n i18n = new I18n();
        Level5 config = new Level5();
        
        i18n.injectConfigIntoFields(config, config);
        
        assertThat(config.deepMessage.asString()).isEqualTo("Root: Middle: Deep");
    }

    // Deep NESTED classes (Sub-configs)
    public static class DeepNestedRoot extends MessageConfig {
        public SingleMessageResult rootPrefix = SingleMessageResult.of("R:");
        public Level1 sub1 = new Level1();

        public static class Level1 extends MessageConfig {
            public SingleMessageResult l1Prefix = SingleMessageResult.of("L1:");
            public Level2 sub2 = new Level2();

            public static class Level2 extends MessageConfig {
                public SingleMessageResult l2Prefix = SingleMessageResult.of("L2:");
                public Level3 sub3 = new Level3();

                public static class Level3 extends MessageConfig {
                    public SingleMessageResult l3Prefix = SingleMessageResult.of("L3:");
                    public Level4 sub4 = new Level4();

                    public static class Level4 extends MessageConfig {
                        public SingleMessageResult deepMsg = SingleMessageResult.of("{@lc:rootPrefix}{@lc:sub1.l1Prefix}{@lc:sub1.sub2.l2Prefix}{@lc:sub1.sub2.sub3.l3Prefix}Done");
                    }
                }
            }
        }
    }

    @Test
    public void testDeepNestedResolution() {
        I18n i18n = new I18n();
        DeepNestedRoot config = new DeepNestedRoot();
        
        // This is what I18n.load() does internally
        i18n.injectConfigIntoFields(config, config);
        
        // Resolve from the deepest level
        String result = config.sub1.sub2.sub3.sub4.deepMsg.asString();
        assertThat(result).isEqualTo("R:L1:L2:L3:Done");
    }
}
