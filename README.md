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
// Build I18n
I18n i18n = I18nBuilder.create(Locale.forLanguageTag("en-US"))
    .registerPlayerLocaleProvider()  // Auto-detect player locale
    .register(MyMessages.class)
        .path("messages")
        .dataFolder(dataFolder)
        .unpack(true)
        .done()
    .loadColorScheme("dark", "themes/dark.json")
    .defaultScheme("dark")
    .build();

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

## DI Support

### Dagger 2

```java
I18n i18n = I18nBuilder.create(Locale.forLanguageTag("en-US"))
    .registerPlayerLocaleProvider()
    .register(MyMessages.class).path("messages").dataFolder(dataFolder).done()
    .build();

// In your module
@Module
public class MyModule {
    @Provides @Singleton
    I18n provideI18n() { return i18n; }
}

// In your service - use I18n directly
@Singleton
public class MessageService {
    private final I18n i18n;
    
    @Inject
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

### Guice

```java
Injector injector = Guice.createInjector(new CubilocModule(i18n));
```

## Examples

See [`samples/example/`](samples/example/) for complete examples:
- `ExampleMessages.java` - Message config with nested subconfigs
- `ExampleUsage.java` - Full usage demonstration

## Dependencies

- [okaeri-configs](https://github.com/OkaeriPoland/okaeri-configs)
- [okaeri-placeholders](https://github.com/OkaeriPoland/okaeri-placeholders)
- [Cubicolor](https://github.com/Project-Carbonica/Cubicolor)
- [Adventure](https://docs.advntr.dev/)

## License

MIT License - see [LICENSE](LICENSE)
