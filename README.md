# Cubiloc

A powerful i18n (internationalization) API extension built on top of [okaeri-configs](https://github.com/OkaeriPoland/okaeri-configs) with [okaeri-placeholders](https://github.com/OkaeriPoland/okaeri-placeholders) and [Cubicolor](https://github.com/Project-Carbonica/Cubicolor) semantic color support.

## Features

- **Multi-locale support** - YAML-based message files per locale
- **Semantic colors** - Use `<primary>`, `<error>`, `<success>` etc. instead of hex codes
- **Placeholder system** - Powered by okaeri-placeholders
- **Adventure components** - Native MiniMessage and Component support
- **Nested configs** - Organize messages with subconfigs
- **@lc placeholders** - Reference other messages within messages
- **Theme switching** - Per-user dark/light mode support

## Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://repo.okaeri.cloud/releases")
    maven("https://nexus.cubizor.net/repository/maven-releases/")
}

dependencies {
    implementation("net.cubizor.cubiloc:cubiloc:1.0-SNAPSHOT")
}
```

## Quick Start

### 1. Create a Message Config

```java
public class Messages extends MessageConfig {
    
    private String welcome = "<success>Welcome {player}!</success>";
    private String balance = "<accent>Balance: {amount} {currency}</accent>";
    
    private List<String> helpMenu = Arrays.asList(
        "<accent>===== Help Menu =====</accent>",
        "<text>• /help - Show this menu</text>",
        "<text>• /spawn - Teleport to spawn</text>"
    );
    
    // Fluent getters
    public String welcome() { return welcome; }
    public String balance() { return balance; }
    public List<String> helpMenu() { return helpMenu; }
}
```

### 2. Initialize I18n

```java
I18n i18n = new I18n("en_US");

// Register message config
i18n.register(Messages.class)
    .path("messages")
    .suffix(".yml")
    .defaultLocale("en_US")
    .unpack(true)
    .dataFolder(dataFolder)
    .load();

// Load color schemes
i18n.loadColorSchemeFromClasspath("dark", "themes/dark.json");
i18n.loadColorSchemeFromClasspath("light", "themes/light.json");
i18n.defaultScheme("dark");
```

### 3. Use Messages

```java
Messages msg = i18n.config(player, Messages.class);

// Get as String
String text = i18n.get(player, msg.welcome())
    .with("player", playerName)
    .asString();

// Get as Adventure Component
Component component = i18n.get(player, msg.welcome())
    .with("player", playerName)
    .component();

// Multi-line as List<Component>
List<Component> lines = i18n.get(player, msg.helpMenu())
    .components();
```

## Semantic Color Tags

Use semantic tags instead of hardcoded colors. Colors are resolved from the active ColorScheme:

| Tag | Purpose |
|-----|---------|
| `<primary>` | Primary brand color |
| `<secondary>` | Secondary color |
| `<accent>` | Accent/highlight color |
| `<error>` | Error messages |
| `<success>` | Success messages |
| `<warning>` | Warning messages |
| `<info>` | Informational text |
| `<text>` | Primary text color |
| `<text_secondary>` | Secondary text color |

```yaml
# messages/en_US.yml
welcome: "<success>Welcome {player}!</success>"
error: "<error>Something went wrong!</error>"
balance: "<accent>Balance:</accent> <text>{amount}</text>"
```

## Theme System

Define themes as JSON files using Cubicolor's format:

```json
{
  "name": "Dark Theme",
  "colors": {
    "PRIMARY": "#6200EE",
    "ERROR": "#CF6679",
    "SUCCESS": "#4CAF50",
    "TEXT": "#FFFFFF"
  }
}
```

Switch themes per-user:

```java
// Set user's preferred theme
i18n.setUserScheme(player, "light");

// Get theme-aware component
Component msg = i18n.get(player, messages.welcome())
    .component(); // Uses player's theme
```

## @lc Placeholders

Reference other messages within messages:

```yaml
# Define status messages
statusOnline: "<success>Online</success>"
statusOffline: "<error>Offline</error>"

# Reference with {@lc:path}
serverStatus: "Server is {@lc:statusOnline}"

# Conditional: {trueValue,falseValue@lc#field}
playerStatus: "Player: {player} - {statusOnline,statusOffline@lc#online}"
```

```java
String status = i18n.get(msg.playerStatus())
    .withConfig(msg)
    .with("player", "Steve")
    .with("online", true)  // Resolves to statusOnline
    .asString();
```

## File Structure

```
plugins/YourPlugin/
├── messages/
│   ├── en_US.yml
│   ├── tr_TR.yml
│   └── de_DE.yml
└── themes/
    ├── dark.json
    └── light.json
```

## Tips & Tricks

### Nested Message Groups

Organize related messages with subconfigs:

```java
public class Messages extends MessageConfig {
    private ErrorMessages errors = new ErrorMessages();
    private AdminMessages admin = new AdminMessages();
    
    public static class ErrorMessages extends OkaeriConfig {
        private String notFound = "<error>Not found!</error>";
        public String notFound() { return notFound; }
    }
    
    public ErrorMessages errors() { return errors; }
}

// Usage
i18n.get(player, msg.errors().notFound()).component();
```

### Deeply Nested Access

```java
// Access deeply nested messages cleanly
Component kickMsg = i18n.get(player, msg.admin().maintenance().kickMessage())
    .with("time", "30 minutes")
    .component();
