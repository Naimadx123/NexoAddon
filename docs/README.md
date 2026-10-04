---
description: Backward compatibility and new features for the Nexo plugin!
---

# NexoAddon

NexoAddon adds item mechanics, components, recipes and world populators to Nexo. Configure item features in your Nexo item files, and global settings in `plugins/NexoAddon/config.yml`.

Use [mechanics](mechanics/aura.md) to add behaviour to items, custom blocks and furniture. [Custom Biomes](biomes/custom-biomes.md) define registered biomes, while [Block Populators](populators/block-populators.md) and [Biome Populators](populators/biome-populators.md) apply generation rules to your worlds.

### Item templates

Addon `Mechanics` and `Components` are also loaded from Nexo item templates. Items can reference an existing template with `template`, either as a single ID or a list:

```yaml
storm_hammer:
  template: storm_tool
  material: IRON_AXE
  Mechanics:
    thor:
      delay: 3000
```

```yaml
storm_hammer:
  template: [ storm_tool, glowing_tool ]
  material: IRON_AXE
```

Templates are merged in order, then the item's own fields are applied. Later templates and explicit item fields take precedence. The `templates` list is also accepted when no `template` IDs are supplied.

### Reloading changes

Use [`/nexoaddon reload`](commands/commands.md#reload) after editing addon settings, mechanics, recipes or populators. Custom biome definitions require a full server restart to update the biome registry.

