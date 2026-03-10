# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Cubiloc is a Kotlin i18n library with [Cubicolor](https://github.com/Project-Carbonica/Cubicolor) semantic color tags and [Kyori Adventure](https://docs.advntr.dev/) component support. Messages live in YAML files (single source of truth), with type-safe key constants auto-generated at build time.

## Build & Test Commands

```bash
./gradlew build                                                     # Build
./gradlew test                                                      # Run all tests
./gradlew test --tests "net.cubizor.cubiloc.I18nTest"               # Single test class
./gradlew test --tests "net.cubizor.cubiloc.I18nTest.test name"     # Single test method
./gradlew generateMessageKeys                                       # Regenerate M.kt from YAML
```

Cubicolor dependencies require `GITHUB_ACTOR`/`GITHUB_TOKEN` env vars or `gpr.user`/`gpr.key` Gradle properties.

## Architecture

### Core Flow

1. Create `I18n(Locale)` — default locale is always explicit, no hardcoded default
2. `i18n.loadMessages("messages", dataFolder)` — loads all YAML files from the directory
3. Load color schemes/themes from JSON
4. `i18n.context(player).use { ... }` — sets ThreadLocal context with locale + color scheme
5. `i18n.message(M.WELCOME).with("player" to name).component()` — resolves message for current context

### Key Components

- **`I18n`** — Central entry point. Loads YAML messages into flat `Map<String, Any>` per locale, manages color schemes, locale providers, and okaeri-placeholders.
- **`SingleMessageResult` / `ListMessageResult`** — Copy-on-write message holders. `.with()` returns a new instance. `.component()` resolves via MiniMessage + context color scheme. `.asString()` for raw text.
- **`I18nContext` / `I18nContextHolder`** — ThreadLocal stack-based context (AutoCloseable). Nested contexts restore previous on close.
- **`YamlMessageLoader`** — Parses YAML via SnakeYAML and flattens nested keys to dot notation (`errors.notFound`).
- **`MessageReference`** — Resolves `{@key}` inter-message references and `{trueKey,falseKey@#field}` conditional references from the message map.
- **`MessageResolver`** — Shared logic for okaeri-placeholders resolution and MiniMessage building with color/theme tag resolvers.
- **`M` object** — Auto-generated from `src/main/resources/messages/en_US.yml` by `generateMessageKeys` Gradle task. Provides `const val` keys for compile-time safety.

### Build Structure

- **`build-logic/`** — Convention plugin (`cubiloc.library-conventions`) applies Kotlin JVM, java-library, maven-publish, and defines `GenerateMessageKeysTask`.
- **`gradle/libs.versions.toml`** — Version catalog for all dependencies.
- **`build.gradle.kts`** — Applies convention plugin, declares dependencies, registers `generateMessageKeys` task.

### Message Reference Syntax

- `{@key}` — Direct reference to another message in the same locale
- `{@key|fallback}` — Reference with fallback if key missing
- `{trueKey,falseKey@#field}` — Conditional: uses placeholder `field` as boolean to pick which key

## Conventions

- **Language**: Kotlin, JVM 21
- **Package**: `net.cubizor.cubiloc`
- **Commits**: Semantic commit messages (`feat:`, `fix:`, `refactor:`, `chore:`) — semantic-release auto-publishes
- **Messages**: YAML files only (no message defaults in code). YAML = single source of truth.
- **Themes**: JSON for color schemes (Cubicolor format). Support both `ColorScheme` and `MessageTheme` (with decorations).
- **14 semantic color tags**: `<primary>`, `<secondary>`, `<tertiary>`, `<accent>`, `<error>`, `<success>`, `<warning>`, `<info>`, `<text>`, `<text_secondary>`, `<background>`, `<surface>`, `<border>`, `<overlay>`
- **Publishing**: GitHub Packages Maven repository
