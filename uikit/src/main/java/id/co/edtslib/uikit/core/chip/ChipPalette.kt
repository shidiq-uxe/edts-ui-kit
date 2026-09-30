package id.co.edtslib.uikit.core.chip

import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorRes

data class ChipPalette(
    @ColorRes val backgroundColorRes: Int? = null,
    @ColorRes val textColorRes: Int? = null,
    @ColorRes val startIconColorRes: Int? = null,
    @ColorRes val endIconColorRes: Int? = null,
    @ColorRes val borderColorRes: Int? = null,
    @ColorRes val startIconBackgroundColorRes: Int? = null,
    @ColorRes val endIconBackgroundColorRes: Int? = null,
    val isStartIconBackgroundVisible: Boolean = false,
    val isEndIconBackgroundVisible: Boolean = false,
    val startIconPaddingDp: Int? = null,
    val endIconPaddingDp: Int? = null,
    @ColorRes val gradientColors: IntArray? = null,
    val gradientOrientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT
)