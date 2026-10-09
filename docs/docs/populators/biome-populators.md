---
description: Populate your worlds with custom or vanilla biomes!
---

# Biome Populators

Biome Populators change the biome of newly generated chunks. Use them to place a [custom biome](../biomes/custom-biomes.md) in your world, or to replace one vanilla biome with another.

Configurations live in `plugins/NexoAddon/populators/biome_populator.yml`. You can also split them into `.yml` files inside `plugins/NexoAddon/populators/biomes/`. Each top-level key is a rule ID of your choice.

{% hint style="info" %}
Custom biomes must already be registered. Enable the biome in `custom_biomes/`, restart the server, and check `/nexoaddon biomes` before using its ID here.
{% endhint %}

### Replacing plains with a custom biome

```yaml
custom_plains:
  enabled: true
  biome: nexoaddon:customx
  worlds: [ world ]
  minY: -64
  maxY: 319
  biomes: [ minecraft:plains ]
  chance: 1.0
```

This rule replaces plains biome cells in new chunks of `world` with `nexoaddon:customx`, between the configured heights. It changes the biome and its colours, weather and ambience; it does not rebuild the terrain.

### Available options

| Key | Description |
| --- | --- |
| `enabled` | Whether to load the rule. Defaults to `true`. The generated example starts disabled. |
| `biome` | Target biome ID. Required. Accepts registered custom and vanilla biomes. |
| `worlds` | World names where the rule applies. Required. Use `[ all ]` for every world. |
| `minY`, `maxY` | Inclusive height bounds. Omit them to cover the world's full height. |
| `biomes` | Source biome filter. An empty list or omitted field allows all registered biomes. |
| `chance` | Probability of applying the rule to each chunk, from `0.0` to `1.0`. Defaults to `1.0`. |

Unknown target biomes, empty world lists, filters matching no registered biomes, reversed height bounds and invalid chance values cause the rule to be skipped with a warning in the server log.

### Biome filters

Vanilla biomes accept names such as `PLAINS`, `plains` or `minecraft:plains`. Custom biomes require the full namespaced ID, for example `nexoaddon:customx`.

```yaml
biomes: [ minecraft:plains, minecraft:forest, nexoaddon:customx ]
```

Prefix names with `!` to exclude them instead:

```yaml
biomes: [ '!minecraft:desert', '!nexoaddon:toxic_water' ]
```

If any entry starts with `!`, the whole list is treated as an exclusion list. Keep inclusion and exclusion filters separate.

{% hint style="warning" %}
Biomes are stored per **4x4x4 block cell**. Height bounds affect every cell they intersect, so a change may extend beyond `minY` or `maxY` within that cell.
{% endhint %}

### Using custom biomes with block populators

Biome Populators run before [Block Populators](block-populators.md), so block placement can filter by the newly assigned biome:

```yaml
ruby_ore:
  iterations: 10
  worlds: [ world ]
  minY: 0
  maxY: 20
  biomes: [ nexoaddon:customx ]
  chance: 0.4
  replace: [ STONE, DEEPSLATE ]
  vein_size: 5
  cluster_chance: 1
```

Put this block rule in `populators/block_populator.yml` or a `.yml` file inside `populators/blocks/`.

### Reloading and existing chunks

`/nexoaddon reload` reloads populator rules for future chunk generation. Adding or changing the custom biome itself still requires a full server restart.

Existing chunks keep their biomes. To change a selected area, use [`/minecraft:fillbiome`](../biomes/custom-biomes.md#changing-the-biome-of-an-existing-area). The experimental [`/nexoaddon repopulate`](../commands/commands.md#repopulate) command can rerun populators on loaded chunks, including block and furniture placement.
