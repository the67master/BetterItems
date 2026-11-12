# BetterItems

Custom items plugin for Spigot 1.12.2 (Java 8).

## Build
Run `mvn clean package`. The plugin jar will be created in `target/`.

## Commands
- `/betteritems help`
- `/betteritems list`
- `/betteritems give <player> <item> [amount]`
- `/betteritems reload`

Custom items are configured in `plugins/BetterItems/items.yml`.

## Notes
Custom item identity currently uses a hidden lore marker for compatibility with Spigot 1.12.2. A later module can replace this with version-specific NBT tagging for stronger identity.
