# HTML Handler

`HtmlHandler` is a flexible Android HTML-to-`Spanned` rendering system that extends `Android.text.Html`. It is designed to render rich HTML content into `TextView` with control over typography and list presentation.

The system is built around three main layers:
- **`HtmlRenderer`** — the entry point that processes and renders HTML strings.
- **`TagHandler`** — a custom `Html.TagHandler` implementation that handles list and font tags.
- **`HtmlRendererConfig`** — the configuration object that wires all styles, fonts, and tag mappings together.

---

## Features

- **Custom List Rendering**: Full support for `<ul>` and `<ol>` with configurable bullets, numbering formats, indentation, and text color.
- **Font Styling via Tags**: Apply custom `Typeface`, text color, and text size.
- **Tag Remapping**: Preprocesses standard HTML tags (e.g. `<ul>`, `<b>`) into custom tags to avoid conflicts with the native parser.
- **Composable Configuration**: Combine list styles, font styles, and tag mappings in a single `HtmlRendererConfig`.
- **Inter Font Family Support**: Prebuilt helpers for Inter font weights via `FontManager`.
- **`TextView` Extension Functions**: Utility extensions for clean, idiomatic integration.

---

## Architecture

The system is composed of the following key classes:

| Class / Object        | Responsibility                                                               |
|-----------------------|------------------------------------------------------------------------------|
| `HtmlRenderer`        | Entry point — preprocesses HTML and delegates to `TagHandler` for rendering  |
| `TagHandler`          | Implements `Html.TagHandler` to handle custom tags and apply spans            |
| `HtmlRendererConfig`  | Aggregates all configuration: list styles, font styles, tag mappings          |
| `ListStyle`           | Data class defining bullet/number appearance and layout                       |
| `FontStyle`           | Data class defining typeface, color, and size for styled tags                 |
| `FontManager`         | Loads `Typeface` resources from font resource IDs or font names               |
| `ListStyles`          | Singleton with preset `ListStyle` instances                                   |
| `TypefaceSpan`        | Custom `MetricAffectingSpan` to apply a `Typeface` to a text range            |
| `OrderedSpan`         | Custom span that renders bullet characters or number prefixes                 |

---

## Core Classes

### `HtmlRenderer`

The main rendering class. Accepts a config and font manager, exposes a single `render()` method.

```kotlin
class HtmlRenderer(
    private val config: HtmlRendererConfig,
    private val fontManager: FontManager
)
```

| Method | Description |
|--------|-------------|
| `render(html: String, textView: TextView): Spanned` | Preprocesses and renders HTML into a `Spanned` object ready for `TextView` |

---

### `HtmlRendererConfig`

Configuration object that defines how the renderer behaves. Passed to both `HtmlRenderer` and `TagHandler`.

```kotlin
class HtmlRendererConfig(
    val listConfig: HtmlListConfig = HtmlListConfig(),
    val unorderedStyle: ListStyle = ListStyles.default,
    val orderedStyle: ListStyle = ListStyles.default,
    val fontStyles: Map<String, FontStyle> = emptyMap(),
    val customTagMappings: Map<String, String> = emptyMap()
)
```

| Parameter | Description | Default |
|-----------|-------------|---------|
| `listConfig` | Line spacing, padding, and text size for list rendering | `HtmlListConfig()` |
| `unorderedStyle` | Style for `<ul>` list items | `ListStyles.default` |
| `orderedStyle` | Style for `<ol>` list items | `ListStyles.default` |
| `fontStyles` | Map of tag name → `FontStyle` for custom font rendering | `emptyMap()` |
| `customTagMappings` | Additional HTML tag remappings to apply before rendering | `emptyMap()` |

#### Companion Factory Methods

```kotlin
HtmlRendererConfig.minimal()                    // Default config, no custom styles
HtmlRendererConfig.styled()                     // Arrow bullets (green text) + parenthesized numbers (blue text)
HtmlRendererConfig.withCustomFonts(fontManager) // Bold red <b>, strong purple <strong>
```

---

### `TagHandler`

