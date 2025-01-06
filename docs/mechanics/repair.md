---
description: Repair mechanic.
---

# Repair

The Repair Mechanic allows players to repair damaged items in their inventory using a specific repair item. The repair process consumes the repair item while restoring a portion of the durability of the damaged item. The amount of durability restored is determined by the repair ratio defined in the mechanic configuration.

{% hint style="info" %}
`ratio` options is required!
{% endhint %}

### One-time Repair item stackable

```yaml
# Example

repair_tool:
  material: PAPER
  itemname: <blue>Repair tool
  Mechanics:
    repair:
      ratio: 0.5         # Percentage of durability restored, relative to the item's current damage.

```

### One-time Repair item unstackable

```yaml
# Example

repair_tool:
  material: PAPER
  itemname: <blue>Repair tool
  Components:
    max_stack_size: 1
  Mechanics:
    repair:
      ratio: 0.5         # Percentage of durability restored, relative to the item's current damage.

```

### Reusable Repair item unstackable

```yaml
# Example

repair_tool:
  material: PAPER
  itemname: <blue>Repair tool
  Components:
    max_stack_size: 1
    durability: 50       # 50 uses
  Mechanics:
    repair:
      ratio: 0.5         # Percentage of durability restored, relative to the item's current damage.

```
