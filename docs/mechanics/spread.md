---
description: Spread custom_block mechanic.
---

# Spread

The Spread Mechanic lets a custom block slowly take over the blocks around it, the way grass creeps across dirt or moss spreads over stone. Every so often the block picks one valid neighbour and converts it.

With [rules](spread.md#rules) the same block can convert different neighbours into different things - stone into corrupted stone, coal ore into corrupted coal ore, and everything else into whatever you pick.

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

### Rules

A single `replace` list turns every neighbour into the same thing. `rules` lets one block convert different neighbours into different results, so a corruption can turn stone into corrupted stone while turning coal ore into corrupted coal ore.

Each rule is a `replace` list plus the `result` those blocks become.

```yaml
# Example

corruption:
  material: PAPER
  Mechanics:
    custom_block:
      type: NOTEBLOCK
      model: corruption
      spread:
        interval: 40
        chance: 0.4
        radius: 1
        rules:
        - replace: [ 'minecraft:stone' ]
          result: self                    # stone becomes another corruption block
        - replace: [ 'minecraft:coal_ore' ]
          result: corrupted_coal_ore      # but coal ore becomes its own corrupted version
        - replace: [ 'minecraft:iron_ore', 'minecraft:copper_ore' ]
          result: corrupted_metal_ore
```

Rules are checked **in order** and the first one that matches wins. A block is never tested against later rules, so put your specific rules above the broad ones.

A rule with no `result` falls back to the `result` set on `spread` itself, which is `self` unless you change it.

### Replacing everything else

`'*'` inside a rule matches every block. Put it last and it becomes the catch-all for anything the rules above did not claim.

```yaml
# Example

corruption:
  material: PAPER
  Mechanics:
    custom_block:
      type: NOTEBLOCK
      model: corruption
      spread:
        interval: 40
        chance: 0.4
        radius: 1
        rules:
        - replace: [ 'minecraft:coal_ore' ]
          result: corrupted_coal_ore
        - replace: [ '*' ]                # everything else
          result: self
```

`all` and `any` are accepted as aliases of `'*'`.

{% hint style="warning" %}
`'*'` also matches **air**, so a wildcard rule grows outwards into open space rather than only creeping over solid ground. Keep `chance` low, or add a `max_nearby` limit, unless that is what you want.
{% endhint %}

Bedrock, barriers and portal blocks are never matched by `'*'`, so a wildcard rule cannot eat the bottom of the world. List them explicitly if you really need them replaced.

### Block tags

A `replace` entry that is not a vanilla material is looked up as a block tag, so you can match a whole family of blocks without listing each one.

```yaml
# Example

corruption:
  material: PAPER
  Mechanics:
    custom_block:
      type: NOTEBLOCK
      model: corruption
      spread:
        interval: 60
        chance: 0.3
        rules:
        - replace: [ 'minecraft:beds' ]        # every bed colour
          result: corrupted_bed
        - replace: [ 'minecraft:wool' ]        # every wool colour
          result: corrupted_wool
        - replace: [ 'minecraft:logs' ]
          result: corrupted_log
```

Materials are tried first, then tags. Prefix an entry with `#` to force a tag lookup and skip the material check, for example `'#minecraft:planks'`. Entries without a namespace default to `minecraft:`.

{% hint style="info" %}
Any vanilla block tag works: `minecraft:base_stone_overworld`, `minecraft:dirt`, `minecraft:leaves`, `minecraft:flowers`, `minecraft:coal_ores`, and so on. Datapack tags work too. An entry that is neither a material nor a known tag is skipped with a warning naming the item and the bad value.
{% endhint %}

### Converting several blocks at once

`mode` controls how many neighbours a successful attempt converts.

```yaml
spread:
  mode: SINGLE     # SINGLE (default) converts one random neighbour, MULTI converts all of them.
```

`SINGLE` is the organic, creeping look. `MULTI` converts every eligible neighbour in the scanned cube in the same attempt, which spreads in expanding shells instead.

{% hint style="danger" %}
`MULTI` with `radius: 2` touches up to 124 blocks per attempt, and each one starts spreading on its own. Combined with `'*'` it will consume a region very quickly. Raise `interval` and lower `chance` before using it.
{% endhint %}

### Land claims

Spreading has no player behind it, so region and claim plugins cannot check permissions the usual way. Turning `protection` on makes NexoAddon fire a vanilla `BlockSpreadEvent` before each conversion, which is the same event WorldGuard, Lands, Towny, GriefPrevention and friends already use to stop grass and mycelium from creeping into claims.

```yaml
spread:
  protection:
    enabled: true          # Defaults to false.
    respect_claims: true   # Let claim and region plugins veto a conversion. Defaults to true.
```

If your protection plugin blocks vanilla block spreading inside claims, it will block this too. If it does not, this option has no effect.

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
        mode: SINGLE               # SINGLE or MULTI. Defaults to SINGLE.
        result: self               # Fallback result for rules that omit one. 'self' or a Nexo block ID.
        rules:                     # Checked in order, first match wins.
        - replace: [ 'minecraft:coal_ore' ]
          result: corrupted_coal_ore
        - replace: [ 'minecraft:beds', 'minecraft:stone', '*' ]
          result: self
        replace:                   # Legacy flat list. Appended as the last rule.
        - STONE
        - COBBLESTONE
        requires_air_above: false
        max_nearby: 64             # Use -1 or 0 for no limit.
        protection:
          enabled: false
          respect_claims: true
        conditions:
          light_min: 0
          light_max: 15
          biome:
          - swamp
```

At least one of `rules` or `replace` must produce a valid entry, otherwise the mechanic is skipped with a warning.

### Upgrading from the old syntax

`rules` is additive. A config written before rules existed keeps working exactly as it did:

```yaml
spread:
  result: infected_stone
  replace:
  - STONE
  - COBBLESTONE
```

is treated as a single rule. If you use both, the flat `replace` list is appended **after** everything in `rules`, so it acts as the fallback.

## How a spread attempt works

Every `interval` ticks the block rolls `chance`. If it passes, it scans the cube of `radius` blocks around itself and:

1. Counts blocks that are already one of this mechanic's results. If that count reaches `max_nearby`, the attempt stops and nothing is converted. Set `max_nearby` to `-1` or `0` to remove the limit.
2. Matches every other block against the rules, in order, and collects the ones that match a rule and pass all conditions. Each one remembers the `result` of the rule that matched it.
3. Converts one of them at random, or all of them when `mode` is `MULTI`.

So `max_nearby` is measured inside the scanned cube, not across the world. It is what stops a patch from filling in completely and keeps the edges growing instead.

Blocks that are already a result of this mechanic are never re-converted, which is what keeps a `'*'` rule from endlessly replacing its own output.

{% hint style="info" %}
`replace` entries are **vanilla materials, block tags, or `'*'`** - never Nexo block IDs. `result` is the opposite: a Nexo block ID, or `self`. If a rule ends up with no valid entries at all it is dropped, and if no rule survives the whole mechanic is skipped, with a warning naming the item and the bad values.
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
