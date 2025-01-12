---
description: MiningTools mechanic.
---

# MiningTools

Allows you to destroy Custom Blocks or only drop Custom Blocks drop using specified list of Nexo Items and Vanilla Materials.

{% hint style="info" %}
`items` option is required!
{% endhint %}

### Mine custom blocks with specified items

```yaml
# Example

Mechanics:
  custom_block:
    type: NOTEBLOCK
    custom_variation: 6
    model: test_block
    drop:
      silktouch: false
      loots:
      - nexo_item: test_block
        probability: 1.0
    miningtools:
      type: CANCEL_EVENT        # Optional: CANCEL_EVENT or CANCEL_DROP if tool is not in list below (default: CANCEL_EVENT)
      items:
      - forest_sword            # Nexo item
      - PAPER                   # Vanilla Material
```
