---
description: Give UniqueId to your items!
---

# UniqueId

Gives `nexoaddon:unique_id` key with random UUID value to the item to make sure about uniqueness

```yaml
# Example

my_unique_sword:
  material: DIAMOND_SWORD
  Pack:
    model: nexo:item/uniqueness/unique_sword
    custom_model_data: 1001
  Mechanics:
    uniqueid:
      enabled: true
```
