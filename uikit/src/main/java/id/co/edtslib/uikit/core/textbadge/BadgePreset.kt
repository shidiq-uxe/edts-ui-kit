package id.co.edtslib.uikit.core.textbadge

import androidx.annotation.StyleRes
import id.co.edtslib.uikit.R

data class BadgePreset(
    val horizontalPadding: Int = 0,
    val verticalPadding: Int = 0,
    val leftPadding: Int? = null,
    val rightPadding: Int? = null,
    val topPadding: Int? = null,
    val bottomPadding: Int? = null,
    val iconSize: Int = 12,
    @StyleRes val textAppearance: Int = R.style.TextAppearance_Inter_SemiBold_B4,
    val drawablePadding: Int = R.dimen.dimen_2,
    val borderWidth: Int = 0
)