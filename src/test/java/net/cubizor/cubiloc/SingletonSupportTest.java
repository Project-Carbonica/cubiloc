package net.cubizor.cubiloc;

import net.cubizor.cubiloc.config.MessageConfig;
import net.cubizor.cubiloc.message.SingleMessageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import static org.assertj.core.api.Assertions.assertThat;

public class SingletonSupportTest {

    public static class SingletonConfig extends MessageConfig {
        // This is what Kotlin generates for an 'object'
        public static transient final SingletonConfig INSTANCE = new SingletonConfig();

        public SingleMessageResult prefix = SingleMessageResult.of("Prefix: ");
        public SingleMessageResult message = SingleMessageResult.of("{@lc:prefix}Message");
    }

    @Test
    public void testSingletonUpdate(@TempDir File tempDir) {
        I18n i18n = new I18n();
        
        // Register and load using the real I18n flow
        i18n.register(SingletonConfig.class)
            .dataFolder(tempDir)
            .load();
        
        // 3. Proof that the static Singleton instance NOW WORKS
        // The fix in I18n should have updated the INSTANCE during load()
        assertThat(SingletonConfig.INSTANCE.message.asString())
                .describedAs("The static Singleton INSTANCE should be updated by I18n.load()")
                .isEqualTo("Prefix: Message");
    }
}