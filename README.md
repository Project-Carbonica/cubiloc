# Cubiloc

i18n API built on [okaeri-configs](https://github.com/OkaeriPoland/okaeri-configs) with [Cubicolor](https://github.com/Project-Carbonica/Cubicolor) semantic colors and Adventure component support.

## Features

- **Multi-locale** - YAML message files per locale
- **Semantic colors** - `<primary>`, `<error>`, `<success>` tags via Cubicolor
- **Adventure components** - Native MiniMessage support
- **Player locale detection** - Auto-detect from Bukkit/Paper/Velocity
- **Theme switching** - Per-user dark/light mode

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
// Create I18n
I18n i18n = new I18n(Locale.forLanguageTag("en-US"));

// Register locale provider for players
i18n.registerLocaleProvider(new PlayerLocaleProvider());

// Register message config
i18n.register(MyMessages.class)
    .path("messages")
    .dataFolder(dataFolder)
    .unpack(true)
    .load();

// Load color schemes
i18n.loadColorSchemeFromClasspath("dark", "themes/dark.json");
i18n.defaultScheme("dark");

// Get localized messages for player
MyMessages msg = i18n.config(player, MyMessages.class);

// Build component with placeholders
Component message = i18n.get(player, msg.welcome())
    .with("player", player.getName())
    .component();
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
        player.sendMessage(i18n.get(player, msg.welcome())
            .with("player", player.getName())
            .component());
    }
}
```

## Examples

See [`samples/example/`](samples/example/) for complete examples:
- `ExampleMessages.java` - Message config with nested subconfigs
- `ExampleUsage.java` - Full usage demonstration
- `SpringExample.java` - Spring DI integration

## Dependencies

- [okaeri-configs](https://github.com/OkaeriPoland/okaeri-configs)
- [okaeri-placeholders](https://github.com/OkaeriPoland/okaeri-placeholders)
- [Cubicolor](https://github.com/Project-Carbonica/Cubicolor)
- [Adventure](https://docs.advntr.dev/)

## License

MIT License - see [LICENSE](LICENSE)
