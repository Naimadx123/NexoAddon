# equippable

{% hint style="info" %}
`eqippable` Component is handling by NexoAddon on versions 1.20.4-1.21.2\
\
Available slots: [here](https://helpch.at/docs/1.20.4/org/bukkit/inventory/EquipmentSlot.html#enum-constant-summary)
{% endhint %}

Refer to [Nexo docs](https://docs.nexomc.com/configuration/items-advanced) for setup this Component.

```yaml
# Example

forest_helmet:
  itemname: Forest Helmet
  material: PAPER
  Pack:
    model: nexo:item/nexo_armor/forest_helmet
    custom_model_data: 1000
  trim_pattern: nexo:forest
  Components:
    equippable:
      slot: HEAD
```
