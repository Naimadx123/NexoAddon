---
description: Define your own biomes in YAML.
---

# Custom Biomes

NexoAddon can create biomes for you. Drop a YAML file into `plugins/NexoAddon/custom_biomes/`, restart, and the biome is registered -
no datapack to write, no world folder to touch.

Custom biomes exist mainly to give the [Liquid mechanic](../mechanics/liquid.md) its colour, since water takes its colour from the biome, but any biome field can be set.

{% hint style="warning" %}
Adding or changing a biome needs a **full server restart**. Biome registries are frozen once the server is running, so `/nexoaddon reload` can only regenerate the files and tell you a restart is due.
{% endhint %}

### A coloured water biome

```yaml
# plugins/NexoAddon/custom_biomes/waters.yml

toxic_water:
  enabled: true                  # Defaults to FALSE. A biome becomes part of your world data, so it is opt-in.
  water_color: "#3bd44b"         # Always quote colours. See the warning below.
```

That is the whole minimum. Everything else is inherited from a neutral base biome.

### A fuller example

```yaml
# plugins/NexoAddon/custom_biomes/waters.yml

red_water:
  enabled: true
  title: "Molten Water"          # Shown by /nexoaddon biomes. Cosmetic.
  inherit: minecraft:the_void    # Base biome copied from the server. Defaults to minecraft:the_void.
  water_color: "#8b0000"
  water_fog_color: "#5a0000"     # Fog colour while submerged
  fog_color: "#3a1010"
  sky_color: "#4a1414"
  temperature: 2.0
  downfall: 0.0
  has_precipitation: false
```

{% hint style="danger" %}
`water_color: #8b0000` without quotes is a **YAML comment**. The value becomes empty and the biome is skipped. Always write `water_color: "#8b0000"`.
{% endhint %}

### Available keys

| Key | Description                                             |
| --- |---------------------------------------------------------|
| `enabled` | Whether to generate this biome. Defaults to `false`.    |
| `inherit` | Biome to copy as the base, e.g. `minecraft:lush_caves`. |
| `title` | Label shown by `/nexoaddon biomes`.                     |
| `water_color` | Colour of the water.                                    |
| `water_fog_color` | Fog colour while underwater.                            |
| `sky_color` | Sky colour.                                             |
| `fog_color` | Distance fog colour.                                    |
| `grass_color`, `foliage_color`, `dry_foliage_color` | Vegetation tints.                                       |
| `grass_color_modifier` | `none`, `dark_forest` or `swamp`.                       |
| `temperature` | Affects snow and ice. See the warning below.            |
| `downfall` | Humidity, `0.0`–`1.0`.                                  |
| `has_precipitation` | `true` or `false`.                                      |
| `temperature_modifier` | `none` or `frozen`.                                     |
| `creature_spawn_probability` | Spawn density for creatures.                            |
| `raw` | Any biome JSON at all -<br/> see below.                      |

Colours accept `"#rrggbb"`, `"0xRRGGBB"`, `"rrggbb"` or a plain integer.

{% hint style="warning" %}
Do not give a water biome a low `temperature`. Below roughly `0.15` the water freezes into ice and the colour stops mattering.
{% endhint %}

### The `raw` escape hatch

Anything not covered by the keys above can be written as raw biome JSON. It is merged **last**, so it overrides everything else. Objects merge with the inherited biome, lists replace it entirely, and an empty object clears a section.

```yaml
# Example

haunted_water:
  enabled: true
  inherit: minecraft:lush_caves   # Borrow that biome's ambience wholesale
  water_color: "#2b2b6e"
  raw:
    spawners: {}                  # ...but without any of its mobs
```

### Namespaces

A top-level key can be a bare name or `namespace:path`. A bare name uses the namespace from `config.yml`, which is `nexoaddon` by default, so `toxic_water` becomes `nexoaddon:toxic_water`. Only lowercase `a-z 0-9 . _ -` are allowed, and the `minecraft` namespace is rejected so you cannot overwrite vanilla biomes.

### Checking what registered

```yaml
/nexoaddon biomes
```

Lists every configured biome as `id · enabled · registered`, plus whether the files have changed since startup. If a biome shows `needs restart`, the definition is on disk but not yet in the world.

Problems with a file -
bad YAML, an unparseable colour, an unknown `inherit` -
are reported in the server log at startup, naming the file and line. If **any** biome is invalid, nothing is written at all and the previously generated biomes are left untouched.

## Global settings

```yaml
custom_biomes:
  enabled: true                       # Generate and register biomes. Read at startup.
  namespace: nexoaddon                # Default namespace for IDs written without one.
  default_inherit: minecraft:the_void # Base biome used when 'inherit' is not set.
  prune_orphans: false                # Delete generated biomes whose YAML is gone. See the warning below.
```

If the server ever fails to start because of a biome, you can disable the whole feature without editing files by adding `-Dnexoaddon.biomes=off` to the startup flags.

{% hint style="danger" %}
Never delete a biome that has already been placed in a world. Chunks store biomes by name, so removing one makes those chunks fail to load. Set `enabled: false` and replace the water first. `prune_orphans` is off by default for exactly this reason.
{% endhint %}

## Limitations

These come from how Minecraft stores biomes and cannot be worked around by a plugin.

* **Biomes are stored per 4x4x4 block cell.** Two biomes cannot differ inside one cell. See [Liquid](../mechanics/liquid.md) for how to build around this.
* **The client blends biome colours.** With the default "Biome Blend: 5x5" setting a colour bleeds several blocks into its surroundings. Players should lower it.
* **A biome changes more than colour.** Grass and foliage tints, mob spawns, snow and ice, ambient sounds and weather all follow the biome. Inheriting a biome close to the surroundings, and changing only the colours, keeps side effects small.
* **Changes need a restart**, as noted at the top.
