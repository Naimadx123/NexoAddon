# fertilizer

`fertilizer`Component have 2 properties, `usable_on`and \`growth\_speedup\`. Check example below.

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

<pre class="language-yaml"><code class="lang-yaml"><strong># Example
</strong>
common_fertilizer:
  itemname: "&#x3C;#D5D6D8>Common Fertilizer"
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
</code></pre>
