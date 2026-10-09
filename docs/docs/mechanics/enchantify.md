---
description: Enchantify mechanic.
---

# Enchantify

The Enchantify Mechanic turns an item into a consumable enchantment applicator. Pick the enchantify item up on your cursor, **left-click** a target item in your inventory, and the configured enchantments are added to it. One enchantify item is consumed per successful use.

Each enchant adds its configured `level` on top of the target's current level, optionally capped by a `limit`.

{% hint style="info" %}
If every configured enchant is already at its `limit` on the target item, **nothing is consumed** — the click is simply ignored, so players never waste an item on a maxed-out target.
{% endhint %}

{% hint style="info" %}
Enchantment names use the Minecraft key (e.g. `sharpness`, `unbreaking`). The `minecraft:` namespace is assumed when no namespace is given, so custom enchants can be referenced as `namespace:key`.
{% endhint %}

### Simple enchantment book

```yaml
# Example

sharpness_scroll:
  material: PAPER
  itemname: <aqua>Scroll of Sharpness
  Mechanics:
    enchantify:
      enchants:
        - enchant: sharpness   # Enchantment key.
          level: 1             # Levels added to the target's current level on each use.
          limit: 5             # Maximum level this enchant can reach on the target. optional.
```

### Multiple enchantments at once

```yaml
# Example

master_scroll:
  material: PAPER
  itemname: <gold>Master Scroll
  Mechanics:
    enchantify:
      enchants:
        - enchant: sharpness
          level: 1
          limit: 10
        - enchant: unbreaking
          level: 2
          limit: 3
        - enchant: mending     # No limit -> can be applied without a level cap.
          level: 1
```

### Restricting which items can be enchanted

```yaml
# Example

sword_only_scroll:
  material: PAPER
  itemname: <red>Blade Scroll
  Mechanics:
    enchantify:
      enchants:
        - enchant: sharpness
          level: 1
          limit: 5
      whitelist:               # Only these items can be enchantified (materials or Nexo ids).
      - DIAMOND_SWORD
      - forest_sword
      blacklist:               # These items can never be enchantified (materials or Nexo ids).
      - NETHERITE_SWORD
      - forest_axe
```
