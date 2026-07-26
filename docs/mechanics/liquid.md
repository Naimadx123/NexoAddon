---
description: Liquid mechanic.
---

# Liquid

The Liquid Mechanic turns an item into a bucket that places **coloured water**. The block placed is ordinary water, so swimming, buoyancy, drowning, fog, boats and fishing all behave exactly like vanilla - only the colour changes. Entering the liquid can apply potion effects and run commands, each with its own cooldown.

The colour comes from a biome, so you need a [custom biome](../biomes/custom-biomes.md) first.

{% hint style="warning" %}
The item **MUST** use `material: WATER_BUCKET`. Vanilla then places the water itself, which is what gives you waterlogging of stairs and slabs, the "no water in the Nether" rule, and compatibility with other plugins' protection checks.
{% endhint %}

### Simple coloured water

```yaml
# Example

toxic_water_bucket:
  material: WATER_BUCKET
  itemname: <green>Toxic Sludge Bucket
  Mechanics:
    liquid:
      biome: nexoaddon:toxic_water   # Custom biome supplying the colour. Required.
```

### Effects and commands on entering

Both blocks are optional and have **separate, independent** cooldowns, so you can have a fast effect and a slow command.

```yaml
# Example

toxic_water_bucket:
  material: WATER_BUCKET
  itemname: <green>Toxic Sludge Bucket
  Mechanics:
    liquid:
      biome: nexoaddon:toxic_water
      enter:
        effects:
          cooldown: 3                # Seconds between re-applying the effects. Defaults to 0 (no cooldown).
          list:
            - type: poison           # 'minecraft:' prefix is optional
              duration: 100          # TICKS (100 = 5 seconds)
              amplifier: 0           # 0-based, like vanilla
            - type: slowness
              duration: 60
              amplifier: 1
        commands:
          cooldown: 15               # Seconds. Completely separate from the effects cooldown.
          as: console                # console (default) or player
          list:
            - "title <player> actionbar {\"text\":\"The sludge burns!\",\"color\":\"green\"}"
```

Placeholders usable in commands: `<player>`, `<uuid>`, `<world>`, `<x>`, `<y>`, `<z>`.

### Returned items

You can decide what the player gets back after emptying the bucket, scooping the liquid, or filling a bottle from it. All three are Nexo item IDs and all are optional.

```yaml
# Example

toxic_water_bucket:
  material: WATER_BUCKET
  itemname: <green>Toxic Sludge Bucket
  Mechanics:
    liquid:
      biome: nexoaddon:toxic_water
      place:
        empty_item: toxic_empty_bucket   # Given after emptying. Omit for a vanilla BUCKET.
        bucket_item: toxic_water_bucket  # Given when scooping this liquid with an empty bucket.
        bottle_item: toxic_water_bottle  # Given when filling a glass bottle from this liquid.
```

Bottles work both from a water block and from a cauldron holding the liquid, so the bottle always remembers which liquid it came from.

{% hint style="info" %}
Filling a cauldron with the bucket also colours the cauldron. Because Minecraft gives no API to change the item a cauldron hands back, a cauldron still returns a vanilla `BUCKET` - `bucket_item` only applies when scooping a water block.
{% endhint %}

### All options

```yaml
# Example

toxic_water_bucket:
  material: WATER_BUCKET
  Mechanics:
    liquid:
      enabled: true                     # Defaults to true.
      biome: nexoaddon:toxic_water      # Required.
      follow_flow: true                 # Colour water that flows out of the painted area. Defaults to true.
      place:
        enabled: true                   # false = the liquid still works, but this item cannot place it.
        radius: 0                       # Extra blocks painted around the placement. Defaults to 0. See the warning below.
        overwrite_other_liquids: false  # Allow recolouring an area another liquid is using. Defaults to false.
        empty_item: toxic_empty_bucket
        bucket_item: toxic_water_bucket
        bottle_item: toxic_water_bottle
        revert_to: minecraft:river      # Biome used when restoring, if the original is unknown.
      enter:
        effects:
          cooldown: 3
          list:
            - type: poison
              duration: 100
              amplifier: 0
              ambient: false            # Optional, defaults to false
              particles: true           # Optional, defaults to true
              icon: true                # Optional, defaults to true
        commands:
          cooldown: 15
          as: console
          list:
            - "effect give <player> glowing 5"
```

## Placing liquids next to each other

{% hint style="warning" %}
Minecraft stores biomes per **4x4x4 block cell**, not per block. Two liquids inside the same cell cannot have different colours, even if there are solid blocks between them.
{% endhint %}

Cells are aligned to coordinates divisible by 4, so what matters is not the distance but whether a cell boundary falls between the two liquids:

| Placement | Result                                         |
| --- |------------------------------------------------|
| Water at `x=3` and `x=4` | Different cells - two colours, blocks touching |
| Water at `x=0` and `x=3` | Same cell - one colour, despite a 2 block gap  |

If you build to that grid, adjacent colours work. `place.radius` above `0` paints neighbouring cells as well, so leave it at `0` unless you know you want that.

By default a liquid will not recolour a cell that another liquid is actively using - the first one keeps its colour. Set `overwrite_other_liquids: true` to allow stealing it.

## Removing liquids

When the last water in a cell is gone, the biome is restored to whatever it was before the liquid was placed. This happens automatically when you break the water, place a block into it, scoop it with a bucket, or empty a cauldron.

Removal done by other means - WorldEdit, `/setblock`, `/fill` - fires no event, so the colour stays behind. Clean those up with:

```yaml
/nexoaddon liquidclean [radius] [biome]
```

* `radius` - blocks around you to scan. Defaults to `16`, maximum `64`.
* `biome` - optional. Forces this biome instead of the remembered original, which is what you need for areas coloured before the original was ever recorded.

The command reports how many cells it restored, how many still contain water, and how many had no known original biome.

{% hint style="info" %}
Cells that still contain water are skipped on purpose: the biome covers the whole cell, so restoring it would break the colour of the water that is still there.
{% endhint %}

## Global settings

These live in `config.yml` and apply to every liquid.

```yaml
liquid:
  refresh_chunks: true          # Resend chunks so clients see a new colour without relogging.
  refresh_cooldown_ticks: 20    # Per-chunk debounce for that resend.
  max_place_radius: 4           # Hard cap on place.radius.
```
