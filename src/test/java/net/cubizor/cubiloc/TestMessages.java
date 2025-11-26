package net.cubizor.cubiloc;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import net.cubizor.cubiloc.config.MessageConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Test MessageConfig for unit tests.
 */
public class TestMessages extends MessageConfig {
    
    private String welcome = "<success>Welcome {player}!</success>";
    private String goodbye = "<text_secondary>Goodbye {player}, see you soon!</text_secondary>";
    private String serverName = "<primary><bold>TestServer</bold></primary>";
    private String balance = "<accent>Balance:</accent> <text>{amount} {currency}</text>";
    
    private List<String> helpMenu = Arrays.asList(
        "<accent>========= Help =========</accent>",
        "<text>• /help - Show this menu</text>",
        "<text>• /spawn - Return to spawn</text>",
        "<accent>========================</accent>"
    );
    
    // Status messages for @lc
    private String statusActive = "<success>Active</success>";
    private String statusInactive = "<error>Inactive</error>";
    private String statusOnline = "<success>Online</success>";
    private String statusOffline = "<text_secondary>Offline</text_secondary>";
    
    // @lc placeholder examples
    private String serverStatus = "<text>Server is {@lc:statusActive}</text>";
    private String playerStatus = "<text>Player: {player} - Status: {statusOnline,statusOffline@lc#online}</text>";
    private String dynamicStatus = "<text>{name}: {statusActive,statusInactive@lc#enabled}</text>";
    
    private ErrorMessages errors = new ErrorMessages();
    private AdminMessages admin = new AdminMessages();
    
    public static class ErrorMessages extends OkaeriConfig {
        private String notFound = "<error>Error: Item '{item}' not found!</error>";
        private String noPermission = "<error>You don't have permission!</error>";
        
        public String notFound() { return notFound; }
        public String noPermission() { return noPermission; }
    }
    
    public static class AdminMessages extends OkaeriConfig {
        private String playerKicked = "<success>Player {player} has been kicked.</success>";
        private MaintenanceMessages maintenance = new MaintenanceMessages();
        
        public static class MaintenanceMessages extends OkaeriConfig {
            private String enabled = "<warning>Maintenance mode enabled!</warning>";
            private List<String> kickMessage = Arrays.asList(
                "<error><bold>Server Maintenance</bold></error>",
                "<text>Please try again later.</text>",
                "<text_secondary>Estimated time: {time}</text_secondary>"
            );
            
            public String enabled() { return enabled; }
            public List<String> kickMessage() { return kickMessage; }
        }
        
        public String playerKicked() { return playerKicked; }
        public MaintenanceMessages maintenance() { return maintenance; }
    }
    
    // Getters
    public String welcome() { return welcome; }
    public String goodbye() { return goodbye; }
    public String serverName() { return serverName; }
    public String balance() { return balance; }
    public List<String> helpMenu() { return helpMenu; }
    public String statusActive() { return statusActive; }
    public String statusInactive() { return statusInactive; }
    public String statusOnline() { return statusOnline; }
    public String statusOffline() { return statusOffline; }
    public String serverStatus() { return serverStatus; }
    public String playerStatus() { return playerStatus; }
    public String dynamicStatus() { return dynamicStatus; }
    public ErrorMessages errors() { return errors; }
    public AdminMessages admin() { return admin; }
}
