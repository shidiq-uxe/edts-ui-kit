# Design System Foundation Tokens

This document describes the first product theme for the generalized foundation
layer. The values are sourced from [Poinku Foundation 2.0](https://www.figma.com/design/VDm3wrVAgs03BglkFrMSH8/Poinku-Foundation-2.0?node-id=1-3).

## Resource layers

The Android resources intentionally have two layers:

- `poinku_colors.xml` contains the exact Poinku palette values from the Color
  foundation: neutral, grey, primary blue, secondary red/orange, and support
  colors.
- `klik_colors.xml` contains the exact Klik Global primitive tokens.
- `klik_theme_colors.xml` contains Klik's semantic mappings using
  product-specific names so this library can compile alongside Poinku.
- `foundation_colors.xml` contains the component-facing semantic contract. It
  retains the original Poinku names for compatibility and also exposes the
  unambiguous `foundation_color_semantic_*` roles for generalized components.

Generalized components should depend on `foundation_color_semantic_*` resources.
The Poinku values are the default implementation in this library. A Klik host
application should provide an overlay resource set that maps the same semantic
roles to the Klik values from `klik_theme_colors.xml`. This keeps component code
product-neutral while keeping both products available in one library module.

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

## Klik color foundation

The Klik source is [Klik Design Foundations 2.0](https://www.figma.com/design/e1rKJ0urLqWrr38TkkH4f6/Klik-Design-Foundations-2.0?node-id=9-145), specifically the
`Global Colors` frame and its bound variables. The local Figma inventory is:

- `Global`: 42 primitive variables.
- `Theme`: 62 semantic variables across background, foreground, and stroke.
- `Sizing`: 24 number variables. These are verified in Figma but are outside
  this color implementation.
- Total local variables: 128.
- Additional remote library dependencies used by the file: 19. These are
  typography, sizing, and elevation variables; they are not copied into the
  Klik color resources.

The 42 primitive values and the 62 semantic aliases match the provided Klik
inventory. The only source-level normalization is an accidental trailing space
in the Figma variable name `stroke width/0,5 (x-thin )`; Android resource names
use the normalized form. Figma's semantic aliases are represented explicitly,
for example `bg/background page/primary` points to `grey/20`, while
`fg/primary` points to `grey/70` and `stroke/primary` points to `grey/30`.

The implementation intentionally separates the layers:

```text
Klik primitives       klik_colors.xml
Klik semantic values  klik_theme_colors.xml
                         │
                         └─ product overlay in a Klik host application

Shared component API  foundation_color_semantic_*
Default implementation foundation_colors.xml → Poinku primitives
```

Android resource merging cannot contain two definitions of the same resource
name in the same source set. Therefore `klik_theme_colors.xml` does not redefine
the existing `foundation_color_*` names. It is the verified Klik mapping source
for the future Klik app/module overlay. The existing Poinku Foundation names
remain unchanged; this avoids silently changing Poinku behavior while allowing
new generalized components to consume the canonical semantic contract.

Example host overlay:

```xml
<color name="foundation_color_semantic_bg_container_primary">
    @color/klik_color_bg_container_primary
</color>
<color name="foundation_color_semantic_fg_primary">
    @color/klik_color_fg_primary
</color>
<color name="foundation_color_semantic_stroke_primary">
    @color/klik_color_stroke_primary
</color>
```

This is a color-only introduction. Klik sizing, typography, elevation, and
remote library variables remain documented scope for their respective
foundation work.

## Typography

The Typography frame in [Poinku Foundation 2.0](https://www.figma.com/design/VDm3wrVAgs03BglkFrMSH8/Poinku-Foundation-2.0?node-id=1-4)
uses Rubik with zero letter spacing. The Android implementation is split into
two resource layers:

- `poinku_typography.xml` contains the concrete Rubik implementation.
- `foundation_typography.xml` contains product-neutral aliases that generalized
  components should consume.
- `foundation_typography_dimensions.xml` stores the exact `sp` sizes and `dp`
  line heights used by the Poinku styles.

The available roles are Display D1-D3, Heading H1-H4, Body B1-B5, Paragraph
P1-P3, and Button Large/Medium/Small. The values are taken from the Figma
typography definitions, including the distinctions between Rubik Regular,
Medium, SemiBold, and Bold. For example:

```xml
<TextView
    style="@style/TextAppearance.Foundation.Body.B2.Medium"
    android:text="Poinku body text" />
```

The Figma library exposes `typography/typeface`, `typography/size/*`, and
`typography/weight/*` variables. Android resources cannot consume those remote
variables directly, so the role-to-value mapping is explicit in the local
dimension and style resources. The Figma variable search exposed names and
scopes, while the Typography frame definitions supplied the resolved values.

Existing `TextAppearance.Inter.*` styles remain unchanged for compatibility.
The Rubik resources use Android downloadable fonts, matching the repository's
existing Inter provider pattern; the host application must provide Google Play
services for runtime font retrieval.
