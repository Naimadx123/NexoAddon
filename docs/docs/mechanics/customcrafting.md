---
description: CustomCrafting mechanic.
---

# CustomCrafting

The CustomCrafting Mechanic turns a CustomBlock or Furniture into a crafting station with its own menu. Ingredients are placed in the slots the recipe asks for, and the result shows up in the slot you picked.

Each station keeps its recipes in its own folder, so every crafting block can have a completely different set of recipes.

### Creating a station

```yaml
# Example

ruby_crafting_table:
  material: PAPER
  itemname: Ruby Crafting Table
  Mechanics:
    custom_block:
      type: NOTEBLOCK
      model: ruby_crafting_table
    custom_crafting:
      station_id: ruby_station              # Folder inside Nexo/recipes/custom_crafting. Required.
      title: <dark_gray>Ruby Station        # Title of the menu. Defaults to 'Crafting'.
      rows: 3                               # Height of the menu, 1-6. Defaults to 3.
      result_slot: 18                       # Slot the result is shown in. Defaults to the last slot.
      filler:                               # Optional item placed in every locked slot.
        minecraft_item: GRAY_STAINED_GLASS_PANE
```

Right-clicking the block or furniture opens the menu. The same `station_id` may be used by several items, so a wooden and a golden crafting table can share one set of recipes.

For furniture, put `custom_crafting` directly under `Mechanics`, at the same indentation as `furniture`. It must be configured on the item ID of the placed furniture. `enabled` is optional and defaults to `true`.

The `title` supports MiniMessage formatting. `rows` is clamped to `1`–`6`; an invalid `result_slot` prevents the station from loading and is reported in the server log.

{% hint style="info" %}
Slots are counted from `0`, left to right and top to bottom, so a menu with `rows: 3` has the slots `0` to `26`.
{% endhint %}

### Adding recipes

Recipes live in `Nexo/recipes/custom_crafting`. Every folder there is a station and every `.yml` file inside it is one recipe.

```
Nexo/recipes/custom_crafting/
└── ruby_station/
    ├── ruby_sword.yml
    └── ruby_pickaxe.yml
```

```yaml
# Example - ruby_sword.yml

ingredients:
  '12':
    nexo_item: ruby                         # Specify a Nexo item or use minecraft_item for vanilla items
    amount: 2                               # Amount required in this slot. Defaults to 1.
  '14':
    minecraft_item: STICK
  '16':
    nexo_item: magic_dust
result:
  nexo_item: ruby_sword                     # Item given when the recipe is crafted
  amount: 1
```

The recipe only matches when every listed slot holds the right item in at least the required amount **and** every other ingredient slot of the station is empty, so the layout itself is part of the recipe.

{% hint style="warning" %}
Ingredient slots outside of the menu, for example slot `30` in a menu with `rows: 3`, or equal to `result_slot` cannot be used, and recipes using them will never match. Slots must be numeric and between `0` and `53`; an invalid slot, ingredient or result causes the recipe to be skipped with a warning.
{% endhint %}

### Matching item components

Without `components`, a vanilla ingredient matches its material regardless of its name, lore or potion contents. Nexo items must use `nexo_item`, even when their material is the same as a vanilla ingredient.

To require a specific vanilla item, add `components` as a separate string field. Keep `minecraft_item` as the material name and write the component assignments without surrounding square brackets.

```yaml
ingredients:
  '11':
    minecraft_item: POTION
    components: 'potion_contents={potion:"minecraft:awkward"}'
    amount: 1
  '15':
    minecraft_item: SUGAR
result:
  minecraft_item: POTION
  components: 'potion_contents={potion:"minecraft:swiftness"}'
  amount: 1
```

This recipe accepts only an awkward potion in slot `11`. Regeneration potions and other potion types do not match.

{% hint style="info" %}
`components` enables an exact comparison of the item's full metadata, not a partial check of the listed components. An awkward potion with an extra custom name or lore will not match unless those components are included too. Stack size is checked separately through `amount`.
{% endhint %}

The same field can be used on vanilla `result` and `filler` items. It is not applied to `nexo_item`; configure those items in their Nexo item definition. Component names and values must be valid for the server's Minecraft version. Empty, non-string or invalid `components` values are rejected.

### How the menu behaves

* Slots that no recipe of the station uses are locked, items cannot be placed there.
* Shift-clicking an item from the inventory puts it in the first free ingredient slot.
* The result is only a preview until it is clicked. Clicking it consumes the ingredients and gives the item.
* Closing the menu returns everything left in the ingredient slots to the player. Items that do not fit in the inventory are dropped at the player's location.
* Every player gets their own menu, nothing is shared between players.

Recipes are reloaded together with the other recipe files, so `/nexoaddon reload`, `/nexo reload` and uploading the resource pack all pick up changes without a restart.
