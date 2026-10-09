---
description: The VeinMiner mechanic
---

# VeinMiner

The VeinMiner mechanic allows players to break multiple whitelisted blocks efficiently.

`distance` is required. `limit` defaults to `10`, `same_material` to `true` and `toggleable` to `false`. Add at least one valid material or Nexo block ID to `whitelist`; an empty whitelist matches no blocks.

`distance` is the maximum distance in blocks from the first broken block. Connected neighbours, including diagonals, are searched within that distance. `limit` includes the first block, so `limit: 5` can break up to four additional blocks.

### Simple

```yaml
# Example
coal_pickaxe:
  material: NETHERITE_PICKAXE
  Mechanics:
    veinminer:
      limit: 5
      distance: 5
      whitelist:
        - COAL_ORE
        - DEEPSLATE_COAL_ORE
        - nether_coal_ore
```

### Toggleable

Right-click while holding the tool in the main hand to toggle VeinMiner. Interactive blocks, such as chests, keep their normal behaviour.

```yaml
# Example
coal_pickaxe:
  material: NETHERITE_PICKAXE
  Mechanics:
    veinminer:
      limit: 10
      distance: 2
      toggleable: true # whether it should be toggleable
      same_material: false # if true, it will only break the same block as broken
      whitelist: # the Materials and NexoIDs that can be broken
        - COAL_ORE
        - DEEPSLATE_COAL_ORE
        - nether_coal_ore
```
