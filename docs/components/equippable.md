# equippable

{% hint style="info" %}
NexoAddon handles the `equippable` Component on versions 1.20.4–1.21.2. On 1.21.3 and newer, it leaves equipping to the native component.

Available armor slots: `HEAD`, `CHEST`, `LEGS`, `FEET`. The default is `HEAD`.
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
