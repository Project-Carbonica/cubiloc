// NOTE: This file is a standalone example, not part of the main source.
// Move to src/main/java/your/package/example/ and update package if you want to compile it.
package example;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import net.cubizor.cubiloc.config.MessageConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Example MessageConfig implementation demonstrating various features.
 * This shows how API users should structure their message configurations.
 * 
 * Usage with I18n:
 * <pre>
 * ExampleMessages msg = i18n.config(player, ExampleMessages.class);
 * i18n.get(player, msg.welcome()).with("player", "Deichor").component();
 * i18n.get(player, msg.admin().maintenance().kickMessage()).with("time", "30 min").component();
 * </pre>
 * 
 * Supports Cubicolor semantic color tags:
 * - &lt;primary&gt;, &lt;secondary&gt;, &lt;accent&gt; for branding colors
 * - &lt;error&gt;, &lt;success&gt;, &lt;warning&gt;, &lt;info&gt; for semantic colors
 * - &lt;text&gt;, &lt;text_secondary&gt; for text colors
 */
@Header("################################")
@Header("#   Example Message Config     #")
@Header("################################")
public class ExampleMessages extends MessageConfig {
    
    // ==================== Status Messages (for @lc references) ====================
    
    @Comment("Status: Active")
    private String statusActive = "<success>Active</success>";
    
    @Comment("Status: Inactive") 
    private String statusInactive = "<error>Inactive</error>";
    
    @Comment("Status: Online")
    private String statusOnline = "<success>Online</success>";
    
    @Comment("Status: Offline")
    private String statusOffline = "<text_secondary>Offline</text_secondary>";
    
    // ==================== Basic Messages ====================
    
    // Simple string message with placeholder - uses semantic colors
    @Comment("Welcome message shown when a player joins")
    private String welcome = "<success>Welcome {player}!</success>";
    
    // Simple message without placeholders - uses primary color
    @Comment("Server name display")
    private String serverName = "<primary><bold>CubiServer</bold></primary>";
    
    // List of strings (multi-line message) - uses semantic colors
    @Comment("Help menu displayed to players")
    private List<String> helpMenu = Arrays.asList(
        "<accent>============ Help Menu ============</accent>",
        "<text>• /help - Show this menu</text>",
        "<text>• /spawn - Teleport to spawn</text>",
        "<text>• /home - Teleport home</text>",
        "<accent>===================================</accent>"
    );
    
    // Message with multiple placeholders - uses accent color
    @Comment("Balance display message")
    private String balance = "<accent>Your balance: <text>{amount} {currency}</text></accent>";
    
    // ==================== @lc Placeholder Examples ====================
    
    @Comment("Player status using @lc conditional placeholder")
    @Comment("Syntax: {trueValue,falseValue@lc#field} - resolves to statusOnline or statusOffline based on 'online' field")
    private String playerStatus = "<text>Player: {player} - Status: {statusOnline,statusOffline@lc#online}</text>";
    
    @Comment("Server status using direct @lc reference")
    @Comment("Syntax: {@lc:path.to.message} - directly references another message")
    private String serverStatus = "<text>Server is {@lc:statusActive}</text>";
    
    @Comment("Dynamic status message with nested @lc reference")
    private String dynamicStatus = "<text>{name}: {statusActive,statusInactive@lc#enabled}</text>";
    
    // Subconfig for error messages
    @Comment("Error messages configuration")
    private ErrorMessages errors = new ErrorMessages();
    
    // Subconfig for admin messages
    @Comment("Admin-only messages")
    private AdminMessages admin = new AdminMessages();
    
    /**
     * Nested configuration for error messages.
     * Demonstrates subconfig support with semantic error colors.
     */
    public static class ErrorMessages extends OkaeriConfig {
        
        @Comment("Item not found error")
        private String notFound = "<error>Error: Item '{item}' not found!</error>";
        
        @Comment("No permission error")
        private String noPermission = "<error>You don't have permission to do that!</error>";
        
        @Comment("Invalid arguments error")
        private List<String> invalidArgs = Arrays.asList(
            "<error>Invalid arguments!</error>",
            "<warning>Usage: {usage}</warning>"
        );
        
        @Comment("Player not found")
        private String playerNotFound = "<error>Player '{player}' not found!</error>";
        
        public String notFound() {
            return notFound;
        }
        
        public ErrorMessages notFound(String notFound) {
            this.notFound = notFound;
            return this;
        }
        
        public String noPermission() {
            return noPermission;
        }
        
        public ErrorMessages noPermission(String noPermission) {
            this.noPermission = noPermission;
            return this;
        }
        
        public List<String> invalidArgs() {
            return invalidArgs;
        }
        
        public ErrorMessages invalidArgs(List<String> invalidArgs) {
            this.invalidArgs = invalidArgs;
            return this;
        }
        
        public String playerNotFound() {
            return playerNotFound;
        }
        
        public ErrorMessages playerNotFound(String playerNotFound) {
            this.playerNotFound = playerNotFound;
            return this;
        }
    }
    
