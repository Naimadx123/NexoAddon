---
description: Smithing Recipe
---

# Smithing Recipe

### Create custom smithing recipe!

```yaml
# Example

example_recipe:                           # Unique identifier for the recipe
  keep_durability: true                   # Should keep durability of base item?
  copy_trim: false                        # Should the result retain the trim design from the base item? Defaults to true
  copy_enchantments: true                 # Should enchantments from the base item be transferred to the result? Defaults to true
  result:
    minecraft_item: NETHERITE_CHESTPLATE  # Specify a Minecraft item or use nexo_item for custom items (e.g., nexo_item: custom_item_id)
  template:
    minecraft_item: NETHERITE_UPGRADE_SMITHING_TEMPLATE
  base:
    nexo_item: forest_chestplate
  addition:
    minecraft_item: NETHER_STAR
```

### New Tool types?

```yaml
# Example

enderite_sword:
  keep_durability: false                   # Will give a result item with full durability!
#  copy_trim: false                        # Those values are default
#  copy_enchantments: true                 # so we can skip them
  result:
    nexo_item: enderite_sword
  template:
    nexo_item: enderite_upgrade_smithing_template
  base:
    minecraft_item: NETHERITE_SWORD
  addition:
    nexo_item: enderite_ingot
```

> Smithing Recipes made by [**AkisYTB3**](https://github.com/AkisYTB3) via [Pull Request](https://github.com/Naimadx123/NexoAddon/pull/35)
