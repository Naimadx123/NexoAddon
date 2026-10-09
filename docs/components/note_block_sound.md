---
description: Set the note block sound of a custom player head.
---

# note\_block\_sound

The `note_block_sound` Component assigns a sound to a player head. A note block with that head placed above it can play the configured sound.

{% hint style="info" %}
Use this component on a `PLAYER_HEAD` item. It works with either [`skull_value`](skull_value.md) or Nexo's `profile` component; `skull_value` is not required. The server must support setting a head's note block sound; on older versions this field is ignored.
{% endhint %}

### A head with a custom sound

```yaml
thunder_head:
  material: PLAYER_HEAD
  itemname: <aqua>Thunder Head
  Components:
    skull_value: eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWY1ZTgzNWMxMTZlOGUyMDBlMmUwNmFhNTkzY2FiOGYxYTlmOGM0MGU3ZjAwNWE5Yzc2ZjEyZTI0ZjRjNjM3MCJ9fX0=
    note_block_sound: minecraft:entity.lightning_bolt.thunder
```

The value is a sound key. Omitting the namespace defaults to `minecraft`; resource pack sounds can use their own namespace, for example `my_pack:head.chime`.

### Using Nexo's profile component

```yaml
thunder_player_head:
  material: PLAYER_HEAD
  itemname: <aqua>Thunder Player Head
  Components:
    profile:
      name: PlayerName
    note_block_sound: minecraft:entity.lightning_bolt.thunder
```

NexoAddon preserves the profile set by Nexo and adds the configured sound. If `skull_value` is also configured, its texture profile takes precedence.
