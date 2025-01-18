---
description: Kill Message mechanic.
---

# Kill Message

The Kill Message Mechanic allows you to set a custom message for when somebody kills a player


### Simple Kill Message

```yaml
# Example

Mechanics:
  kill_message: <player> was killed by <italic><red><killer>
  # <player> gets replaced with the victim
  # <killer> gets replaced with the killer
  # <italic> and <red> are minimessage formatting
```