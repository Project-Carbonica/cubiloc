package net.cubizor.cubiloc.config;

import eu.okaeri.configs.OkaeriConfig;

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
 *     private String welcome = "Welcome {player}!";
 *     private List<String> helpMenu = Arrays.asList(
 *         "=== Help Menu ===",
 *         "Use /help for assistance"
 *     );
 *     
 *     // Subconfig support
 *     private ErrorMessages errors = new ErrorMessages();
 *     
 *     public static class ErrorMessages extends OkaeriConfig {
 *         private String notFound = "Item not found!";
 *         private String noPermission = "You don't have permission!";
 *     }
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
