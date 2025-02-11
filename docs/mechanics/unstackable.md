---
description: Stack Stringblocks!
---

# Unstackable

Take part of your stringblocks like a CAKE!

{% hint style="info" %}
This mechanic only works with Stringblocks!
{% endhint %}

```yaml
# Example

cake_3:
  itemname: Clover
  material: PAPER
  Pack:
    ...
  Mechanics:
    custom_block:
      type: STRINGBLOCK
      ...
      unstackable:
        next: cake_2           # Next Stringblock item
        give: cake_piece       # Give NexoItem

cake_2:
  Pack:
    ...
  Mechanics:
    custom_block:
      type: STRINGBLOCK
      ...
      unstackable:
        next: cake              # Next Stringblock item
        give: REDSTONE          # Give Vanilla Material

cake:
  Pack:
    ...
  Mechanics:
    custom_block:
      type: STRINGBLOCK
      ...
      unstackable:
        next: stop              # No next stage.
        give: cake_piece        # Give Vanilla Material
        items:                  # List of items that may be used to trigger Unstackable Mechanic
        - forest_axe
        - DIAMOND_AXE
      # There is no more stackable Mechanic so this is the last stackable option
```
