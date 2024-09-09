package id.co.edtslib.uikit.utils

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.SpannedString
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import id.co.edtslib.uikit.R

fun buildHighlightedMessage(
    context: Context,
    message: String,
    highlightedTextAppearance: List<TextStyle> = emptyList(),
    highlightedMessages: List<String>,
    highlightClickAction: List<() -> Unit> = List(highlightedMessages.size) { {} }
): SpannedString {
    return buildSpannedString {
        var remainingMessage = message

        // Iterate over each highlightedMessage
        for ((currentIndex, highlightedMessage) in highlightedMessages.withIndex()) {
            // Find the position of each highlighted message in the main message
            val beforeHighlight = remainingMessage.substringBefore(highlightedMessage)
            append(beforeHighlight)

            if (currentIndex < highlightedTextAppearance.size) {
                val textAppearance = highlightedTextAppearance[currentIndex]

                // Apply text appearance to highlighted message
                applyTextAppearanceSpan(context, textAppearance) {
                    noUnderlineClick(onClick = {
                        highlightClickAction[currentIndex].invoke()
                    }) {
                        append(highlightedMessage)
                    }
                }
            } else {
                // Append highlighted message without custom appearance if not enough styles provided
                bold {
                    color(context.color(R.color.primary_30)) {
                        noUnderlineClick(onClick = {
                            highlightClickAction[currentIndex].invoke()
                        }) {
                            append(highlightedMessage)
                        }
                    }
                }
            }

            remainingMessage = remainingMessage.substringAfter(highlightedMessage)
        }

        // Append any remaining message after the last highlight
        append(remainingMessage)
    }
}
