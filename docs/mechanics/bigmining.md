---
description: The BigMining mechanic
---

# BigMining

The BigMining mechanic allows players to break multiple blocks around a target block efficiently, respecting protection rules, unbreakable blocks, and player permissions.

{% hint style="info" %}
`radius` and `depth` options are required!
{% endhint %}

```yaml
# Example

Mechanics:
  bigmining:
    radius: 2         # The radius of blocks around the destroyed block to break.
    depth: 3          # The depth of blocks to break in the direction of mining.
```
