---
description: SpawnerBreak mechanic.
---

# SpawnerBreak

The SpawnerBreak Mechanic allows players to break spawners and have them drop


### Break a Spawner and always get the spawner

```yaml
# Example

Mechanics:
  spawnerbreak:
    probability: 1.0
```

### Break a Spawner but only have a 1% chance to get it and you get experience

```yaml
# Example

Mechanics:
  spawnerbreak:
    probability: 0.01
    dropExperience: true # This is optional, defaults to false
```