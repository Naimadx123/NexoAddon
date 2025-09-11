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
      toggable: false      # Allow do enbale/disable auto chatch
      recast: false        # Should recast the fish hook when player is looking or to to the previous location?
```
