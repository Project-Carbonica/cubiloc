package net.cubizor.cubiloc;

import net.cubizor.cubiloc.config.MessageConfig;
import net.cubizor.cubiloc.message.SingleMessageResult;
import net.cubizor.cubiloc.message.ListMessageResult;

import java.util.Arrays;

/**
 * Test MessageConfig for unit tests.
 * Hybrid approach: demonstrating both constructors and .of() methods.
 */
public class TestMessages extends MessageConfig {

    // Using constructors
    public SingleMessageResult welcome = new SingleMessageResult("<success>Welcome {player}!</success>");
    public SingleMessageResult goodbye = new SingleMessageResult("<text_secondary>Goodbye {player}, see you soon!</text_secondary>");
    
    // Using .of() factory methods
    public SingleMessageResult serverName = SingleMessageResult.of("<primary><bold>TestServer</bold></primary>");
    public SingleMessageResult balance = SingleMessageResult.of("<accent>Balance:</accent> <text>{amount} {currency}</text>");
    
    public ListMessageResult helpMenu = new ListMessageResult(Arrays.asList(
        "<accent>========= Help =========</accent>",
        "<text>• /help - Show this menu</text>",
        "<text>• /spawn - Return to spawn</text>",
        "<accent>========================</accent>"
    ));

    // Status messages for @lc
    public SingleMessageResult statusActive = new SingleMessageResult("<success>Active</success>");
    public SingleMessageResult statusInactive = new SingleMessageResult("<error>Inactive</error>");
    public SingleMessageResult statusOnline = SingleMessageResult.of("<success>Online</success>");
    public SingleMessageResult statusOffline = SingleMessageResult.of("<text_secondary>Offline</text_secondary>");

    // @lc placeholder examples
    public SingleMessageResult serverStatus = new SingleMessageResult("<text>Server is {@lc:statusActive}</text>");
    public SingleMessageResult playerStatus = new SingleMessageResult("<text>Player: {player} - Status: {statusOnline,statusOffline@lc#online}</text>");
    public SingleMessageResult dynamicStatus = new SingleMessageResult("<text>{name}: {statusActive,statusInactive@lc#enabled}</text>");

    public ErrorMessages errors = new ErrorMessages();
    public OkaeriErrorMessages okaeriErrors = new OkaeriErrorMessages();
    public AdminMessages admin = new AdminMessages();

    public static class ErrorMessages extends MessageConfig {
        public SingleMessageResult notFound = new SingleMessageResult("<error>Error: Item '{item}' not found!</error>");
        public SingleMessageResult noPermission = SingleMessageResult.of("<error>You don't have permission!</error>");
    }

    public static class OkaeriErrorMessages extends eu.okaeri.configs.OkaeriConfig {
        public SingleMessageResult noPermission = SingleMessageResult.of("<error>Okaeri Error: No permission!</error>");
    }

    public static class AdminMessages extends MessageConfig {
        public SingleMessageResult playerKicked = new SingleMessageResult("<success>Player {player} has been kicked.</success>");
        public MaintenanceMessages maintenance = new MaintenanceMessages();

        public static class MaintenanceMessages extends MessageConfig {
            public SingleMessageResult enabled = new SingleMessageResult("<warning>Maintenance mode enabled!</warning>");
            public ListMessageResult kickMessage = ListMessageResult.of(Arrays.asList(
                "<error><bold>Server Maintenance</bold></error>",
                "<text>Please try again later.</text>",
                "<text_secondary>Estimated time: {time}</text_secondary>"
            ));
        }
    }
}
