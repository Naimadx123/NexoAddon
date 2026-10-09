---
description: Lifesteal mechanic.
---

# Lifesteal

The Lifesteal Mechanic heals the attacker whenever they damage a living entity while holding the item in their main hand. The same amount of health is subtracted from the victim. An optional cooldown limits how often the heal can trigger.

{% hint style="info" %}
The item must be held in the **main hand** and it respects protection regions (players can only lifesteal where they are allowed to interact).
{% endhint %}

### Simple lifesteal sword

```yaml
# Example

lifesteal_sword:
  material: DIAMOND_SWORD
  itemname: <red>Vampiric Blade
  Mechanics:
    lifesteal:
      amount: 2          # Health points healed on hit (and subtracted from the victim). Defaults to 1.
```

### Lifesteal with cooldown

```yaml
# Example

lifesteal_sword:
  material: DIAMOND_SWORD
  itemname: <red>Vampiric Blade
  Mechanics:
    lifesteal:
      amount: 4          # Health points healed on hit. Defaults to 1.
      cooldown: 2.5      # Cooldown in seconds between two lifesteal triggers. Defaults to 0.0 (no cooldown).
```
