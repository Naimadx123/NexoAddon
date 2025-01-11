---
description: Aura mechanic
---

# Aura

The Aura Mechanic creates dynamic, particle-based visual effects around the player, triggered by specific  items. Auras are customizable through formulas and particle types, allowing for a wide variety of effects like rings, spirals, hearts, or custom shapes. These effects can be used for decorative or gameplay purposes, adding an immersive visual layer to the player's experience.

{% hint style="info" %}
Aura mechanic works on held item and items in armor slots!
{% endhint %}

### Basic Aura

```yaml
# Example

Mechanics:
  aura:
    type: simple          # Available: simple, ring, helix, heart, custom
    particle: PORTAL      # Particle to spawn
```

### Advanced Aura

{% hint style="info" %}
Available variables: `x`, `y`, `z`, `angle`, `angle2`,`yaw`, `pitch`, `Math_PI`
{% endhint %}

```yaml
# Example

Mechanics:
  aura:
    type: custom
    # Dropper like formula
    custom: "(x+cos(angle)*angle*0.1),(y+angle*0.1),(z+sin(angle)*angle*0.1)"
    particle: HEART
```

```yaml
# Example

Mechanics:
  aura:
    type: custom
    # Heart like formula - nonrotatable (you can add yaw or pitch to rotate)
    custom: "(x+4*sin(angle)^3),(y+0.5),(z+3*cos(angle)-1.25*cos(2*angle)-0.75*cos(3*angle)-0.25*cos(4*angle))"
    particle: FLAME
```
