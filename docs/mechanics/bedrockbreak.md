---
description: BedrockBreak mechanic.
---

# BedrockBreak

The BedrockBreak Mechanic allows players to break bedrock blocks with specified tools

{% hint style="danger" %}
**For this Mechanic ProtocolLib is required!**
{% endhint %}

Looooooooong Bedrock Break

```yaml
# Example

Mechanics:
  bedrockbreak:
    hardness: 60               # How long should be breaking
    probability: 1             # Chance to drop Bedrock
    durability_cost: 1         # Cost of durability
```

Break Bedrock, but at what cost 😢 (break item on bedrock break)

```yaml
# Example

Mechanics:
  bedrockbreak:
    hardness: 50
    probability: 0.5                      
    durability_cost: 999999999            # Probably will destroy every created item
    disable_on_first_layer: true          # Default on true
```

Beyond the world! (Break bottom/last layer of the world)

```yaml
# Example

Mechanics:
  bedrockbreak:
    hardness: 1                           # Fast digging
    probability: 0.5                      
    durability_cost: 1
    disable_on_first_layer: false
```
