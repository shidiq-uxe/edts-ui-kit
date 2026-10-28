package id.co.edtslib.uikit.recipes.linearprogressbar.poinku

import android.graphics.drawable.GradientDrawable
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.linearprogressbar.LinearProgressBarVariant

object PoinkuLinearProgressBarVariant : LinearProgressBarVariant() {
    override val trackThickness: Float = 4f
    override val trackCornerRadius: Float? = null
    override val trackPadding: Float = 0f
    override val indicatorPadding: Float = 0f

    override val trackColorRes: Int = R.color.poinku_color_neutral_white
    override val indicatorGradient: IntArray? = intArrayOf(
        R.color.poinku_color_progress_bar_light_blue,
        R.color.poinku_color_progress_bar_dark_blue
    )
    override val gradientOrientation: GradientDrawable.Orientation = GradientDrawable.Orientation.BL_TR
    override val disabledTrackColorRes: Int = R.color.poinku_color_grey_20
    override val disabledIndicatorColorRes: Int = R.color.poinku_color_grey_50

    override val showThumb: Boolean = false
    override val thumbColorRes: Int = R.color.poinku_color_primary_blue_30
    override val thumbGradient: IntArray? = indicatorGradient
    override val disabledThumbColorRes: Int = R.color.poinku_color_grey_50

    override val completedColorRes: Int = R.color.poinku_color_primary_blue_10
    override val disabledCompletedColorRes: Int = R.color.poinku_color_grey_30

    override val showInnerShadow: Boolean = true
    override val innerShadowColorRes: Int = R.color.poinku_color_progress_bar_black_opacity
    override val disabledInnerShadowColorRes: Int = R.color.poinku_color_progress_bar_black_opacity
    override val innerShadowOffsetX: Float = -0.5f
    override val innerShadowOffsetY: Float = -0.5f
    override val innerShadowBlur: Float = 4f
    override val innerShadowSpread: Float = 1f

    override val showBadge: Boolean = false

    override val animationDurationMs: Long = 800L
    override val animationDelayMs: Long = 300L
}