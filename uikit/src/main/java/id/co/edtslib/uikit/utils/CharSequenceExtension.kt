package id.co.edtslib.uikit.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LineHeightSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorInt
import androidx.annotation.FontRes
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
    val styleSpan = StyleSpan(context.font(textStyle.font)!!.style)

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


sealed class TextStyle(
    open val context: Context,
    open val color: Int,
    @FontRes open val font: Int,
    open val textSize: Float,
    open val lineHeight: Float
) {
    // Display Typography
    data class D1(
        override val context: Context,
        @ColorInt override val color: Int,
        @FontRes override val font: Int,
        override val textSize: Float,
        override val lineHeight: Float
    ) : TextStyle(context, color, font, textSize, lineHeight)

    data class D2(
        override val context: Context,
        @ColorInt override val color: Int,
        @FontRes override val font: Int,
        override val textSize: Float,
        override val lineHeight: Float
    ) : TextStyle(context, color, font, textSize, lineHeight)

    // Heading Typography
    data class H1(
        override val context: Context,
        @ColorInt override val color: Int,
        @FontRes override val font: Int,
        override val textSize: Float,
        override val lineHeight: Float
    ) : TextStyle(context, color, font, textSize, lineHeight)

    data class H2(
        override val context: Context,
        @ColorInt override val color: Int,
        @FontRes override val font: Int,
        override val textSize: Float,
        override val lineHeight: Float
    ) : TextStyle(context, color, font, textSize, lineHeight)

    data class H3(
        override val context: Context,
        @ColorInt override val color: Int,
        @FontRes override val font: Int,
        override val textSize: Float,
        override val lineHeight: Float
    ) : TextStyle(context, color, font, textSize, lineHeight)

    data class H4(
        override val context: Context,
        @ColorInt override val color: Int,
        @FontRes override val font: Int,
        override val textSize: Float,
        override val lineHeight: Float
    ) : TextStyle(context, color, font, textSize, lineHeight)

    // Body Typography
    data class B1(
        override val context: Context,
        @ColorInt override val color: Int,
        @FontRes override val font: Int,
        override val textSize: Float,
        override val lineHeight: Float
    ) : TextStyle(context, color, font, textSize, lineHeight)

    data class B2(
        override val context: Context,
        @ColorInt override val color: Int,
        @FontRes override val font: Int,
        override val textSize: Float,
        override val lineHeight: Float
    ) : TextStyle(context, color, font, textSize, lineHeight)

    data class B3(
        override val context: Context,
        @ColorInt override val color: Int,
        @FontRes override val font: Int,
        override val textSize: Float,
        override val lineHeight: Float
    ) : TextStyle(context, color, font, textSize, lineHeight)

    data class B4(
        override val context: Context,
        @ColorInt override val color: Int,
        @FontRes override val font: Int,
        override val textSize: Float,
        override val lineHeight: Float
    ) : TextStyle(context, color, font, textSize, lineHeight)

    // Paragraph Typography
    data class P1(
        override val context: Context,
        @ColorInt override val color: Int,
        @FontRes override val font: Int,
        override val textSize: Float,
        override val lineHeight: Float
    ) : TextStyle(context, color, font, textSize, lineHeight)

    // Error Typography
    data class Error(
        override val context: Context,
        @ColorInt override val color: Int,
        @FontRes override val font: Int,
        override val textSize: Float,
        override val lineHeight: Float
    ) : TextStyle(context, color, font, textSize, lineHeight)

    // Other Typography
    data class Other(
        override val context: Context,
        @ColorInt override val color: Int,
        @FontRes override val font: Int,
        override val textSize: Float,
        override val lineHeight: Float
    ) : TextStyle(context, color, font, textSize, lineHeight)

    companion object {
        // Display Typography
        fun d1Style(context: Context) = D1(
            context = context,
            color = context.color(R.color.black_50),
            font = R.font.inter_semibold,
            textSize = context.resources.getDimension(R.dimen.d1_text_size),
            lineHeight = context.resources.getDimension(R.dimen.dimen_30)
        )

        fun d2Style(context: Context) = D2(
            context = context,
            color = context.color(R.color.black_50),
            font = R.font.inter_semibold,
            textSize = context.resources.getDimension(R.dimen.d2_text_size),
            lineHeight = context.resources.getDimension(R.dimen.dimen_26)
        )

        // Heading Typography
        fun h1Style(context: Context) = H1(
            context = context,
            color = context.color(R.color.black_50),
            font = R.font.inter_bold,
            textSize = context.resources.getDimension(R.dimen.h1_text_size),
            lineHeight = context.resources.getDimension(R.dimen.dimen_18)
        )

        fun h2Style(context: Context) = H2(
            context = context,
            color = context.color(R.color.black_50),
            font = R.font.inter_semibold,
            textSize = context.resources.getDimension(R.dimen.h2_text_size),
            lineHeight = context.resources.getDimension(R.dimen.dimen_16)
        )

        fun h3Style(
            context: Context,
            color: Int = context.color(R.color.black_50),
            fontFamily: Int = R.font.inter_semibold,
        ) = H3(
            context = context,
            color = color,
            font = fontFamily,
            textSize = context.resources.getDimension(R.dimen.h3_text_size),
            lineHeight = context.resources.getDimension(R.dimen.dimen_14)
        )

        fun h4Style(context: Context) = H4(
            context = context,
            color = context.color(R.color.black_50),
            font = R.font.inter_semibold,
            textSize = context.resources.getDimension(R.dimen.h4_text_size),
            lineHeight = context.resources.getDimension(R.dimen.dimen_14)
        )

        // Body Typography
        fun b1Style(context: Context) = B1(
            context = context,
            color = context.color(R.color.black_50),
            font = R.font.inter,
            textSize = context.resources.getDimension(R.dimen.b1_text_size),
            lineHeight = context.resources.getDimension(R.dimen.dimen_18)
        )

        fun b2Style(context: Context) = B2(
            context = context,
            color = context.color(R.color.black_50),
            font = R.font.inter,
            textSize = context.resources.getDimension(R.dimen.b2_text_size),
            lineHeight = context.resources.getDimension(R.dimen.dimen_16)
        )

        fun b3Style(
            context: Context,
            color: Int = context.color(R.color.black_50),
        ) = B3(
            context = context,
            color = color,
            font = R.font.inter,
            textSize = context.resources.getDimension(R.dimen.b3_text_size),
            lineHeight = context.resources.getDimension(R.dimen.dimen_16)
        )

        fun b4Style(context: Context) = B4(
            context = context,
            color = context.color(R.color.black_50),
            font = R.font.inter,
            textSize = context.resources.getDimension(R.dimen.b3_text_size),
            lineHeight = context.resources.getDimension(R.dimen.dimen_14)
        )

        // Paragraph Typography
        fun p1Style(context: Context) = P1(
            context = context,
            color = context.color(R.color.black_50),
            font = R.font.inter,
            textSize = context.resources.getDimension(R.dimen.b2_text_size),
            lineHeight = context.resources.getDimension(R.dimen.dimen_21)
        )

        // Error Typography
        fun errorStyle(context: Context) = Error(
            context = context,
            color = context.color(R.color.red_30),
            font = R.font.inter,
            textSize = context.resources.getDimension(R.dimen.b3_text_size),
            lineHeight = context.resources.getDimension(R.dimen.dimen_18)
        )

        // Other Typography
        fun otherStyle(context: Context) = Other(
            context = context,
            color = context.color(R.color.primary_20),
            font = R.font.inter,
            textSize = context.resources.getDimension(R.dimen.b3_text_size),
            lineHeight = context.resources.getDimension(R.dimen.dimen_18)
        )
    }
}







