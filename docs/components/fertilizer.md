# fertilizer

`fertilizer`Component have 2 properties, `usable_on`and \`growth\_speedup\`. Check example below.

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
