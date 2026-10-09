---
description: Commands and permissions for NexoAddon.
---

# Commands

The administrative commands below require `nexoaddon.admin`. Opening a custom crafting station uses the separate `nexoaddon.customcrafting` permission.

### Reload

```text
/nexoaddon reload
```

Reloads the configuration, item components and mechanics, recipes and populator rules. Custom biome files are regenerated, but adding or changing registered biomes still needs a full server restart.

### Biomes

```text
/nexoaddon biomes
```

Lists configured [custom biomes](../biomes/custom-biomes.md) with their registration status and source file. It also reports changes since startup or a rejected generated datapack.

### Liquid cleanup

```text
/nexoaddon liquidclean [radius] [biome]
```

Restores dry cells using a configured [Liquid](../mechanics/liquid.md#removing-liquids) biome. Run it in-game. The radius defaults to `16` blocks and is clamped to `1`–`64`. The optional registered biome overrides the remembered original.

Cells containing water, waterlogged blocks or water-filled cauldrons are skipped. Only loaded chunks are scanned.

### Repopulate

```text
/nexoaddon repopulate world true
/nexoaddon repopulate #all true
```

Reruns the configured biome, block and furniture populators on **loaded, generated chunks** in the chosen world. `#all` selects every loaded world. It does not load or scan the world's other chunks.

{% hint style="warning" %}
Repopulation is experimental and can change biomes or place additional blocks and furniture in existing areas. Back up the world before using it. The final `true` argument acknowledges the command's experimental-feature notice; without it, nothing is scheduled.
{% endhint %}

For a biome change in a selected area, use [`/minecraft:fillbiome`](../biomes/custom-biomes.md#changing-the-biome-of-an-existing-area).

### Totem animation

```text
/nexoaddon totem <player> <customModelData|nexoID> [sound]
```

Plays a custom totem animation for an online player. Requires PacketEvents. See [Totem Animation](totemcommand.md) for item setup, sounds and the optional animation delay.

### Custom crafting

```text
/nexoaddon customcrafting <station_id> <player>
```

Opens a [CustomCrafting](../mechanics/customcrafting.md#opening-a-station-with-a-command) menu for an online player. `station_id` is the value of `Mechanics.custom_crafting.station_id` on a configured item. For example:

```text
/nexoaddon customcrafting ruby_station PlayerName
```

Requires `nexoaddon.customcrafting` on the command sender, with access granted to operators by default. `nexoaddon.admin` is not required. The target player does not need the command permission.

Run it in-game or from the console. Tab completion lists configured stations and online players. An unknown station or offline player prevents the menu from opening.
