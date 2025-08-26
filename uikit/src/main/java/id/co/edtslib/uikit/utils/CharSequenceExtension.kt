package id.co.edtslib.uikit.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LineHeightSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.inSpans
import id.co.edtslib.uikit.R
import kotlin.math.roundToInt

fun SpannableStringBuilder.applyTextAppearanceSpan(
    context: Context,
    textStyle: TextStyle,
    action: SpannableStringBuilder.() -> Unit
) {
    val colorSpan = ForegroundColorSpan(textStyle.color)
    val textSizeSpan = AbsoluteSizeSpan(textStyle.textSize.roundToInt())

    // Todo : Fallback Style if not found
    val style = try {
        val tf = ResourcesCompat.getFont(context, textStyle.font)
        tf?.style ?: Typeface.NORMAL
    } catch (e: Exception) {
        Typeface.NORMAL
    }

    val styleSpan = StyleSpan(style)

    inSpans(colorSpan, textSizeSpan, styleSpan) {
        action()
    }

    setLineHeight(textStyle.lineHeight.roundToInt())
}

private fun SpannableStringBuilder.setLineHeight(lineHeightPx: Int) {
    val span = LineHeightSpan { _, _, _, _, v, fm ->
        fm.descent += lineHeightPx - (fm.descent - fm.ascent)
        fm.bottom += lineHeightPx - (fm.descent - fm.ascent)
    }
    setSpan(span, 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}

data class TextStyle(
    val color: Int,
    @FontRes val font: Int,
    val textSize: Float,
    val lineHeight: Float
)

enum class TextStyleKey {
    D1_SEMIBOLD, D2_SEMIBOLD, D3_SEMIBOLD, D4_SEMIBOLD,
    H1_SEMIBOLD, H2_SEMIBOLD, H3_SEMIBOLD,
    B1_SEMIBOLD, B1, B2_BOLD, B2_MEDIUM, B2, B3_BOLD, B3_SEMIBOLD, B3_MEDIUM, B3, B4_BOLD, B4_SEMIBOLD, B4_MEDIUM, B4,
    P1_SEMIBOLD, P1, P2_SEMIBOLD, P2,
    BUTTON_EXTRA_LARGE, BUTTON_LARGE, BUTTON_MEDIUM, BUTTON_SMALL,
    ERROR, ERROR_BLACK
}


object TextStyleProvider {
    fun TextStyleKey.get(
        context: Context,
        @ColorRes colorRes: Int = R.color.black_50
    ): TextStyle {
        return when (this) {
            TextStyleKey.D1_SEMIBOLD -> create(context, colorRes, R.font.inter_semibold, R.dimen.d1_text_size, R.dimen.dimen_44)
            TextStyleKey.D2_SEMIBOLD -> create(context, colorRes, R.font.inter_semibold, R.dimen.d2_text_size, R.dimen.dimen_28)
            TextStyleKey.D3_SEMIBOLD -> create(context, colorRes, R.font.inter_semibold, R.dimen.d3_text_size, R.dimen.dimen_26)
            TextStyleKey.D4_SEMIBOLD -> create(context, colorRes, R.font.inter_semibold, R.dimen.d4_text_size, R.dimen.dimen_22)

            TextStyleKey.H1_SEMIBOLD -> create(context, colorRes, R.font.inter_semibold, R.dimen.h1_text_size, R.dimen.dimen_18)
            TextStyleKey.H2_SEMIBOLD -> create(context, colorRes, R.font.inter_semibold, R.dimen.h2_text_size, R.dimen.dimen_16)
            TextStyleKey.H3_SEMIBOLD -> create(context, colorRes, R.font.inter_semibold, R.dimen.h3_text_size, R.dimen.dimen_14)

            TextStyleKey.B1_SEMIBOLD -> create(context, colorRes, R.font.inter_semibold, R.dimen.b1_text_size, R.dimen.dimen_18)
            TextStyleKey.B1 -> create(context, colorRes, R.font.inter, R.dimen.b1_text_size, R.dimen.dimen_18)

            TextStyleKey.B2_BOLD -> create(context, colorRes, R.font.inter_bold, R.dimen.b2_text_size, R.dimen.dimen_16)
            TextStyleKey.B2_MEDIUM -> create(context, colorRes, R.font.inter_medium, R.dimen.b2_text_size, R.dimen.dimen_16)
            TextStyleKey.B2 -> create(context, colorRes, R.font.inter, R.dimen.b2_text_size, R.dimen.dimen_16)

            TextStyleKey.B3_BOLD -> create(context, colorRes, R.font.inter_bold, R.dimen.b3_text_size, R.dimen.dimen_16)
            TextStyleKey.B3_SEMIBOLD -> create(context, colorRes, R.font.inter_semibold, R.dimen.b3_text_size, R.dimen.dimen_16)
            TextStyleKey.B3_MEDIUM -> create(context, colorRes, R.font.inter_medium, R.dimen.b3_text_size, R.dimen.dimen_16)
            TextStyleKey.B3 -> create(context, colorRes, R.font.inter, R.dimen.b3_text_size, R.dimen.dimen_16)

            TextStyleKey.B4_BOLD -> create(context, colorRes, R.font.inter_bold, R.dimen.b4_text_size, R.dimen.dimen_14)
            TextStyleKey.B4_SEMIBOLD -> create(context, colorRes, R.font.inter_semibold, R.dimen.b4_text_size, R.dimen.dimen_14)
            TextStyleKey.B4_MEDIUM -> create(context, colorRes, R.font.inter_medium, R.dimen.b4_text_size, R.dimen.dimen_14)
            TextStyleKey.B4 -> create(context, colorRes, R.font.inter, R.dimen.b4_text_size, R.dimen.dimen_14)

            TextStyleKey.P1_SEMIBOLD -> create(context, colorRes, R.font.inter_semibold, R.dimen.p1_text_size, R.dimen.dimen_20)
            TextStyleKey.P1 -> create(context, colorRes, R.font.inter, R.dimen.p1_text_size, R.dimen.dimen_20)
            TextStyleKey.P2_SEMIBOLD -> create(context, colorRes, R.font.inter_semibold, R.dimen.p2_text_size, R.dimen.dimen_16)
            TextStyleKey.P2 -> create(context, colorRes, R.font.inter, R.dimen.p2_text_size, R.dimen.dimen_16)

            TextStyleKey.BUTTON_EXTRA_LARGE -> create(context, colorRes, R.font.inter_semibold, R.dimen.button_text_extra_large, R.dimen.dimen_24)
            TextStyleKey.BUTTON_LARGE -> create(context, colorRes, R.font.inter_semibold, R.dimen.button_text_large, R.dimen.dimen_24)
            TextStyleKey.BUTTON_MEDIUM -> create(context, colorRes, R.font.inter_semibold, R.dimen.button_text_medium, R.dimen.dimen_16)
            TextStyleKey.BUTTON_SMALL -> create(context, colorRes, R.font.inter_semibold, R.dimen.button_text_small, R.dimen.dimen_16)

            TextStyleKey.ERROR -> create(context, R.color.red_30, R.font.inter_semibold, R.dimen.b3_text_size, R.dimen.dimen_14)
            TextStyleKey.ERROR_BLACK -> create(context, colorRes, R.font.inter_semibold, R.dimen.b3_text_size, R.dimen.dimen_14)
        }
    }


    fun create(
        context: Context,
        @ColorRes color: Int,
        @FontRes font: Int,
        @DimenRes size: Int,
        @DimenRes height: Int
    ): TextStyle {
        return TextStyle(
            color = context.color(color),
            font = font,
            textSize = context.resources.getDimension(size),
            lineHeight = context.resources.getDimension(height)
        )
    }

}







