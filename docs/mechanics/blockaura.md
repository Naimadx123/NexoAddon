---
description: BlockAura mechanic.
---

# BlockAura

The Block Aura Mechanic allows you to add particle effects to your block like the Campfire or the Cherry Blossom


### Example of a campfire effect:

```yaml
Mechanics:
  custom_block:
    type: NOTEBLOCK      # Works with all block types
    block_aura:
      particle: campfire_cosy_smoke
      xOffset: ..1       # selects a random number between 0 and 1, defaults to 0.5
      yOffset: 0.85      # always 0.85 as there is no range defined, defaults to 0.5
      zOffset: 0..       # selects a random number between 0 and 1, defaults to 0.5
      amount: 0          # amount of particles, defaults to 10
      deltaX: 0          # how much it should spread on the X axis, defaults to 0.6
      deltaY: 0.075      # how much it should spread on the Y axis, defaults to 0.6
      deltaZ: 0          # how much it should spread on the Z axis, defaults to 0.6
      speed: 1           # how fast the particles should move, defaults to 0.05
      force: false       # makes them render higher distances and more, defaults to true
```

### Cherry Blossom recreation

```yaml
Mechanics:
  custom_block:
    block_aura:
      particle: spore_blossom_air
      xOffset: 0.5
      yOffset: 0.5
      zOffset: 0.5
      amount: 100
      deltaX: 4
      deltaY: 4
      deltaZ: 4
      speed: 0.05
      force: true
```