---
description: Signal mechanic.
---

# Signal

The Signal mechanic lets you control light sources of nearby furniture using just 1 furniture, meaning you can make light switches

{% hint style="danger" %}
**Setting the `radius` too high will result in lag and possibly crashing!**
{% endhint %}


### Light switch that controls furniture in a radius of 15 (31x31x31) on channel 1.00:

```yaml
Mechanics:
  furniture:
    furniture:
      signal:
        radius: 15         # Can be any integer (1, 2, 48594, 2147483647) DON'T MAKE THIS VALUE HIGH.
        channel: 1.00      # Can be any double (1, 1.00, 0.001, 12.34, 3.657).
        role: TRANSMITTER  # TRANSMITTER makes it be the "controller".
```

### Light that gets toggled if it's in the range of a channel 1.00 transmitter

```yaml
Mechanics:
  furniture:
    furniture:
      signal:
        channel: 1.00   # Same channel as the TRANSMITTER (Light switch) above.
        role: RECEIVER  # RECEIVER makes it receive.
```