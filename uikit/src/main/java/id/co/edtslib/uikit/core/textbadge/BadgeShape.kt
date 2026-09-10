package id.co.edtslib.uikit.core.textbadge

import id.co.edtslib.uikit.R

abstract class BadgeShape(
    open val name: String
) {
    abstract val topLeftRadius: Float
    abstract val topRightRadius: Float
    abstract val bottomLeftRadius: Float
    abstract val bottomRightRadius: Float

    open val disabledPalette: BadgePalette = BadgePalette(
        backgroundColorRes = R.color.foundation_color_grey_50,
        textColorRes = R.color.foundation_color_neutral_white,
        iconColorRes = R.color.foundation_color_neutral_white,
        borderColorRes = R.color.foundation_color_grey_50
    )
}

class DefaultShape : BadgeShape("default") {
    override val topLeftRadius = 4f
    override val topRightRadius = 4f
    override val bottomLeftRadius = 4f
    override val bottomRightRadius = 4f

    override val disabledPalette = BadgePalette(
        backgroundColorRes = R.color.foundation_color_grey_40,
        textColorRes = R.color.foundation_color_neutral_white,
        iconColorRes = R.color.foundation_color_neutral_white,
        borderColorRes = R.color.foundation_color_grey_40
    )
}
