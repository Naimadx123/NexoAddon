---
description: Automate your fishing!
---

# AutoCatch

Make your fishing rod catch fish automatically!

```yaml
# Example

automatic_fishing_rod:
  material: FISHING_ROD
  Pack:
    model: nexo:item/fishing/auto_fishing
    custom_model_data: 1001
  Mechanics:
    autocatch:
      toggable: false
      recast: false
```

`toggable` defaults to `false`, so automatic catching is always active. Set it to `true` to let the player switch it on or off by left-clicking a non-interactive block with the rod in their main hand. A toggleable rod starts disabled.

`recast` defaults to `true` and casts in the direction the player is looking. With `false`, the new hook is returned to the previous hook's location.
