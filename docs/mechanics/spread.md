---
description: Spread custom_block mechanic.
---

# Spread

The Spread Mechanic lets a custom block slowly take over the blocks around it, the way grass creeps across dirt or moss spreads over stone. Every so often the block picks one valid neighbour and converts it.

{% hint style="warning" %}
`spread` goes **inside** `custom_block`, not next to it. If you indent it as a sibling of `custom_block` the mechanic is silently ignored and nothing happens.
{% endhint %}

### Moss creeping over stone

```yaml
# Example

mossy_block:
  material: PAPER
  Pack:
    parent_model: block/cube_all
    texture: block/mossy_block.png
  Mechanics:
    custom_block:
      type: NOTEBLOCK
      model: mossy_block
      spread:
        interval: 100      # Ticks between spread attempts. Defaults to 100 (5 seconds).
        chance: 0.15       # Chance each attempt actually spreads. Defaults to 0.15.
        radius: 1          # How far around the block to look. Defaults to 1.
        replace:           # Which blocks may be taken over. Required.
        - STONE
        - COBBLESTONE
```

### Conditions

A neighbour is only eligible if every condition passes. Use these to stop a block spreading into caves, under other blocks, or outside its intended climate.

```yaml
# Example

mossy_block:
  material: PAPER
  Mechanics:
    custom_block:
      type: NOTEBLOCK
      model: mossy_block
      spread:
        interval: 60
        chance: 0.25
        radius: 2
        replace:
        - STONE
        requires_air_above: true   # Only convert blocks with air directly above. Defaults to false.
        max_nearby: 12             # Stop once this many result blocks are already in range. Defaults to 64.
        conditions:
          light_min: 9             # Lowest light level allowed. Defaults to 0.
          light_max: 15            # Highest light level allowed. Defaults to 15.
          biome:                   # Optional whitelist. Omit to allow every biome.
          - swamp
          - jungle
```

{% hint style="info" %}
Biome names are written without the `minecraft:` prefix, for example `swamp`, `jungle`, `plains`. Custom biomes work too, using just the path part of their ID.
{% endhint %}

### Spreading into a different block

By default a block spreads into copies of itself. Set `result` to another Nexo block ID to convert neighbours into something else instead.

```yaml
# Example

infection_source:
  material: PAPER
  Mechanics:
    custom_block:
      type: NOTEBLOCK
      model: infection_source
      spread:
        interval: 40
        chance: 0.5
        radius: 2
        result: infected_stone    # Nexo block ID, or 'self' (default) to spread copies of this block.
        replace:
        - STONE
```

The converted block starts spreading on its own, so with `result` pointing at a block that also has a Spread mechanic you can build chains.

### All options

```yaml
# Example

mossy_block:
  material: PAPER
  Mechanics:
    custom_block:
      type: NOTEBLOCK
      model: mossy_block
      spread:
        enabled: true              # Defaults to true.
        interval: 100              # Ticks between attempts. Minimum 1.
        chance: 0.15               # 0.0 to 1.0.
        radius: 1                  # Minimum 1.
        result: self               # 'self' or a Nexo block ID.
        replace:                   # Required. Vanilla materials only.
        - STONE
        - COBBLESTONE
        requires_air_above: false
        max_nearby: 64
        conditions:
          light_min: 0
          light_max: 15
          biome:
          - swamp
```

## How a spread attempt works

Every `interval` ticks the block rolls `chance`. If it passes, it scans the cube of `radius` blocks around itself and:

1. Counts blocks that are already the result block. If that count reaches `max_nearby`, the attempt stops and nothing is converted.
2. Collects every block whose material is listed in `replace` and which passes all conditions.
3. Picks one of those at random and converts it.

So `max_nearby` is measured inside the scanned cube, not across the world. It is what stops a patch from filling in completely and keeps the edges growing instead.

{% hint style="info" %}
`replace` accepts **vanilla materials only**, not Nexo block IDs. If none of the entries are valid materials the whole mechanic is skipped and a warning naming the bad values appears in the log.
{% endhint %}

## Performance

Spreading blocks are driven by one shared scheduler with a server-wide budget, so the cost does not grow with the number of blocks. Once more blocks are due than the budget allows, the extras simply wait for the next tick, oldest first.

```yaml
spread:
  max_per_tick: 40    # Maximum spread attempts processed server-wide per tick.
```

Lower it if you want spreading to weigh less on the server, raise it if large areas feel too slow. Blocks are also given a random starting offset, so a batch placed in the same tick does not all fire together.

{% hint style="info" %}
`interval` counts server ticks, so lowering the server tick rate with `/tick rate` slows spreading down as well.
{% endhint %}

Spreading survives restarts and chunk unloads. Each spreading block is marked in the chunk data and picked up again when the chunk loads, so nothing needs to be replaced by hand.
