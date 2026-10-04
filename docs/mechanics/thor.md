---
description: Thor mechanic.
---

# Thor

The Thor Mechanic lets an item call lightning at the block the player is looking at. Right-click while holding the item in the main hand to strike a target up to 50 blocks away.

### A lightning hammer

```yaml
storm_hammer:
  material: IRON_AXE
  itemname: <aqua>Storm Hammer
  Mechanics:
    thor:
      lightning_bolts_amount: 1
      random_location_variation: 0
      delay: 3000
```

This hammer strikes the targeted block once, with a three-second cooldown. A target block must be in range, and the player must be allowed to interact at its location by the protection plugin.

{% hint style="warning" %}
Thor creates real lightning, so it can damage entities and start fires according to the world's rules. Right-clicking an interactive block, such as a chest, normally uses that block instead of calling lightning.
{% endhint %}

### Calling several bolts

```yaml
storm_hammer:
  material: IRON_AXE
  itemname: <aqua>Storm Hammer
  Mechanics:
    thor:
      lightning_bolts_amount: 5
      random_location_variation: 4
      delay: 5000
```

All five bolts strike in the same activation. Each gets a random horizontal offset of up to two blocks from the target on both the X and Z axes.

### Available options

| Key | Description |
| --- | --- |
| `lightning_bolts_amount` | Number of bolts per activation. Defaults to `1`, minimum `1`. |
| `random_location_variation` | Width of the random area on each horizontal axis, in blocks. Defaults to `1.5`. `0` strikes the exact target. |
| `delay` | Cooldown in **milliseconds**. Defaults to `0`, which disables the cooldown. |

The cooldown is shared by the player's Thor items. Negative variation or delay values are treated as `0`.

### Cooldown message

Change the message in `config.yml`:

```yaml
messages:
  thor:
    cooldown: "<red>You must wait <time>s before calling the storm again."
```

`<time>` is replaced with the remaining cooldown in seconds, with one decimal place. The rest of the message supports MiniMessage formatting.
