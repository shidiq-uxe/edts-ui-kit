package id.co.edtslib.uikit.recipes.linearprogressbar.klik

import android.graphics.drawable.GradientDrawable
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.linearprogressbar.LinearProgressBarVariant

object KlikLinearProgressBarVariant : LinearProgressBarVariant() {

    override val trackThickness: Float = 6f
    override val trackCornerRadius: Float? = null
    override val trackPadding: Float = 0f
    override val indicatorPadding: Float = 1f

    override val trackColorRes: Int = R.color.black_20
    override val indicatorColorRes: Int = R.color.primary_30
    override val indicatorGradient: IntArray? = intArrayOf(
        R.color.progress_bar_light_blue,
        R.color.progress_bar_dark_blue
    )
    override val gradientOrientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT
    override val disabledTrackColorRes: Int = R.color.black_20
    override val disabledIndicatorColorRes: Int = R.color.black_40

    override val showThumb: Boolean = true
    override val thumbSizeRes: Int = R.dimen.xxs
    override val thumbColorRes: Int = R.color.primary_30
    override val thumbGradient: IntArray? = indicatorGradient
    override val disabledThumbColorRes: Int = R.color.black_40

    override val completedColorRes: Int = R.color.primary_10
    override val disabledCompletedColorRes: Int = R.color.black_30

    override val showInnerShadow: Boolean = true
    override val innerShadowColorRes: Int = R.color.progress_bar_black_opacity
    override val disabledInnerShadowColorRes: Int = R.color.progress_bar_black_opacity
    override val innerShadowOffsetX: Float = 1f
    override val innerShadowOffsetY: Float = 2f
    override val innerShadowBlur: Float = 5f
    override val innerShadowSpread: Float = 0f

    override val showBadge: Boolean = false

    override val animationDurationMs: Long = 800L
    override val animationDelayMs: Long = 300L
}