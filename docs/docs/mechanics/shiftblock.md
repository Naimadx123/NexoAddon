---
description: Temporarily change the CustomBlock or Furniture to another one!
---

# ShiftBlock

Change CustomBlock or Furniture to another one for specified time and add new Mechanic to your server!

### Change CustomBlock

<pre class="language-yaml"><code class="lang-yaml"># Example

Mechanics:
<strong>  shiftblock:
</strong>    time: 5                      # Time in seconds
    replace_to: test_block2      # Id of CustomBlock
</code></pre>

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

#### Resource generation with `on_break`

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

<figure><img src="../.gitbook/assets/on_break.gif" alt="" width="640"><figcaption></figcaption></figure>

#### ‎ ‎ Block transform with `on_place`

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

<figure><img src="../.gitbook/assets/on_place.gif" alt="" width="640"><figcaption></figcaption></figure>
