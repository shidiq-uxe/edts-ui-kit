package id.co.edtslib.uikit.core.textbadge

import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorRes

data class BadgePalette(
    @ColorRes val backgroundColorRes: Int? = null,
    @ColorRes val textColorRes: Int? = null,
    @ColorRes val iconColorRes: Int? = null,
    @ColorRes val borderColorRes: Int? = null,
    @ColorRes val gradientColors: IntArray? = null,
    val gradientOrientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT
)
