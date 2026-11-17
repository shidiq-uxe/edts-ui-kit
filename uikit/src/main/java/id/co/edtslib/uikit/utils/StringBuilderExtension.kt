package id.co.edtslib.uikit.utils

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.html.TypefaceSpan


/**
 * Builds a `SpannedString` from a full message, applying custom text styles and click actions
 * to specified highlighted substrings.
 *
 * This function scans the `message` for each string in `highlightedMessages`, applies a
 * corresponding text style from `highlightedTextAppearance` (or a default bold if not provided),
 * and assigns a click action from `highlightClickAction` to each highlighted segment.
 *
 * Any non-highlighted part of the message will be styled using the optional `defaultTextAppearance`.
 *
 * Example usage:
 * ```
 * val result = buildHighlightedMessage(
 *     context = context,
 *     message = "Click here to login or register",
 *     highlightedMessages = listOf("login", "register"),
 *     highlightedTextAppearance = listOf(loginStyle, registerStyle),
 *     highlightClickAction = listOf({ loginClick() }, { registerClick() })
 * )
 * textView.text = result
 * textView.movementMethod = LinkMovementMethod.getInstance()
 * ```
 *
 * @param context The context used to access resources for styling and color.
 * @param message The full text message that contains one or more highlightable substrings.
 * @param defaultTextAppearance Optional text style to apply to non-highlighted parts of the message.
 * @param highlightedMessages A list of substrings that should be styled and made clickable.
 * @param highlightedTextAppearance A list of `TextStyle` to apply to each highlighted substring.
 *        If not enough styles are provided, a default bold colored style is used.
 * @param highlightClickAction A list of lambdas (click listeners) triggered when each highlighted
 *        substring is clicked. If not specified, default no-op lambdas are used.
 *
 * @return A `SpannedString` with highlighted and clickable sections.
 *
 * @see id.co.edtslib.uikit.utils.TextStyle
 */
fun buildHighlightedMessage(
    context: Context,
    message: String,
    includeUnderline: Boolean = false,
    defaultTextAppearance: TextStyle? = null,
    highlightedMessages: List<String>,
    highlightedTextAppearance: List<TextStyle> = emptyList(),
    highlightClickAction: List<(View) -> Unit> = List(highlightedMessages.size) { {} }
): SpannedString {
    return buildSpannedString {
        var remainingMessage = message

        for ((currentIndex, highlightedMessage) in highlightedMessages.withIndex()) {
            val beforeHighlight = remainingMessage.substringBefore(highlightedMessage)

            defaultTextAppearance?.let { textStyle ->
                applyTextAppearanceSpan(context, textStyle) { append(beforeHighlight) }
            } ?: append(beforeHighlight)

            if (currentIndex < highlightedTextAppearance.size) {
                val textAppearance = highlightedTextAppearance[currentIndex]

                applyTextAppearanceSpan(context, textAppearance) {
                    if (includeUnderline.not()) {
                        noUnderlineClick(onClick = {
                            highlightClickAction[currentIndex].invoke(it)
                        }) {
                            append(highlightedMessage)
                        }
                    } else {
                        click (onClick = {
                            highlightClickAction[currentIndex].invoke(it)
                        }) {
                            append(highlightedMessage)
                        }
                    }
                }
            } else {
                bold {
                    color(context.color(R.color.primary_30)) {
                        if (includeUnderline.not()) {
                            noUnderlineClick(onClick = {
                                highlightClickAction[currentIndex].invoke(it)
                            }) {
                                append(highlightedMessage)
                            }
                        } else {
                            click(onClick = {
                                highlightClickAction[currentIndex].invoke(it)
                            }) {
                                append(highlightedMessage)
                            }
                        }
                    }
                }
            }

            remainingMessage = remainingMessage.substringAfter(highlightedMessage)
        }

        defaultTextAppearance?.let { textStyle ->
            applyTextAppearanceSpan(context, textStyle) { append(remainingMessage) }
        } ?: append(remainingMessage)
    }
}

/**
 * Applies the given typeface to HTML emphasis ranges while retaining the other parsed spans.
 * Omit the size to inherit the TextView's paragraph size.
 */
internal fun buildHighlightedMessage(
    parsedMessage: Spanned,
    emphasisTypeface: Typeface,
    emphasisSizePx: Int? = null
): SpannedString {
    val boldSpans = parsedMessage.getSpans(0, parsedMessage.length, StyleSpan::class.java)
        .filter { it.style and Typeface.BOLD != 0 }
    if (boldSpans.isEmpty()) return SpannedString.valueOf(parsedMessage)

    val styled = SpannableStringBuilder(parsedMessage)
    val emphasisRanges = boldSpans.map { span ->
        val start = styled.getSpanStart(span)
        val end = styled.getSpanEnd(span)
        val flags = styled.getSpanFlags(span)
        styled.removeSpan(span)

        if (start < end && span.style and Typeface.ITALIC != 0) {
            styled.setSpan(StyleSpan(Typeface.ITALIC), start, end, flags)
        }
        Triple(start, end, flags)
    }

    emphasisRanges.forEach { (start, end, flags) ->
        if (start < end) {
            styled.setSpan(TypefaceSpan(emphasisTypeface, preserveItalic = true), start, end, flags)
            emphasisSizePx?.let { styled.setSpan(AbsoluteSizeSpan(it), start, end, flags) }
        }
    }

    return SpannedString(styled)
}
