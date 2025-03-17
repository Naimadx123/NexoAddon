---
description: Decay chorusblock mechanic.
---

# Decay

Create unique leaves with Decay Mechanic!

{% hint style="info" %}
This Mechanic works on every custom\_block type
{% endhint %}

### Decaying leaves

```yaml
# Example

Mechanics:
    custom_block:
      type: CHORUSBLOCK
      decay:
        time: 5                  # Try to destroy leaves every x
        chance: 0.2              # Chance of decay
        base:
        - palmtree               # Nexo Custom Block
        - palm_y
        - palm_x
        - palm_z
        - OAK_LOG                # Vanilla Material
        radius: 3                # Radius of checking base items
```
