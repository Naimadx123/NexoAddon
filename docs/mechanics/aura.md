---
description: Aura mechanic
---

# Aura

The Aura Mechanic creates dynamic, particle-based visual effects around the player, triggered by specific  items. Auras are customizable through formulas and particle types, allowing for a wide variety of effects like rings, spirals, hearts, or custom shapes. These effects can be used for decorative or gameplay purposes, adding an immersive visual layer to the player's experience.

<figure><img src="../.gitbook/assets/obraz_2025-01-12_162030861.png" alt=""><figcaption><p>Aura showcase</p></figcaption></figure>

{% hint style="info" %}
Aura mechanic works on held item and items in armor slots!
{% endhint %}

## Basic Aura

```yaml
# Example

Mechanics:
  aura:
    type: simple          # Available: simple, ring, helix, heart, custom
    particle: PORTAL      # Particle to spawn
```

## Advanced Aura

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

```yaml
# Example

Mechanics:
  aura:
    type: custom
    # Egg shape
    custom: (x+1*cos(angle)*cos(angle2)),(y+1.5*sin(angle2)+1),(z+1*sin(angle)*cos(angle2))
    particle: END_ROD
```

### Supported Operators

- **Addition (`+`)**: Adds two numbers. Example: `2 + 2`.
- **Subtraction (`-`)**: Subtracts the second number from the first. Example: `2 - 2`.
- **Multiplication (`*`)**: Multiplies two numbers. Example: `2 * 2`.
- **Division (`/`)**: Divides the first number by the second. Example: `2 / 2`.
- **Exponential (`^`)**: Raises the first number to the power of the second. Example: `2 ^ 2`.
- **Unary minus/plus (`-` / `+`)**: Negates or keeps the sign of a single number. Example: `+2 - (-2)`.
- **Modulo (`%`)**: Returns the remainder of the division of the first number by the second. Example: `2 % 2`.

### Supported Functions

- **`abs`**: Absolute value.
- **`acos`**: Arc cosine.
- **`asin`**: Arc sine.
- **`atan`**: Arc tangent.
- **`cbrt`**: Cube root.
- **`ceil`**: Round up.
- **`cos`**: Cosine.
- **`cosh`**: Hyperbolic cosine.
- **`exp`**: Exponential (`e^x`).
- **`floor`**: Round down.
- **`log`**: Natural logarithm.
- **`log2`**: Base-2 logarithm.
- **`log10`**: Base-10 logarithm.
- **`sin`**: Sine.
- **`sinh`**: Hyperbolic sine.
- **`sqrt`**: Square root.
- **`tan`**: Tangent.
- **`tanh`**: Hyperbolic tangent.
- **`signum`**: Signum. 