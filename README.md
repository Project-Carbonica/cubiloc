# Cubiloc

i18n API built on [okaeri-configs](https://github.com/OkaeriPoland/okaeri-configs) with [Cubicolor](https://github.com/Project-Carbonica/Cubicolor) semantic colors and Adventure component support.

## Features

- **Multi-locale** - YAML message files per locale
- **Semantic colors** - `<primary>`, `<error>`, `<success>` tags via Cubicolor
- **Adventure components** - Native MiniMessage support
- **Player locale detection** - Auto-detect from Bukkit/Paper/Velocity
- **Theme switching** - Per-user dark/light mode
- **Context system** - ThreadLocal context for zero-parameter message retrieval
- **Zero-Boilerplate** - Direct field access, no getters or annotation processors needed
- **Hybrid Creation** - Support for both constructors and static factory methods

## Installation

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

```java
// 1. Create I18n instance
I18n i18n = new I18n(Locale.forLanguageTag("en-US"));

// 2. Register locale provider for players
i18n.registerLocaleProvider(new PlayerLocaleProvider());

// 3. Register message config
i18n.register(MyMessages.class)
    .path("messages")
    .dataFolder(dataFolder)
    .unpack(true)
    .load();

// 4. Load color schemes
i18n.loadColorSchemeFromClasspath("dark", "themes/dark.json");
i18n.defaultScheme("dark");

// 5. Create message config class
public class MyMessages extends MessageConfig {
    // Direct fields - Okaeri automatically transforms YAML values to these types!
    public SingleMessageResult welcome = new SingleMessageResult("<primary>Welcome {player}!</primary>");
    public ListMessageResult menu = new ListMessageResult(List.of(
        "<text>• Line 1</text>", 
        "<text>• Line 2</text>"
    ));
    
    // Nested configs (just extend MessageConfig)
    public ErrorMessages errors = new ErrorMessages();

    public static class ErrorMessages extends MessageConfig {
        public SingleMessageResult notFound = new SingleMessageResult("<error>Item not found!</error>");
    }
}

// 6. Use with context (Zero-parameter messages)
try (var ctx = i18n.context(player)) {
    MyMessages msg = i18n.config(player, MyMessages.class);

    // Access fields directly!
    Component message = msg.welcome
        .with("player", player.getName())
        .component();

    player.sendMessage(message);
}
```

## Context System (Recommended!)

The context system eliminates the need to pass player objects around:

```java
// Context handles it automatically!
try (var ctx = i18n.context(player)) {
    MyMessages msg = i18n.config(player, MyMessages.class);
    
    // Access directly from field - context (locale/theme) is applied automatically!
    Component c = msg.welcome.with("name", "Deichor").component();

    // Even in nested methods - no player parameter needed!
    notifyStaff(msg);
}

void notifyStaff(MyMessages msg) {
    // Automatically uses the context set above!
    Component notification = msg.admin.announcement.component();
}
```

**Benefits:**
- ✅ Clean, concise code (Direct fields, no getters)
- ✅ Automatic locale and color scheme handling
- ✅ Thread-safe with ThreadLocal
- ✅ No memory leaks (AutoCloseable)
- ✅ Works in nested method calls

## Message Config Class Structure

```java
public class MyMessages extends MessageConfig {
    // 1. Use SingleMessageResult or ListMessageResult for fields
    // 2. Initialize with constructor OR .of() factory method
    public SingleMessageResult welcome = new SingleMessageResult("Welcome {player}!");
    public ListMessageResult help = ListMessageResult.of(List.of("Line 1", "Line 2"));

    // 3. Nested configs (simply extend MessageConfig again)
    public ErrorMessages errors = new ErrorMessages();

    public static class ErrorMessages extends MessageConfig {
        public SingleMessageResult notFound = new SingleMessageResult("Not found!");
    }
}
```

## Semantic Color Tags

| Tag | Purpose |
|-----|---------|
| `<primary>`, `<secondary>`, `<accent>` | Branding colors |
| `<error>`, `<success>`, `<warning>`, `<info>` | Status colors |
| `<text>`, `<text_secondary>` | Text colors |

## Spring Integration

I18n is a simple class that works with any DI framework:

```java
@Configuration
public class I18nConfig {
    
    @Bean
    public I18n i18n() {
        I18n i18n = new I18n(Locale.forLanguageTag("en-US"));
        i18n.registerLocaleProvider(new PlayerLocaleProvider());
        i18n.register(MyMessages.class)
            .path("messages")
            .dataFolder(new File("./data"))
            .load();
        return i18n;
    }
}

@Service
public class MessageService {
    private final I18n i18n;
    
    public MessageService(I18n i18n) {
        this.i18n = i18n;
    }
    
    public void sendWelcome(Player player) {
        MyMessages msg = i18n.config(player, MyMessages.class);
        // Direct field access
        player.sendMessage(msg.welcome
            .with("player", player.getName())
            .component());
    }
}
```

## Advanced Features

### Async Support

When working with async operations, context propagation needs care:

```java
try (var ctx = i18n.context(player)) {
    MyMessages msg = i18n.config(player, MyMessages.class);

    // WRONG - context lost in async task!
    CompletableFuture.runAsync(() -> {
        msg.welcome.component(); // May use wrong context!
    });

    // CORRECT - capture context before async
    var capturedMsg = msg.welcome.with("player", player.getName());
    CompletableFuture.runAsync(() -> {
        Component c = capturedMsg.component(); // Safe!
        player.sendMessage(c);
    });
}
```

### Nested Contexts

Contexts can be nested - inner context takes precedence:

```java
try (var ctx1 = i18n.context(player1)) {
    try (var ctx2 = i18n.context(player2)) {
        // Uses player2's locale
    }
    // Back to player1's locale
}
```

## Dependencies

- [okaeri-configs](https://github.com/OkaeriPoland/okaeri-configs)
- [okaeri-placeholders](https://github.com/OkaeriPoland/okaeri-placeholders)
- [Cubicolor](https://github.com/Project-Carbonica/Cubicolor)
- [Adventure](https://docs.advntr.dev/)

## License

MIT License - see [LICENSE](LICENSE)