---
description: Totem Animation Command.
icon: "🗿"
---

# Totem Animation

{% hint style="warning" %}
The item **MUST** be a `TOTEM_OF_UNDYING` to display custom models/textures!
{% endhint %}

### Usage

```yaml
/nexoaddon totem <player> <customModelData|nexoID>
```

### Examples

#### Using CustomModelData
```yaml
/nexoaddon totem PlayerName 1000
```

#### Using Nexo Items
```yaml
/nexoaddon totem PlayerName diamond_totem
```

### Required Item Setup

For custom totems to work, they must be properly configured as `TOTEM_OF_UNDYING` items. Here are example configurations:

#### Nexo Item Configuration
```yaml
diamond_totem:
  material: TOTEM_OF_UNDYING
  Pack:
    parent_model: item/generated
    texture: item/diamond
    custom_model_data: 1000

cool_totem:
  material: TOTEM_OF_UNDYING
  Pack:
    model: my_cool_model
    custom_model_data: 1001
```
{% hint style="info" %}
The animation will only show correctly if:
1. The item is a `TOTEM_OF_UNDYING`
2. The custom model/texture is properly set up in Nexo
{% endhint %}