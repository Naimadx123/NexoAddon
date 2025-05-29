---
description: The BigMining mechanic
---

# VeinMiner

The VeinMiner mechanic allows players to break multiple whitelisted blocks efficiently.

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

```yaml
# Example
coal_pickaxe:
  material: NETHERITE_PICKAXE
  Mechanics:
    veinminer:
      limit: 10
      distance: 2 # How far it should check in each direction
      toggleable: true # whether it should be toggleable
      same_material: false # if true, it will only break the same block as broken
      whitelist: # the Materials and NexoIDs that can be broken
        - COAL_ORE
        - DEEPSLATE_COAL_ORE
        - nether_coal_ore
```
