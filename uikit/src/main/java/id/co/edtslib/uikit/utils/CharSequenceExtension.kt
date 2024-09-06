package id.co.edtslib.uikit.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LineHeightSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.text.inSpans
import id.co.edtslib.uikit.R
import kotlin.math.roundToInt

// Extension function to apply a TextAppearanceSpan since TextAppearanceSpan itself doesn't supported
fun SpannableStringBuilder.applyH2TextAppearanceSpan(
    context: Context,
    color: Int = context.color(R.color.primary_30),
    action: SpannableStringBuilder.() -> Unit
) {
    val primaryColorSpan = ForegroundColorSpan(color)
    val textSizeSpan = AbsoluteSizeSpan(context.dimenPixelSize(R.dimen.h2_text_size) ?: 14.dp.roundToInt())
    val semiBoldStyleSpan = StyleSpan(context.font(R.font.inter_semibold)!!.style)

    inSpans(primaryColorSpan, textSizeSpan, semiBoldStyleSpan) {
        action()
    }
}

// Extension function to apply a TextAppearanceSpan since TextAppearanceSpan itself doesn't supported
fun SpannableStringBuilder.applyH3TextAppearanceSpan(
    context: Context,
    color: Int = context.color(R.color.primary_30),
    action: SpannableStringBuilder.() -> Unit
) {
    val primaryColorSpan = ForegroundColorSpan(color)
    val textSizeSpan = AbsoluteSizeSpan(context.dimenPixelSize(R.dimen.h3_text_size) ?: 12.dp.roundToInt())
    val semiBoldStyleSpan = StyleSpan(context.font(R.font.inter_semibold)!!.style)

    inSpans(primaryColorSpan, textSizeSpan, semiBoldStyleSpan) {
        action()
    }
}

private fun SpannableStringBuilder.setLineHeight(lineHeightPx: Int) {
    val span = LineHeightSpan { text, start, end, spanstartv, v, fm ->
        fm.descent += lineHeightPx - (fm.descent - fm.ascent)
        fm.bottom += lineHeightPx - (fm.descent - fm.ascent)
    }
    setSpan(span, 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}

fun SpannableStringBuilder.applyH1TextAppearance(
    context: Context,
    color: Int = context.color(R.color.black_70),
    action: SpannableStringBuilder.() -> Unit
) {
    val textSize = context.resources.getDimensionPixelSize(R.dimen.h1_text_size)
    val lineHeight = context.resources.getDimensionPixelSize(R.dimen.dimen_18)
    val styleSpan = StyleSpan(context.font(R.font.inter_bold)!!.style)

    val colorSpan = ForegroundColorSpan(color)
    val textSizeSpan = AbsoluteSizeSpan(textSize)

    inSpans(colorSpan, textSizeSpan, styleSpan) {
        action()
    }

    setLineHeight(lineHeight)
}

fun SpannableStringBuilder.applyB1TextAppearance(
    context: Context,
    color: Int = context.color(R.color.black_70),
    action: SpannableStringBuilder.() -> Unit
) {
    val textSize = context.resources.getDimensionPixelSize(R.dimen.b1_text_size)
    val lineHeight = context.resources.getDimensionPixelSize(R.dimen.dimen_18)
    val styleSpan = StyleSpan(context.font(R.font.inter)!!.style)

    val colorSpan = ForegroundColorSpan(color)
    val textSizeSpan = AbsoluteSizeSpan(textSize)

    inSpans(colorSpan, textSizeSpan, styleSpan) {
        action()
    }

    setLineHeight(lineHeight)
}

fun SpannableStringBuilder.applyP1TextAppearance(
    context: Context,
    color: Int = context.color(R.color.black_70),
    action: SpannableStringBuilder.() -> Unit
) {
    val textSize = context.resources.getDimensionPixelSize(R.dimen.b2_text_size)
    val lineHeight = context.resources.getDimensionPixelSize(R.dimen.dimen_21)
    val styleSpan = StyleSpan(context.font(R.font.inter)!!.style)

    val colorSpan = ForegroundColorSpan(color)
    val textSizeSpan = AbsoluteSizeSpan(textSize)

    inSpans(colorSpan, textSizeSpan, styleSpan) {
        action()
    }

    setLineHeight(lineHeight)
}

fun SpannableStringBuilder.applyErrorTextAppearance(
    context: Context,
    color: Int = context.color(R.color.red_30),
    action: SpannableStringBuilder.() -> Unit
) {
    val textSize = context.resources.getDimensionPixelSize(R.dimen.b3_text_size)
    val lineHeight = context.resources.getDimensionPixelSize(R.dimen.dimen_18)
    val styleSpan = StyleSpan(context.font(R.font.inter)!!.style)

    val colorSpan = ForegroundColorSpan(color)
    val textSizeSpan = AbsoluteSizeSpan(textSize)

    inSpans(colorSpan, textSizeSpan, styleSpan) {
        action()
    }

    setLineHeight(lineHeight)
}

enum class TextStyle {
    H2, H1, B1, P1, ERROR;
}






