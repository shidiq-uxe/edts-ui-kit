package id.co.edtslib.uikit.list

import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.px

class ListOptions {
    
    var titleText: String? = null
    @StyleRes
    var titleTextAppearance: Int = R.style.TextAppearance_Inter_Semibold_H2
    @ColorInt
    var titleTextColor: Int? = Color.BLACK
    var isTitleVisible: Boolean = true

    var subtitleText: String? = null
    @StyleRes
    var subtitleTextAppearance: Int = R.style.TextAppearance_Inter_Regular_B2
    @ColorInt
    var subtitleTextColor: Int? = Color.BLACK
    var isSubtitleVisible: Boolean = false

    var startIcon: Drawable? = null
    var startIconTint: Int? = null
    var isStartIconVisible: Boolean = false
    var startIconSize: Float? = null
    var startIconGravity: Int = IconGravity.CENTER.ordinal

    var endIcon: Drawable? = null
    var endIconTint: Int? = null
    var isEndIconVisible: Boolean = false
    var endIconSize: Float? = null
    var endIconGravity: Int = IconGravity.CENTER.ordinal

    var cornerRadius: Float = 4.px
    var topLeftCornerRadius: Float = 0.px
    var topRightCornerRadius: Float = 0.px
    var bottomLeftCornerRadius: Float = 0.px
    var bottomRightCornerRadius: Float = 0.px

    var strokeWidth: Float = 1.px
    @ColorInt
    var strokeColor: Int? = null

    @ColorInt
    var backgroundTint: Int = Color.WHITE

    var cardElevation: Float = 0.px


    enum class IconGravity {
        TOP, CENTER, BOTTOM
    }
}
