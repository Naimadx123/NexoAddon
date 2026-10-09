---
description: Aura mechanic
---

# Aura

The Aura Mechanic creates dynamic, particle-based visual effects around the player, triggered by specific  items. Auras are customizable through formulas and particle types, allowing for a wide variety of effects like rings, spirals, hearts, or custom shapes. These effects can be used for decorative or gameplay purposes, adding an immersive visual layer to the player's experience.

<figure><img src="../.gitbook/assets/obraz_2025-01-12_162030861.png" alt=""><figcaption><p>Aura showcase</p></figcaption></figure>

{% hint style="info" %}
Aura mechanic works on the item held in the **main hand** and items in armor slots!
{% endhint %}

## Basic Aura

```yaml
# Example

Mechanics:
  aura:
    type: simple          # Available: simple, ring, helix, heart, custom
    particle: PORTAL      # Particle to spawn
```

## Multiple Auras

`aura` also accepts a list, so one item can combine several effects - including more than one `custom` formula.

```yaml
# Example

Mechanics:
  aura:
    - type: ring
      particle: PORTAL
    - type: custom
      particle: FLAME
      custom: "(x+2*cos(angle+time)),(y+1),(z+2*sin(angle+time))"
    - type: custom
      particle: SOUL_FIRE_FLAME
      custom: "(x+2*cos(angle-time)),(y+1.5),(z+2*sin(angle-time))"
```

## Advanced Aura

{% hint style="info" %}
Available variables: `x`, `y`, `z`, `angle`, `angle2`, `yaw`, `pitch`, `time`, `Math_PI`

`x`, `y` and `z` are the player's world coordinates. `angle` increases by `2π / points` for each particle and continues across passes. `angle2` starts at `-π/2` and increases by `π / points` after each pass. Both angles are in radians.

`time` is the number of seconds since the aura manager was created when the plugin enabled, so `angle+time` rotates a shape and `sin(time)` makes it pulse. `yaw` and `pitch` are in degrees; multiply them by `Math_PI / 180` to use them in `sin` or `cos`.
{% endhint %}

```yaml
# Example

Mechanics:
  aura:
    type: custom
    # Rotating ring
    custom: "(x+2*cos(angle+time)),(y+1),(z+2*sin(angle+time))"
    particle: FLAME
    points: 40
```

The `custom` value must contain three expressions separated by commas: the particle's world `x`, `y` and `z` coordinates. Invalid formulas and unknown particle names are skipped with a warning in the server log.

`points` defaults to `20`, with a minimum of `1`. A custom aura runs `points` passes with `points` particles each: `20` produces `400` particles per update, while `40` produces `1600`. This option only affects custom auras.

The update interval is controlled by [`aura_mechanic_delay`](../settings/prevent-furniture-breaks-1.md) in `config.yml`, which defaults to `5` ticks. Combining several custom auras adds their particle counts together.

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
