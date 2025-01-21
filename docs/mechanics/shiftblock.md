---
description: Temporarily change the CustomBlock to another one!
---

# ShiftBlock

Change CustomBlock to another one for specified time and add new Mechanic to your server!

### Change CustomBlock

```yaml
# Example

Mechanics:
  custom_block:
    type: NOTEBLOCK
    shiftblock:
      time: 5                      # Time in seconds
      replace_to: test_block2      # Id of CustomBlock
```

### Change CustomBlock if clicked with specified items

```yaml
# Example

Mechanics:
  custom_block:
    type: NOTEBLOCK
    shiftblock:
      time: 60                     # Time in seconds
      replace_to: test_block2      # Id of CustomBlock
      items:                       # Allow to use this Mechanic only using those items
      - forest_axe                 # Nexo Item
      - PAPER                      # Vanilla Material
```
