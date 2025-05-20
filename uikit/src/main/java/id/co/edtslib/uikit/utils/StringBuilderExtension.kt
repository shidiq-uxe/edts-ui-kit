package id.co.edtslib.uikit.utils

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import id.co.edtslib.uikit.R


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
