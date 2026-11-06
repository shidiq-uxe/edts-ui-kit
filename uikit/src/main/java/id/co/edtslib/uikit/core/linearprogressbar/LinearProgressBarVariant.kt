package id.co.edtslib.uikit.core.linearprogressbar

import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import id.co.edtslib.uikit.R

abstract class LinearProgressBarVariant {

    open val trackThickness: Float = 6f
    open val trackCornerRadius: Float? = null
    open val trackPadding: Float = 0f
    open val indicatorPadding: Float = 1f

    @get:ColorRes open val trackColorRes: Int = R.color.black_20
    @get:ColorRes open val indicatorColorRes: Int = R.color.primary_30
    @get:ColorRes open val indicatorGradient: IntArray? = null
    open val gradientOrientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT
    @get:ColorRes open val disabledTrackColorRes: Int = R.color.black_20
    @get:ColorRes open val disabledIndicatorColorRes: Int = R.color.black_40

    open val showThumb: Boolean = false
    @get:DimenRes open val thumbSizeRes: Int = R.dimen.xxs
    @get:ColorRes open val thumbColorRes: Int = R.color.primary_30
    @get:ColorRes open val thumbGradient: IntArray? = null
    @get:ColorRes open val disabledThumbColorRes: Int = R.color.black_40

    @get:ColorRes open val completedColorRes: Int = R.color.primary_10
    @get:ColorRes open val disabledCompletedColorRes: Int = R.color.black_30

    open val showInnerShadow: Boolean = false
    @get:ColorRes open val innerShadowColorRes: Int = R.color.progress_bar_black_opacity
    @get:ColorRes open val disabledInnerShadowColorRes: Int = R.color.progress_bar_black_opacity
    open val innerShadowOffsetX: Float = 1f
    open val innerShadowOffsetY: Float = 2f
    open val innerShadowBlur: Float = 5f
    open val innerShadowSpread: Float = 0f

    open val showBadge: Boolean = false

    open val animationDurationMs: Long = 800L
    open val animationDelayMs: Long = 300L
}

object DefaultLinearProgressBarVariant : LinearProgressBarVariant()