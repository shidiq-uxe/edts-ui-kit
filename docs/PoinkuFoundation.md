# Poinku Foundation Tokens

This document describes the first product theme for the generalized foundation
layer. The values are sourced from [Poinku Foundation 2.0](https://www.figma.com/design/VDm3wrVAgs03BglkFrMSH8/Poinku-Foundation-2.0?node-id=1-3).

## Resource layers

The Android resources intentionally have two layers:

- `poinku_colors.xml` contains the exact Poinku palette values from the Color
  foundation: neutral, grey, primary blue, secondary red/orange, and support
  colors.
- `foundation_colors.xml` is the component-facing semantic contract. It maps
  roles such as `foundation_color_bg_primary` and
  `foundation_color_bg_success_subtle` to the Poinku implementation.

Generalized components should depend on `foundation_color_*` resources. A Klik
theme can later override those semantic resource names without changing the
component implementation.

## Figma variable mapping

The Figma library exposes variable roles including:

```text
color/bg/primary
color/fg/primary
color/fg/label
color/fg/disabled
color/stroke/primary
color/bg/success/subtle
color/fg/success/intense
```

The local file has no local variable collections; these are library variables,
not variables stored in the Poinku file. The concrete values used here come
from the Color page definitions, for example:

```text
Primary Blue/30 (Base)             #1178D4
Grey/80                            #151823
Support/Success/Weak               #F6FFEB
Support/Highlight/Primary/Weak     #E7F1FD
```

The semantic mappings in `foundation_colors.xml` are explicit Poinku defaults
derived from the visible foundation definitions and usage notes. They are not
an assertion that the remote Figma alias graph was imported, because the
connected library search exposed the variable names/scopes but not their
resolved alias values.

The six Figma gradient variables were exposed without resolved color values,
so they are intentionally not represented as guessed Android drawables yet.
They should be added once their stop colors and direction are available.

## Adoption example

```xml
<TextView
    android:textColor="@color/foundation_color_fg_label"
    android:background="@color/foundation_color_bg_primary" />
```

Do not add new generalized component references to the legacy palette names in
`colors.xml`; those names remain untouched for compatibility with existing
components and product work.
