---
description: Infested mechanic.
---

# Infested

The Infested Mechanic lets you add a list of entities that should spawn upon breaking a custom block

{% hint style="info" %}
`entities` option is required!
{% endhint %}

### A custom block infested with Zombies and Slimes

It will select one of them, and 100% spawn it, it will also spawn particles

```yaml
# Example

Mechanics:
  custom_block:
    type: NOTEBLOCK
    infested:
      entities:
        - ZOMBIE
        - SLIME
      probability: 1.0     # The chance for an entity to spawn. optional, defaults to 1.0
      selector: random     # entity selection method (all|random) all makes all entities spawn, random makes 1 of them spawn, optional, defaults to all.
      particles: true      # If breaking the block should spawn particles. optional, defaults to false
      drop-loot: false     # If the entity spawns, the loot won't drop, defaults to true
```

### A custom block infested with SkeletalKnights and Wither Skeletons

It has an 80% chance to spawn a wither skeleton and a skeletal knight when broken

```yaml
# Example

Mechanics:
  custom_block:
    type: NOTEBLOCK
    infested:
      entities:
        - WITHER_SKELETON
      mythic-mobs:
        - SkeletalKnight
      probability: 0.8     # The chance for an entity to spawn. optional, defaults to 1.0
      particles: false      # If breaking the block should spawn particles. optional, defaults to false
```

<sup><sub>Akis was here ;)</sub></sup>