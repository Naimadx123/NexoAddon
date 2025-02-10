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

### Shift triggers

```yaml
# Example

Mechanics:
  custom_block:
    type: NOTEBLOCK
    shiftblock:
      time: 5                      # Time in seconds
      replace_to: test_block2      # Id of CustomBlock      

      on_interact: true            # true is default
      on_break: false              # false is default
      on_place: false              # false is default
```
### Use Case examples

Resource generation with `on_break`
```yaml
honeycomb_normal:
  Mechanics:
    custom_block:
      type: NOTEBLOCK
      hardness: 999
```
```yaml
honeycomb_ore:
  Mechanics:
    custom_block:
      type: NOTEBLOCK
      shiftblock:
        time: 5
        replace_to: honeycomb_normal
        on_interact: false
        on_break: true
        on_place: false
```
https://github.com/user-attachments/assets/26967429-25ae-4759-a5ee-9f8e517d32ec

‎ 
‎ 


‎ 


‎ 
‎
Block transform with `on_place`
```yaml
honeycomb_normal:
  Mechanics:
    custom_block:
      type: NOTEBLOCK
      hardness: 999
```
```yaml
cut_nano_flux:
  Mechanics:
    custom_block:
      type: NOTEBLOCK
      shiftblock:
        time: 5
        replace_to: honeycomb_normal
        on_interact: false
        on_break: false
        on_place: true
```
https://github.com/user-attachments/assets/f1882326-d695-4e48-add9-fbb93dfe058f

