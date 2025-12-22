package net.cubizor.cubiloc.config;

import eu.okaeri.configs.OkaeriConfig;
import net.cubizor.cubiloc.context.I18nContextHolder;
import net.cubizor.cubiloc.message.ListMessageResult;
import net.cubizor.cubiloc.message.SingleMessageResult;

import java.util.List;

/**
 * Abstract base class for message configurations.
 * Extends OkaeriConfig to provide full configuration capabilities including:
 * - String and List<String> fields
 * - Subconfig support (nested classes extending OkaeriConfig)
 * - Serializable objects
 * - All standard okaeri-configs features (comments, variables, etc.)
 * 
 * Users of the API should extend this class to define their message structures.
 * 
 * Example usage:
 * <pre>
 * public class MyMessages extends MessageConfig {
 *     public SingleMessageResult welcome = SingleMessageResult.of("Welcome {player}!");
 *     public ListMessageResult helpMenu = ListMessageResult.of(Arrays.asList(
 *         "=== Help Menu ===",
 *         "Use /help for assistance"
 *     ));
 *
 *     // Subconfig support
 *     public ErrorMessages errors = new ErrorMessages();
 *
 *     public static class ErrorMessages extends SubConfig {
 *         public SingleMessageResult notFound = SingleMessageResult.of("Item not found!");
 *     }
 * }
 * </pre>
 *
 * With I18n context:
 * <pre>
 * try (var ctx = i18n.context(player)) {
 *     MyMessages msg = i18n.config(MyMessages.class);
 *
 *     // Automatically uses player's locale and color scheme!
 *     Component c = msg.welcome.with("player", "Deichor").component();
 * }
 * </pre>
 */
public abstract class MessageConfig extends OkaeriConfig {
    
    /**
     * Gets the locale identifier for this message config.
     * Can be overridden to provide custom locale handling.
     *
     * @return the locale identifier (e.g., "tr_TR", "en_US")
     */
    public String getLocale() {
        if (this.getConfigurer() != null && this.getBindFile() != null) {
            String fileName = this.getBindFile().toFile().getName();
            return fileName.replace(".yml", "").replace(".yaml", "");
        }
        return "default";
    }
}
