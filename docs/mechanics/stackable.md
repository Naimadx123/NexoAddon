---
description: Stack Stringblocks on each other!
---

# Stackable

Change your custom stringblock like a minecraft PINK PETALS or CANDLE!

{% hint style="info" %}
This mechanic only works with Stringblocks!
{% endhint %}

```yaml
# Example

clover:
  itemname: Clover
  material: PAPER
  Pack:
    ...
  Mechanics:
    custom_block:
      type: STRINGBLOCK
      ...
      stackable:
        next: clover_2         # Next Stringblock item
        group: clover          # Whatever group id

clover_2:
  Pack:
    ...
  Mechanics:
    custom_block:
      type: STRINGBLOCK
      ...
      stackable:
        next: clover_3          # Next Stringblock item
        group: clover           # The same group id

clover_3:
  Pack:
    ...
  Mechanics:
    custom_block:
      type: STRINGBLOCK
      ...
      # There is no more stackable Mechanic so this is the last stackable option
```
