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
Ingredient slots outside of the menu, for example slot `30` in a menu with `rows: 3`, are ignored and recipes using them will never match.
{% endhint %}

### How the menu behaves

* Slots that no recipe of the station uses are locked, items cannot be placed there.
* Shift-clicking an item from the inventory puts it in the first free ingredient slot.
* The result is only a preview until it is clicked. Clicking it consumes the ingredients and gives the item.
* Closing the menu returns everything left in the ingredient slots to the player.
* Every player gets their own menu, nothing is shared between players.

Recipes are reloaded together with the other recipe files, so `/nexoaddon reload`, `/nexo reload` and uploading the resource pack all pick up changes without a restart.
