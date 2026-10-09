---
description: Unstack Stringblocks!
---

# Unstackable

Take part of your stringblocks like a CAKE!

{% hint style="info" %}
This mechanic works with Stringblocks and Furniture!
{% endhint %}

```yaml
# Example

cake_3:
  itemname: Clover
  material: PAPER
  Pack:
    ...
  Mechanics:
    unstackable:
      next: cake_2           # Next Stringblock item
      give: cake_piece       # Give NexoItem

cake_2:
  Pack:
    ...
  Mechanics:
    unstackable:
      next: cake              # Next Stringblock item
      give: REDSTONE          # Give Vanilla Material

cake:
  Pack:
    ...
  Mechanics:
    unstackable:
      next: stop              # No next stage.
      give: cake_piece        # Give Vanilla Material
      items:                  # List of items that may be used to trigger Unstackable Mechanic
      - forest_axe
      - DIAMOND_AXE
```
