# fertilizer

The `fertilizer` Component speeds up furniture evolution or applies bone meal to vanilla blocks. `usable_on` and `growth_speedup` are required; `cooldown` is optional and defaults to `0` seconds.

For furniture, `growth_speedup` is added to its stored evolution progress. For vanilla blocks, it is the number of bone meal applications attempted per use.

### One time usable

```yaml
# Example

common_fertilizer:
  material: DIAMOND
  itemname: Common Ferilizer
  Components:
    fertilizer:
      usable_on:                  # List of furniture on which this fertiliser can be applied
      - rose_seed
      growth_speedup: 50          # Amount of time to skip growth
```

### Multi time usable

```yaml
# Example

common_fertilizer:
  itemname: "<#D5D6D8>Common Fertilizer"
  material: PAPER
  Pack:
    texture: test:fert/common_fertilizer.png
    custom_model_data: 1007
  Components:
    fertilizer:
      usable_on:
      - rose_seed
      - clover
      growth_speedup: 50
    max_stack_size: 1
    durability: 50                                  # How many uses
```

### Support Vanilla Blocks

```yaml
# Example

common_fertilizer:
  material: DIAMOND
  itemname: Common Ferilizer
  Components:
    fertilizer:
      usable_on:
      - _MINECRAFT                 # Act as bone meal on any vanilla block
      - WHEAT                      # Or just on specific minecraft block
      growth_speedup: 50
```

### Cooldown

```yaml
# Example

common_fertilizer:
  itemname: "<#D5D6D8>Common Fertilizer with Cooldown"
  material: PAPER
  Pack:
    texture: test:fert/common_fertilizer.png
    custom_model_data: 1007
  Components:
    fertilizer:
      usable_on:
      - _MINECRAFT
      growth_speedup: 50
      cooldown: 30
    max_stack_size: 1
    durability: 50                                  # How many uses
```

Put `cooldown` inside `fertilizer`. It is measured in seconds and shared by the player's fertilizer items.
