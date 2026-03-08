# NexoAddon

NexoAddon is a feature pack for the [Nexo](https://polymart.org/resource/nexo.4231) plugin that backports legacy behaviour and ships new content on top of the official API. It extends Nexo's item, block and world systems with extra mechanics, visual polish and admin tooling while staying fully server-friendly.

## Table of contents
- [Key features](#key-features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Commands & permissions](#commands--permissions)
- [Configuration](#configuration)
- [World generation populators](#world-generation-populators)
- [Recipe management](#recipe-management)
- [Building from source](#building-from-source)
- [Support](#support)

## Key features
- **Extended mechanics for every item type.** Toggle or configure systems such as Big Mining, VeinMiner, bedrock breaking, aura particles, spawner drops, automatic fishing, enchant application limits, block aura visuals, experience bottles and much more straight from your Nexo item YAML files.【F:src/main/java/zone/vao/nexoAddon/items/Mechanics.java†L16-L158】【F:src/main/java/zone/vao/nexoAddon/utils/ItemConfigUtil.java†L43-L205】
- **Additional item components.** Allow custom items to become equippable in specific slots, behave as jukebox records, fertilise crops or expose skull and note block data using familiar component definitions.【F:src/main/java/zone/vao/nexoAddon/items/Components.java†L11-L51】【F:src/main/java/zone/vao/nexoAddon/utils/ItemConfigUtil.java†L29-L101】
- **Custom world population.** Generate ores, saplings, trees and furniture powered by Folia-friendly schedulers, including clustering logic, biome filters and placement rules per world.【F:src/main/java/zone/vao/nexoAddon/NexoAddon.java†L188-L215】【F:src/main/java/zone/vao/nexoAddon/populators/CustomChunkGenerator.java†L12-L23】【F:src/main/java/zone/vao/nexoAddon/events/chunk/ChunkLoadListener.java†L8-L13】【F:src/main/java/zone/vao/nexoAddon/events/chunk/FurniturePopulator.java†L15-L94】【F:src/main/java/zone/vao/nexoAddon/events/chunk/SaplingPopulator.java†L18-L96】
- **Player feedback & quality-of-life.** Boss bars and particle auras react to furniture focus, holograms display crop evolution, and PacketEvents-powered totem animations can be triggered for any player.【F:src/main/java/zone/vao/nexoAddon/NexoAddon.java†L65-L169】【F:src/main/java/zone/vao/nexoAddon/events/player/PlayerMovementListener.java†L13-L16】【F:src/main/java/zone/vao/nexoAddon/events/nexo/furnitures/interacts/DisplayCropsHologram.java†L13-L23】【F:src/main/java/zone/vao/nexoAddon/commands/NexoAddonCommand.java†L16-L45】
- **Automation helpers.** Automatic recipe registration, update checking, metrics reporting and Folia-compatible schedulers keep the addon responsive without sacrificing performance.【F:src/main/java/zone/vao/nexoAddon/NexoAddon.java†L101-L169】【F:src/main/java/zone/vao/nexoAddon/utils/handlers/RecipeManager.java†L13-L27】【F:src/main/java/zone/vao/nexoAddon/utils/metrics/Metrics.java†L13-L26】

## Requirements
- **Minecraft server:** Paper or compatible fork running 1.21.1 (matches the compiled NMS dependency).【F:pom.xml†L61-L97】
- **Java:** JDK 21 or newer on both the build and runtime environment.【F:pom.xml†L15-L35】
- **Base plugin:** [Nexo](https://polymart.org/resource/nexo.4231) `1.14.0-dev.1` or later installed on the server.【F:pom.xml†L98-L116】
- **Optional integrations:**
  - [PacketEvents](https://www.spigotmc.org/resources/packetevents.80279/) enables cosmetic totem playback and packet listeners.【F:src/main/java/zone/vao/nexoAddon/NexoAddon.java†L86-L105】【F:src/main/java/zone/vao/nexoAddon/commands/NexoAddonCommand.java†L32-L45】
  - [MythicMobs](https://mythiccraft.io/index.php?pages/mythicmobs/) unlocks Mythic entity targets for mechanics such as Infested.【F:src/main/java/zone/vao/nexoAddon/NexoAddon.java†L106-L115】

## Installation
1. Download the latest NexoAddon release from [Polymart](https://polymart.org/r/6950) or [Spigot](https://www.spigotmc.org/resources/nexoaddon.121241).
2. Place the `.jar` in your server's `plugins/` directory alongside the Nexo plugin.
3. Start the server to let NexoAddon create its configuration files and load item data from Nexo.
4. Adjust the generated configuration under `plugins/NexoAddon/` to match your server and run `/nexoaddon reload` to apply changes.【F:src/main/java/zone/vao/nexoAddon/NexoAddon.java†L145-L170】

## Commands & permissions
| Command | Permission | Description |
|---------|------------|-------------|
| `/nexoaddon reload` | `nexoaddon.admin` | Reloads configs, populators, recipes and cached item data without restarting the server.【F:src/main/java/zone/vao/nexoAddon/commands/NexoAddonCommand.java†L16-L20】【F:src/main/java/zone/vao/nexoAddon/NexoAddon.java†L145-L170】 |
| `/nexoaddon totem <player> <customModelData\|nexoID>` | `nexoaddon.admin` | Plays a totem animation for a target player using vanilla CustomModelData or a Nexo item identifier (requires PacketEvents).【F:src/main/java/zone/vao/nexoAddon/commands/NexoAddonCommand.java†L22-L45】 |

## Configuration
- `config.yml` toggles boss bars, furniture behaviour, aura refresh intervals, and localisation messages for mechanics like Big Mining, VeinMiner, AutoCatch and Bottled Experience.【F:src/main/resources/config.yml†L1-L22】
- Item YAML files loaded by Nexo control addon mechanics and components through the `Mechanics` and `Components` sections; NexoAddon reads them automatically on startup or reload.【F:src/main/java/zone/vao/nexoAddon/utils/ItemConfigUtil.java†L29-L205】
- Use the `count_shears_as_silktouch` list to allow shears to harvest specific string and chorus blocks safely.【F:src/main/resources/config.yml†L1-L7】【F:src/main/java/zone/vao/nexoAddon/events/blocks/breaks/ShearsBreak.java†L13-L40】

## World generation populators
Configure `block_populator.yml` to scatter Nexo custom blocks or furniture with per-world filters, height limits, biome restrictions, replacement targets and optional vein behaviour.【F:src/main/resources/block_populator.yml†L1-L14】【F:src/main/java/zone/vao/nexoAddon/events/chunk/FurniturePopulator.java†L19-L94】

Use `tree_populator.yml` for custom tree presets with world, biome, height and chance controls that hook into the tree populator pipeline.【F:src/main/resources/tree_populator.yml†L1-L8】【F:src/main/java/zone/vao/nexoAddon/NexoAddon.java†L182-L215】

Saplings defined as Nexo string blocks inherit natural growth timers and are automatically placed with the correct persistent data while chunks generate.【F:src/main/java/zone/vao/nexoAddon/events/chunk/SaplingPopulator.java†L18-L76】

## Recipe management
NexoAddon keeps a `recipes/` folder and can generate smithing entries that copy durability, meta, trims and enchantments from base items. Edit `recipes/smithing.yml` to add or tweak recipes, then reload the plugin.【F:src/main/resources/recipes/smithing.yml†L1-L18】【F:src/main/java/zone/vao/nexoAddon/NexoAddon.java†L145-L170】

## Building from source
1. Clone this repository and ensure the submodule containing Nexo API dependencies is available (internet access is required for Maven to resolve repositories).【F:pom.xml†L37-L120】
2. Install JDK 21 and run `mvn clean package` from the project root. The shaded jar will be created under `target/` as `NexoAddon-<version>.jar`.【F:pom.xml†L25-L73】

## Support
- 📕 [Documentation](https://nexoaddon.gitbook.io/nexoaddon-docs)
- 💬 [Discord](https://discord.com/invite/aSRYxqSjVJ)
- ❤️ [Donate a coffee](https://buymeacoffee.com/naimad)

Downloads are available on [Polymart](https://polymart.org/r/6950) and [SpigotMC](https://www.spigotmc.org/resources/nexoaddon.121241/).
