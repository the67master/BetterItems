# BetterItems

Custom items framework for **Spigot 1.12.2 / Java 8**.

## Build
Use Maven: `mvn clean package`. Output: `target/betteritems-1.0.0.jar`.

## Commands
- `/bi help`
- `/bi list`
- `/bi give <player> <item> [amount]`
- `/bi editor`
- `/bi reload`

Permission: `betteritems.admin` for administration, `betteritems.use` for ability use. Each item or ability can also declare its own permission node.

## YAML abilities

Items live in `plugins/BetterItems/items.yml`. Use `abilities` to define multiple actions per item:

```yaml
items:
  donor_book:
    material: BOOK
    name: "&6Donor Book"
    abilities:
      rank:
        trigger: RIGHT_CLICK
        type: COMMAND
        command: "lp user {player} parent set donador"
```

Supported triggers: `RIGHT_CLICK`, `LEFT_CLICK`, `HIT`.

Ability parameters include `cooldown` (seconds), `cooldown-bar-ticks` (client item cooldown indicator), `permission`, `sound`, `particle`, `particle-count`, `particle-color` (RGB 0-255 for legacy colored dust), `duration`, `level`, `amount`, `radius`, `range`, `multiplier`, and ability-specific options.

## Included ability types (49)
COMMAND, RUN_COMMAND, LIGHTNING, LIGHTNING_DAMAGE, HEAL, FEED, SATURATION, SPEED, SLOWNESS, JUMP, STRENGTH, RESISTANCE, FIRE_RESISTANCE, WATER_BREATHING, NIGHT_VISION, INVISIBILITY, REGENERATION, ABSORPTION, DAMAGE, KNOCKBACK, LAUNCH, TELEPORT_FORWARD, EXPLOSION_EFFECT, FIREBALL, SNOWBALL, ENDER_PEARL, ARROW, POISON, WITHER, BLINDNESS, WEAKNESS, SLOW_TARGET, POTION_ON_HIT, FIRE_TARGET, EXTINGUISH, CLEANSE, CURE_POISON, CRITICAL_ALL, CRITICAL_HIT, PARTICLE_BURST, SOUND, PULL_NEARBY, PUSH_NEARBY, AREA_HEAL, AREA_DAMAGE, DOLPHINS_GRACE, HASTE, MINING_FATIGUE, LEVITATION.

For `POTION_ON_HIT`, set `effect`, `level`, and `duration`. Critical abilities accept `multiplier`.

## Passive potion effects

Attach effects while an item is held, worn as armor, or in the inventory:

```yaml
passive-effects:
  haste:
    while: HELD # HELD, ARMOR, ANY
    effect: FAST_DIGGING
    level: 2
    duration: 60
```

Potion effect duration is refreshed by the passive-effect task. Armor effects are read from equipped armor slots.

## Crafting recipes

```yaml
recipe:
  enabled: true
  shape:
    - " G "
    - " G "
    - " S "
  ingredients:
    G: GOLD_INGOT
    S: STICK
```

## Compatibility notes
- Item identity currently uses a hidden lore marker to stay compatible with legacy Spigot 1.12.2 APIs. Avoid removing the hidden marker when manually editing lore.
- Bukkit's item cooldown indicator is material-based in this server version, so another stack of the same material can show the same client cooldown bar.
- Colored dust is rendered through Spigot's legacy colored-dust effect. Particle names must exist in the 1.12.2 API.
- Only configure commands that are safe to execute from console. The `{player}` and `{uuid}` placeholders are replaced before dispatch.
