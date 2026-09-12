---
description: Fuel mechanic.
---

# Fuel

The Fuel Mechanic turns an item into furnace fuel with its own burn time. It works in furnaces, blast furnaces and smokers, can be limited to some of them, and can leave an item behind once burnt - like a lava bucket leaving an empty bucket.

{% hint style="warning" %}
The item's `material` **MUST** be something vanilla already accepts as fuel (`COAL`, `CHARCOAL`, `STICK`, `LAVA_BUCKET`, any wooden block...). The fuel slot and hoppers only take vanilla fuels; the burn time is then fully replaced by the value below.
{% endhint %}

### Simple fuel

```yaml
# Example

magma_coal:
  material: COAL
  itemname: <gold>Magma Coal
  Mechanics:
    fuel:
      burn_time: 4800    # Burn time in TICKS. Coal is 1600 (8 items). Defaults to 1600.
```

### Only in some furnaces

```yaml
# Example

magma_coal:
  material: COAL
  itemname: <gold>Magma Coal
  Mechanics:
    fuel:
      burn_time: 4800
      furnaces:          # Where this fuel burns. Omit for all of them.
        - BLAST_FURNACE
        - SMOKER
```

In any other furnace the item just sits in the fuel slot and never ignites.

### Leftover item

```yaml
# Example

magma_bottle:
  material: LAVA_BUCKET
  itemname: <gold>Bottled Magma
  Mechanics:
    fuel:
      burn_time: 20000
      leftover: GLASS_BOTTLE   # Material or Nexo item ID given back once the fuel is used up.
```

The leftover replaces the fuel in the slot. If the fuel was stacked, the leftover is dropped on top of the furnace instead.

### Infinite fuel

```yaml
# Example

eternal_flame:
  material: COAL
  itemname: <red>Eternal Flame
  Mechanics:
    fuel:
      burn_time: -1      # Never consumed. The furnace re-lights from the same item forever.
```

The item stays in the fuel slot across server restarts. Hoppers cannot pull it out; players still can. `leftover` is ignored here since nothing is ever used up.
