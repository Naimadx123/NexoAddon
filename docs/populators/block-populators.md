---
description: Populate your worlds with CustomBlocks or Furnitures!
---

# Block Populators

Configurations live in `plugins/NexoAddon/populators/block_populator.yml`, or in `.yml` files inside `plugins/NexoAddon/populators/blocks/`. Each top-level key is the Nexo item ID or vanilla block material to place. `/nexoaddon reload` reloads these rules for future generation.

Use [Biome Populators](biome-populators.md) to change a chunk's biome. They run before block populators, so block rules can match the newly assigned biome.

{% hint style="info" %}
Supports **Furnitures**, **Noteblocks**, **Stringblocks, Chorusblocks** and **Vanilla Materials**
{% endhint %}

### Flowers Populator Example

With Block Populators you can simply populate your world with for eg. flowers:

```yaml
# Example

sword_flower:                             # nexo item id
  iterations: 5
  worlds: [ world ]
  maxY: 120
  minY: 20
  biomes: [ PLAINS, BIRCH_FOREST ]
  chance: 1
  air_only: true                          # Replace Air only
  place_on: [ GRASS_BLOCK ]
```

### Ore Populator Example

Create custom ores in veins!

```yaml
# Example

ruby_ore:
  iterations: 10
  worlds: [ world ]
  maxY: 20
  minY: 0
  biomes: [  ]
  chance: 0.4
  replace: [ STONE, DEEPSLATE ]
  vein_size: 5
  cluster_chance: 1
```

### Place below - Icicles

```yaml
# Example made by Phil (discord: voogle)

icicle:
  iterations: 50
  worlds: [ world ]
  maxY: 150
  minY: 61
  biomes: [ SNOWY_TAIGA, SNOWY_BEACH, SNOWY_SLOPES, SNOWY_PLAINS ]
  chance: 1
  air_only: true                                                     # Replace Air only
  place_below: [ SPRUCE_LEAVES, OAK_LEAVES ]
```

<table data-card-size="large" data-view="cards"><thead><tr><th></th><th></th><th></th><th data-hidden data-card-cover data-type="files"></th></tr></thead><tbody><tr><td></td><td></td><td></td><td><a href="../.gitbook/assets/obraz_2025-01-04_232133289.png">obraz_2025-01-04_232133289.png</a></td></tr><tr><td></td><td></td><td></td><td><a href="../.gitbook/assets/obraz_2025-01-04_231941587.png">obraz_2025-01-04_231941587.png</a></td></tr></tbody></table>

### Biome filters

An empty `biomes` list allows all registered biomes. Vanilla names accept `PLAINS`, `plains` and `minecraft:plains`. For custom biomes, use the full ID:

```yaml
biomes: [ minecraft:plains, nexoaddon:customx ]
```

Use `!` to turn the list into an exclusion filter:

```yaml
biomes: [ '!minecraft:desert', '!nexoaddon:toxic_water' ]
```

If any entry begins with `!`, every name in the list is treated as excluded. A filter matching no registered biomes causes the populator to be skipped with a warning.

### Random counts and existing chunks

`iterations` and `vein_size` can be fixed integers or quoted ranges, for example `iterations: "5-10"` and `vein_size: "3-6"`. Use `worlds: [ all ]` to apply a rule to every world.

Populators normally affect new chunks. The experimental [`/nexoaddon repopulate`](../commands/commands.md#repopulate) command reruns them on loaded chunks and can place additional blocks or furniture there.

### Available options

| Key | Description |
| --- | --- |
| `iterations` | Target number of placements per chunk. Defaults to `50`. Accepts an integer or a quoted range. Placement conditions can reduce the actual count. |
| `worlds` | Worlds where the rule applies. Use `[ all ]` for every world. A rule without a matching world is skipped. |
| `minY`, `maxY` | Height range used for random placement. Both default to `0`; set them for the intended area. |
| `biomes` | Source biome filter. Defaults to all registered biomes. Supports namespaced IDs and exclusion lists. |
| `chance` | Chance of running the rule in a chunk, from `0.0` to `1.0`. Defaults to `0.1`. |
| `replace` | Vanilla materials that may be replaced by the configured block. Defaults to an empty list. |
| `place_on` | Vanilla materials the block or furniture can be placed above. Defaults to an empty list. |
| `place_below` | Vanilla materials the block or furniture can be placed below. Defaults to an empty list. |
| `air_only` | Require air at the placement position for `place_on` and `place_below`. Defaults to `false`. |
| `vein_size` | Maximum blocks in a vein. Defaults to `0`, which disables veins. Accepts an integer or a quoted range. |
| `cluster_chance` | Chance of generating a vein at a selected position. Defaults to `0.0`; use it with a positive `vein_size`. |

For block placement, a valid `replace` match is checked first, followed by `place_on` and then `place_below`.

### Replacing every matching block

A negative integer `iterations`, such as `-1`, makes a block rule scan the chunk and replace every material listed in `replace`, subject to the biome filter. This mode does not use the random-placement `minY`, `maxY`, `chance`, `place_on`, `place_below` or vein settings. It does not apply to furniture.
