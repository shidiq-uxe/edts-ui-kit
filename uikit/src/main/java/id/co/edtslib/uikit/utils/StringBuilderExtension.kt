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

// Helper function to apply a specific TextAppearanceSpan
fun SpannableStringBuilder.applyTextAppearanceSpan(
    context: Context,
    textStyle: TextStyle? = null,
    builderAction: SpannableStringBuilder.() -> Unit
) {
    when(textStyle) {
        TextStyle.H2 -> applyH2TextAppearanceSpan(context) {
            builderAction.invoke(this)
        }
        TextStyle.H1 -> applyH1TextAppearance(context) {
            builderAction.invoke(this)
        }
        TextStyle.B1 -> applyB1TextAppearance(context) {
            builderAction.invoke(this)
        }
        TextStyle.P1 -> applyP1TextAppearance(context) {
            builderAction.invoke(this)
        }
        TextStyle.ERROR -> applyErrorTextAppearance(context) {
            builderAction.invoke(this)
        }
        null -> {}
    }
}
