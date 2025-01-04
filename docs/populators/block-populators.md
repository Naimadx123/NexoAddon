---
description: Populate your worlds with CustomBlocks or Furnitures!
---

# Block Populators



{% hint style="info" %}
Supports **Furnitures**, **Noteblocks**, **Stringblocks** and **Vanilla Materials**
{% endhint %}

### Flowers Populator Example

With Block Populators you can simply populate your world with for eg. flowers:

```yaml
# Example
sword_flower:                       # nexo item id
  iterations: 5
  worlds: [ world ]
  maxY: 120
  minY: 20
  biomes: [ PLAINS, BIRCH_FOREST ]
  chance: 1
  place_on: [ GRASS_BLOCK ]
```

### Ore Populator Example

Create custom ores in veins!

```yaml
ruby_ore:
  iterations: 10
  worlds: [ world ]
  maxY: 20
  minY: 0
  biomes: [  ]
  chance: 0.4
  replace: [ STONE,  ]
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

***

```yaml
# custom_block:            # The unique ID of a Nexo custom block or Nexo furniture item to be placed.
#   iterations: 50         # Number of placement attempts per chunk. Higher numbers increase the chance of generating the block.
#   worlds: [ world ]      # List of worlds where the custom block can spawn. Example: [ world, world_nether, world_the_end ].
#   maxY: 50               # Maximum Y-level where the custom block can spawn within the chunk.
#   minY: 20               # Minimum Y-level where the custom block can spawn within the chunk.
#   biomes: [ SWAMP ]      # List of biomes where the custom block can spawn. Use an empty list `[]` for all biomes.
#   chance: 0.2            # Probability of the custom block spawning in the chunk. Value ranges from 0.0 (never) to 1.0 (always).
#   replace:               # List of block types that the custom block can replace. Leave empty if not replacing any blocks.
#     - STONE              # Example: Replaces STONE blocks during generation.
#   place_on: [ STONE ]    # Optional: List of block types that the custom block can be placed on. Overrides `replace` if provided.
#   place_below: [ STONE ] # Optional: List of block types that the custom block can be placed below. Overrides `replace` if provided.
#   air_only: false        # Optional: Replace Air only (for place_on and place_below).
#   vein_size: 5           # Optional: Maximum number of blocks in a vein. Set to 0 for single block placement. Default: 0.
#   cluster_chance: 0.8    # Optional: Probability of cluster (vein) formation within the chunk. Defaults to 0.0 for no clustering.

```