Internal `Html.TagHandler` implementation. Handles the custom tags produced by preprocessing and applies spans to the output `Editable`.

Handles the following tag types:

| Tag (after preprocessing) | Behavior |
|---------------------------|----------|
| `<myul>` | Opens/closes an unordered list context |
| `<myol>` | Opens/closes an ordered list context with counter |
| `<myli>` | Applies `LeadingMarginSpan` + `OrderedSpan` + color spans to each item |
| Custom font tags (e.g. `<myb>`) | Applies `TypefaceSpan`, `ForegroundColorSpan`, `AbsoluteSizeSpan` |

> **Note:** `TagHandler` is created internally by `HtmlRenderer` so do not need to instantiate it directly.

---

### `ListStyle`

Data class defining the visual appearance of a list.

```kotlin
data class ListStyle(
    val bulletChar: String = "•",
    val numberFormat: String = "%d.",
    val indentDp: Int = 8,
    val gapDp: Int = 8,
    val textColor: Int? = null,
    val backgroundColor: Int? = null
)
```

| Property | Description | Default |
|----------|-------------|---------|
| `bulletChar` | Character used as bullet for `<ul>` | `"•"` |
| `numberFormat` | `String.format` pattern for `<ol>` numbers | `"%d."` |
| `indentDp` | Left indent for list items (in dp) | `8` |
| `gapDp` | Gap between bullet/number and text (in dp) | `8` |
| `textColor` | Optional text color for list items | `null` |
| `backgroundColor` | Optional background color for list items | `null` |

#### Extension Functions

```kotlin
style.withCustomBullet("→")        // Change bullet character
style.withColor(Color.RED)         // Apply text color
style.withIndent(16)               // Change indentation
style.withGap(12)                  // Change bullet-to-text gap
```

#### Preset Styles (`ListStyles`)

```kotlin
ListStyles.default         // "•" bullet
ListStyles.arrow           // "→" bullet
ListStyles.star            // "★" bullet
ListStyles.numbered        // "1." format
ListStyles.parenthesized   // "(1)" format

ListStyles.green()         // Default style with green text color
ListStyles.blue()          // Default style with blue text color
ListStyles.red()           // Default style with red text color
```

---

### `FontStyle`

Data class describing how a custom HTML tag should style its text.

```kotlin
data class FontStyle(
    val fontFamily: Typeface? = null,
    val textColor: Int? = null,
    val textSize: Float? = null,
    val style: Int = Typeface.NORMAL
)
```

| Property | Description |
|----------|-------------|
| `fontFamily` | `Typeface` to apply (use `FontManager` helpers) |
| `textColor` | Optional `Int` color |
| `textSize` | Optional text size in sp |
| `style` | `Typeface.NORMAL`, `Typeface.BOLD`, etc. |

---

### `FontManager`

Loads `Typeface` objects from font resources. Provides prebuilt helpers for the Inter font family.

```kotlin
class FontManager(private val context: Context)
```

| Method / Extension | Description |
|--------------------|-------------|
| `loadFont(@FontRes fontRes: Int)` | Loads a font by resource ID |
| `loadFontByName(fontName: String)` | Loads a font by resource name |
| `interBlack()` | Inter Black |
| `interBold()` | Inter Bold |
| `interSemiBold()` | Inter SemiBold |
| `interMedium()` | Inter Medium |
| `interRegular()` | Inter Regular |

#### FontStyle Convenience Builders

```kotlin
fontManager.mediumStyle(color)    // Inter Medium + bold flag
fontManager.semiBoldStyle(color)  // Inter SemiBold + bold flag
fontManager.boldStyle(color)      // Inter Bold + bold flag
fontManager.strongStyle(color)    // Inter Black + bold flag
```

---

## Installation

Add the Material Components dependency to `build.gradle`:

```gradle
implementation 'com.google.android.material:material:1.9.0'
```

Ensure Inter font files are present in `res/font/`:

```
res/font/
├── inter.xml
├── inter_medium.xml
├── inter_semibold.xml
├── inter_bold.xml
└── inter_black.xml
```

---

## Usage

### Basic Usage

