# Highlighted messages

Use `buildHighlightedMessage` for inline emphasis and actions. It returns a `SpannedString`
that can be assigned to a TextView or `AlertBox.text`.

## Backend HTML

Pass unescaped HTML to the `htmlMessage` overload. Bold and strong ranges use the supplied
`TextStyle`, including the actual typeface and weight. Other supported HTML spans, entities,
line breaks, and links are retained. Repeated text is highlighted at its original markup
positions, rather than by searching for the first matching word.

```kotlin
import id.co.edtslib.uikit.utils.TextStyleKey
import id.co.edtslib.uikit.utils.TextStyleProvider.get
import id.co.edtslib.uikit.utils.buildHighlightedMessage

alertBox.text = buildHighlightedMessage(
    context = context,
    htmlMessage = "Sudah ambil <strong>2</strong> tebus murah",
    highlightedTextAppearance = TextStyleKey.P2_SEMIBOLD.get(context, R.color.black_70),
)
```

`defaultTextAppearance` is optional. When omitted, the TextView's existing appearance styles
the ordinary text. Emphasis alone does not create click targets. HTML links retain URL spans;
enable the TextView's appropriate movement method if those links should be interactive.

The parser supports Android's `HtmlCompat` markup rather than arbitrary HTML/CSS or embedded
web content. HTML image tags require a separate image-loading implementation.

For ordered lists or custom tags, parse through the shared `HtmlRenderer` first, then pass
its `Spanned` result to the `parsedMessage` overload. Numbering, hanging indents, colors,
links, and other renderer spans survive while bold/strong ranges receive the same exact
typeface handling:

```kotlin
textView.text = buildHighlightedMessage(
    context = context,
    parsedMessage = renderer.render(html, textView),
    highlightedTextAppearance = TextStyleKey.P1_SEMIBOLD.get(context, R.color.black_70),
)
```

The HTML-string and legacy plain-text overloads retain their existing signatures.
Custom `OrderedSpan` markers are adapted to drawing-only leading-margin delegates in this
new path, preventing Android accessibility from parceling unsupported custom framework span
IDs. Their drawing and hanging indents are retained. Persist the HTML/model and render it
again after recreation rather than relying on framework serialization of custom markers.

## Inline actions from plain text

The existing `message` / `highlightedMessages` overload remains available for text with
explicit click callbacks. Supply highlighted strings in occurrence order, with matching
styles and callbacks. Missing or empty highlighted strings are ignored without duplicating
the message.

The shared typography span loads fonts through `context.font()`, preserving bundled fallback
behavior and the actual weight instead of reducing a font to a bold/normal flag.