```

### Multi-line to Single Component

```java
// Join list into single component with newlines
Component joined = i18n.get(msg.helpMenu()).component();
```

### Override ColorScheme

```java
// Use specific scheme for this message
Component msg = i18n.get(message)
    .withColorScheme(i18n.getColorScheme("light"))
    .component();
```

### Type-Safe Results

- `SingleMessageResult` for `String` → use `.component()`
- `ListMessageResult` for `List<String>` → use `.components()` for list, `.component()` for joined

## YAML Message Example

```yaml
# messages/en_US.yml
################################
#   Game Messages              #
################################

welcome: "<success>Welcome {player}!</success>"
goodbye: "<text_secondary>Goodbye {player}, see you soon!</text_secondary>"

balance: "<accent>Balance:</accent> <text>{amount} {currency}</text>"

help-menu:
  - "<accent>========= Help =========</accent>"
  - "<text>• /help - Show this menu</text>"
  - "<text>• /spawn - Return to spawn</text>"
  - "<accent>========================</accent>"

errors:
  not-found: "<error>Item '{item}' not found!</error>"
  no-permission: "<error>You don't have permission!</error>"
  
admin:
  kick: "<success>{player} has been kicked.</success>"
  reload: "<success>Configuration reloaded!</success>"
```

## Dependencies

- [okaeri-configs](https://github.com/OkaeriPoland/okaeri-configs) - Configuration management
- [okaeri-placeholders](https://github.com/OkaeriPoland/okaeri-placeholders) - Placeholder processing
- [Cubicolor](https://github.com/Project-Carbonica/Cubicolor) - Semantic color system
- [Adventure](https://docs.advntr.dev/) - Text components & MiniMessage

## Dependency Injection Support

Cubiloc provides built-in support for popular DI frameworks. Both Guice and Dagger 2 are supported.

### I18nBuilder - Common Setup

Use `I18nBuilder` to create an `I18nProvider` for DI:

```java
I18nProvider provider = I18nBuilder.create("tr_TR")
    .register(MyMessages.class)
        .path("messages")
        .suffix(".yml")
        .unpack(true)
        .dataFolder(dataFolder)
        .done()
    .loadColorScheme("dark", "themes/dark.json")
    .loadColorScheme("light", "themes/light.json")
    .defaultScheme("dark")
    .buildProvider();
```

### Google Guice Integration

Add Guice dependency:

```kotlin
dependencies {
    implementation("com.google.inject:guice:7.0.0")
}
```

Create and use the module:

```java
// Create Guice injector with CubilocModule
Injector injector = Guice.createInjector(
    new CubilocModule(provider),
    new YourOtherModules()
);

// Inject in your classes
public class MessageService {
    private final I18nProvider i18nProvider;
    
    @Inject
    public MessageService(I18nProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }
    
    public void sendWelcome(Player player) {
        MyMessages msg = i18nProvider.config(player, MyMessages.class);
        Component message = i18nProvider.i18n().get(player, msg.welcome())
            .with("player", player.getName())
            .component();
        player.sendMessage(message);
    }
}
```

#### Advanced: Direct MessageConfig Binding

Use `CubilocConfigModule` for direct MessageConfig injection:

```java
CubilocConfigModule module = CubilocConfigModule.builder(provider)
    .bindConfig(MyMessages.class)           // Default locale
    .bindConfig("tr_TR", MyMessages.class)  // @Named("tr_TR")
    .bindConfig("en_US", MyMessages.class)  // @Named("en_US")
    .build();

// Now inject configs directly
public class MyService {
    @Inject
    public MyService(MyMessages messages) {
        // messages is the default locale config
    }
}

// Or with @Named for specific locale
public class LocalizedService {
    @Inject
    public LocalizedService(@Named("tr_TR") MyMessages turkishMessages) {
        // turkishMessages is the Turkish locale config
    }
}
```

### Dagger 2 Integration

Add Dagger dependency:

```kotlin
dependencies {
    implementation("com.google.dagger:dagger:2.51.1")
    annotationProcessor("com.google.dagger:dagger-compiler:2.51.1")
}
```

Define your component:

```java
@Singleton
@Component(modules = {CubilocDaggerModule.class})
public interface AppComponent {
    I18n i18n();
    I18nProvider i18nProvider();
    
    void inject(MyPlugin plugin);
    
    @Component.Builder
    interface Builder {
        Builder cubilocDaggerModule(CubilocDaggerModule module);
        AppComponent build();
    }
}
```

Build and use:

```java
// Build component
AppComponent component = DaggerAppComponent.builder()
    .cubilocDaggerModule(new CubilocDaggerModule(provider))
    .build();

// Inject into your plugin
component.inject(this);

// Or get services directly
MessageService service = component.messageService();
```

Inject in your classes:

```java
@Singleton
public class MessageService {
    private final I18nProvider i18nProvider;
    
    @Inject
    public MessageService(I18nProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }
    
    public Component getWelcomeMessage(Player player) {
        MyMessages msg = i18nProvider.config(player, MyMessages.class);
        return i18nProvider.i18n().get(player, msg.welcome())
            .with("player", player.getName())
            .component();
    }
}
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgements

Special thanks to [RedFoxRR](https://github.com/RedFoxRR) for their contributions and support.
