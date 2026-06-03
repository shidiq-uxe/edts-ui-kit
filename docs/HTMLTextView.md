# HTML TextView

`HtmlTextView` is a custom `AppCompatTextView` that renders HTML content using a configurable styling system. It acts as a high-level wrapper around `HtmlRenderer`, automatically handling tag preprocessing, typography.

---

## Features

* **XML-Based Styling**: Configure headings, paragraphs, and inline tags (`<b>`, `<i>`, etc.) using `TextAppearance`.
* **Custom List Rendering**: Supports `<ul>` and `<ol>` with configurable bullets, numbering, spacing, and indentation.
* **Font Resolution from Styles**: Extracts `textSize`, `textColor`, `fontFamily`, and `textStyle` from `TextAppearance`.
* **Seamless Integration**: Works directly in XML or programmatically via `setHtmlText()`.

---

## Architecture

This component acts as a bridge between XML styling and the rendering engine:

| Component            | Responsibility                                           |
| -------------------- | -------------------------------------------------------- |
| `HtmlTextView`       | Entry point view that parses attributes and renders HTML |
| `HtmlStyleConfig`    | Holds all `TextAppearance` and list styling references   |
| `HtmlRenderer`       | Performs actual HTML parsing and span rendering          |
| `FontManager`        | Resolves font resources into `Typeface`                  |
| `HtmlRendererConfig` | Aggregates all rendering configuration                   |

---

## `HtmlTextView`

### Overview

Custom `TextView` that:

* Parses XML attributes into styling config
* Prepares `HtmlRenderer`
* Intercepts `setText()` to render HTML automatically

```kotlin
class HtmlTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.htmlTextViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr)
```

---

### Key Responsibilities

* Parse XML attributes into `HtmlStyleConfig` and `HtmlListConfig`
* Build `FontStyle` mappings dynamically
* Preprocess HTML tags before rendering
* Delegate rendering to `HtmlRenderer`
* Maintain original raw HTML text

---

### Core Methods

| Method                      | Description                                             |
| --------------------------- | ------------------------------------------------------- |
| `setHtmlText(html: String)` | Sets and renders HTML content                           |
| `getHtmlText(): String`     | Returns the raw HTML string                             |
| `setText(text, type)`       | Overrides default behavior to render HTML               |
| `onFinishInflate()`         | Ensures XML-defined text is rendered                    |
| `preprocessHtmlTags()`      | Converts standard tags (`<h1>`, `<p>`) into custom tags |

---

### Rendering Flow

1. XML attributes are parsed into config objects
2. `HtmlRenderer` is initialized
3. Input text is stored as raw HTML
4. HTML is preprocessed (`<h1>` → `<myh1>`)
5. Renderer converts HTML into `Spanned`
6. List rendering is internally composed from multiple span types coordinated by [`TagHandler`](#taghandler), including indentation, numbering/bullets, and color styling.
7. Result is applied to the `TextView`

Unsupported or unmapped tags are safely ignored while preserving rendering stack consistency internally.

---

## XML Attributes

### Typography

| Attribute                               | Description     |
| --------------------------------------- | --------------- |
| `h1TextAppearance` ... `h6TextAppearance` | Heading styles  |
| `pTextAppearance`                       | Paragraph style |
| `bTextAppearance`                       | Bold text       |
| `strongTextAppearance`                  | Strong text |
| `iTextAppearance`                       | Italic text     |
| `emTextAppearance`                      | Emphasis        |
| `uTextAppearance`                       | Underline       |
| `aTextAppearance`                       | Link styling    |

---

### List Styling

| Attribute         | Description                     | Default          |
| ----------------- | ------------------------------- | ---------------- |
| `bulletChar`      | Character for unordered lists   | `"•"`            |
| `bulletColor`     | Bullet text color               | `TextView color` |
| `bulletGap`       | Space between bullet and text   | `8dp`            |
| `orderedFormat`   | Number format (`String.format`) | `"%d."`          |
| `orderedColor`    | Ordered list text color         | `TextView color` |
| `listIndent`      | Indentation for list items      | `16dp`           |
| `htmlLineSpacing` | Line spacing multiplier         | `1.15`           |
| `htmlPadding`     | Padding applied to view         | `0`              |

---

## Tag Stack Handling

The rendering system internally uses stack-based tag tracking to correctly handle nested HTML structures and overlapping styles.

This applies not only to list containers (`<ul>`, `<ol>`, `<li>`), but also typography and inline formatting tags such as `<b>`, `<strong>`, `<i>`, `<em>`, `<u>`, Headings (`<h1>` ... `<h6>`), and Paragraphs (`<p>`)

The stack mechanism ensures spans are applied to the correct text range even when tags are deeply nested or combined.

Example:

```html id="3fvbcu"
<p>
    Normal text 
    <b>
        Bold text <i>Bold + Italic</i>
    </b>
</p>
```

In this case, `TagHandler` internally maintains dedicated stacks for:

* List hierarchy management
* Ordered-list counters
* Font/style tag ranges

Implementation details and span application behavior can be referred from the internal [`TagHandler`](#taghandler) class and related rendering utilities.


---

## Usage

### XML Usage

```xml
<id.co.edtslib.uikit.textview.HtmlTextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="<h1>Hello</h1><p>This is HTML</p>"
    app:h1TextAppearance="@style/TextAppearance.Heading1"
    app:pTextAppearance="@style/TextAppearance.Body"
    app:bulletChar="→"
    app:listIndent="16dp"/>
```

---

### Programmatic Usage

```kotlin
htmlTextView.setHtmlText("""
    <h1>Title</h1>
    <p>This is a paragraph</p>
    <ul>
        <li>Item A</li>
        <li>Item B</li>
    </ul>
""".trimIndent())
```

---

### Nested Tag / Stack Rendering Example


```kotlin id="n6x2de"
htmlTextView.setHtmlText("""
    <h2>Nested Formatting Example</h2>

    <p>
        Normal paragraph text with
        <b>bold text</b>,
        <i>italic text</i>,
        and
        <b>
            nested bold with
            <i>italic inside bold</i>
        </b>.
    </p>

    <p>
        You can also combine
        <u>underline</u>,
        <em>emphasis</em>,
        and nested formatting safely.
    </p>
""".trimIndent())
```