    /**
     * Nested configuration for admin messages.
     * Uses success and warning semantic colors.
     */
    public static class AdminMessages extends OkaeriConfig {
        
        @Comment("Player kicked message")
        private String playerKicked = "<success>Player {player} has been kicked. Reason: {reason}</success>";
        
        @Comment("Server reload complete")
        private String reloadComplete = "<success>Server configuration reloaded successfully!</success>";
        
        @Comment("Maintenance mode messages")
        private MaintenanceMessages maintenance = new MaintenanceMessages();
        
        /**
         * Deeply nested configuration demonstrating multiple levels.
         * Uses warning and error semantic colors.
         */
        public static class MaintenanceMessages extends OkaeriConfig {
            
            @Comment("Maintenance mode enabled")
            private String enabled = "<warning>Maintenance mode enabled!</warning>";
            
            @Comment("Maintenance mode disabled")
            private String disabled = "<success>Maintenance mode disabled!</success>";
            
            @Comment("Kick message for non-admin players")
            private List<String> kickMessage = Arrays.asList(
                "<error><bold>Server Maintenance</bold></error>",
                "",
                "<text>The server is currently under maintenance.</text>",
                "<text>Please try again later.</text>",
                "",
                "<text_secondary>Estimated time: {time}</text_secondary>"
            );
            
            public String enabled() {
                return enabled;
            }
            
            public MaintenanceMessages enabled(String enabled) {
                this.enabled = enabled;
                return this;
            }
            
            public String disabled() {
                return disabled;
            }
            
            public MaintenanceMessages disabled(String disabled) {
                this.disabled = disabled;
                return this;
            }
            
            public List<String> kickMessage() {
                return kickMessage;
            }
            
            public MaintenanceMessages kickMessage(List<String> kickMessage) {
                this.kickMessage = kickMessage;
                return this;
            }
        }
        
        public String playerKicked() {
            return playerKicked;
        }
        
        public AdminMessages playerKicked(String playerKicked) {
            this.playerKicked = playerKicked;
            return this;
        }
        
        public String reloadComplete() {
            return reloadComplete;
        }
        
        public AdminMessages reloadComplete(String reloadComplete) {
            this.reloadComplete = reloadComplete;
            return this;
        }
        
        public MaintenanceMessages maintenance() {
            return maintenance;
        }
        
        public AdminMessages maintenance(MaintenanceMessages maintenance) {
            this.maintenance = maintenance;
            return this;
        }
    }
    
    // Fluent getters and setters
    
    public String welcome() {
        return welcome;
    }
    
    public ExampleMessages welcome(String welcome) {
        this.welcome = welcome;
        return this;
    }
    
    public String serverName() {
        return serverName;
    }
    
    public ExampleMessages serverName(String serverName) {
        this.serverName = serverName;
        return this;
    }
    
    public List<String> helpMenu() {
        return helpMenu;
    }
    
    public ExampleMessages helpMenu(List<String> helpMenu) {
        this.helpMenu = helpMenu;
        return this;
    }
    
    public String balance() {
        return balance;
    }
    
    public ExampleMessages balance(String balance) {
        this.balance = balance;
        return this;
    }
    
    public ErrorMessages errors() {
        return errors;
    }
    
    public ExampleMessages errors(ErrorMessages errors) {
        this.errors = errors;
        return this;
    }
    
    public AdminMessages admin() {
        return admin;
    }
    
    public ExampleMessages admin(AdminMessages admin) {
        this.admin = admin;
        return this;
    }
    
    // Status message getters/setters
    
    public String statusActive() {
        return statusActive;
    }
    
    public ExampleMessages statusActive(String statusActive) {
        this.statusActive = statusActive;
        return this;
    }
    
    public String statusInactive() {
        return statusInactive;
    }
    
    public ExampleMessages statusInactive(String statusInactive) {
        this.statusInactive = statusInactive;
        return this;
    }
    
    public String statusOnline() {
        return statusOnline;
    }
    
    public ExampleMessages statusOnline(String statusOnline) {
        this.statusOnline = statusOnline;
        return this;
    }
    
    public String statusOffline() {
        return statusOffline;
    }
    
    public ExampleMessages statusOffline(String statusOffline) {
        this.statusOffline = statusOffline;
        return this;
    }
    
    // @lc example getters/setters
    
    public String playerStatus() {
        return playerStatus;
    }
    
    public ExampleMessages playerStatus(String playerStatus) {
        this.playerStatus = playerStatus;
        return this;
    }
    
    public String serverStatus() {
        return serverStatus;
    }
    
    public ExampleMessages serverStatus(String serverStatus) {
        this.serverStatus = serverStatus;
        return this;
    }
    
    public String dynamicStatus() {
        return dynamicStatus;
    }
    
    public ExampleMessages dynamicStatus(String dynamicStatus) {
        this.dynamicStatus = dynamicStatus;
        return this;
    }
}