Use the `TextView.setHtmlText()` extension for a zero-config setup with semibold `<b>` support:

```kotlin
textView.setHtmlText(
    "<b>Hello</b>, this is a list:<ul><li>Item A</li><li>Item B</li></ul>",
    context
)
```

This internally creates a `FontManager` and configures `<myb>` (remapped from `<b>`) with semibold Inter.

---

### Custom Configuration

```kotlin
val fontManager = FontManager(context)

val config = HtmlRendererConfig(
    unorderedStyle = ListStyles.arrow.withColor(Color.DKGRAY).withIndent(12),
    orderedStyle = ListStyles.parenthesized.withColor("#1565C0".toColorInt()),
    fontStyles = mapOf(
        "myb" to fontManager.boldStyle("#D32F2F".toColorInt()),
        "strong" to fontManager.strongStyle()
    )
)

val renderer = HtmlRenderer(config, fontManager)
textView.text = renderer.render(htmlString, textView)
```

---

### Applying TextView Layout Config

Use `applyHtmlConfig` to set line spacing, text size, and padding from `HtmlListConfig`:

```kotlin
val config = HtmlRendererConfig(
    listConfig = HtmlListConfig(lineSpacing = 1.4f, textSizeSp = 14f, padding = 8)
)
textView.applyHtmlConfig(config.listConfig)
textView.renderHtml(htmlString, HtmlRenderer(config, fontManager))
```

---

### Custom Tag Mappings

Tag can be remapped with any additional HTML tags before rendering:

```kotlin
val config = HtmlRendererConfig(
    customTagMappings = mapOf(
        "<em>" to "<i>",
        "</em>" to "</i>"
    )
)
```

---

## Tag Preprocessing

Before the HTML is passed to Android's `Html.fromHtml()`, the renderer automatically remaps the following tags to avoid conflicts with the native parser:

| Original Tag | Custom Tag |
|-------------|------------|
| `<ul>` / `</ul>` | `<myul>` / `</myul>` |
| `<ol>` / `</ol>` | `<myol>` / `</myol>` |
| `<li>` / `</li>` | `<myli>` / `</myli>` |
| `<b>` / `</b>` | `<myb>` / `</myb>` |

> **Important:** When defining `fontStyles` in the config, use the **remapped** tag names (e.g. `"myb"` instead of `"b"`).

---

## `Editable` Extension Functions

Low-level span helpers used internally by `TagHandler`. Can be used directly if needed to build custom span logic.

| Function | Description |
|----------|-------------|
| `applyIndentSpan(start, end, indent)` | Applies `LeadingMarginSpan.Standard` |
| `applyBulletSpan(start, end, gap)` | Applies `BulletSpan` |
| `applyOrderedSpan(char, textSize, start, end, gap)` | Applies `OrderedSpan` with a bullet character or number |
| `applyListColors(start, end, style)` | Applies foreground and/or background color from `ListStyle` |

---

## `TextView` Extension Functions

| Function | Description |
|----------|-------------|
| `applyHtmlConfig(config: HtmlListConfig)` | Sets line spacing, text size, and padding |
| `renderHtml(html: String, renderer: HtmlRenderer)` | Renders HTML and assigns it to `text` |
| `setHtmlText(value: CharSequence?, context: Context)` | All-in-one: creates config, renderer, and sets text |
| `dpToPx(dp: Int): Int` | Converts dp to pixels using the view's display metrics |

---

## Custom Span: `TypefaceSpan`

A `MetricAffectingSpan` that applies a custom `Typeface` to both drawing and measurement passes. This is the recommended way to apply custom fonts (e.g. Inter) as spans in Android, since the framework's built-in `TypefaceSpan` only accepts font family name strings.

```kotlin
class TypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
    override fun updateDrawState(ds: TextPaint) { ds.typeface = typeface }
    override fun updateMeasureState(paint: TextPaint) { paint.typeface = typeface }
}
```

---

## Dependencies

```gradle
implementation 'com.google.android.material:material:1.9.0'
```

Requires Inter font resources under `res/font/` (see [Installation](#installation)).