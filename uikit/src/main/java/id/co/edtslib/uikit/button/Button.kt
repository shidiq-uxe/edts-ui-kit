package id.co.edtslib.uikit.button

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.ColorUtils
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.button.CoreButton

@Deprecated("Use KlikButton or PoinkuButton with an explicit XML recipe. See docs/Button.md for migration.")
class Button @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = R.attr.buttonStyle,
) : CoreButton(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr) {

    var buttonType: ButtonType = ButtonType.FILLED
        set(value) {
            field = value

            applyStyle(value)
        }

    init {
        if (attrs != null) {
            context.withStyledAttributes(
                attrs,
                R.styleable.Button,
                defStyleAttr,
                0
            ) {
                buttonType = ButtonType.values()[getInt(R.styleable.Button_buttonType, buttonType.ordinal)]
                shouldShowShimmer = getBoolean(R.styleable.Button_shouldShowShimmer, shouldShowShimmer)
                pressedScale = getFloat(R.styleable.Button_pressedScale, pressedScale)
                isDestructive = getBoolean(R.styleable.Button_isDestructive, false)
            }
        }
    }

    private fun applyDefaultRipple() {
        val enabledState = intArrayOf(android.R.attr.state_enabled)
        val bgColor = backgroundTintList
            ?.getColorForState(enabledState, backgroundTintList?.defaultColor ?: Color.TRANSPARENT)
            ?: Color.TRANSPARENT

        if (bgColor == Color.WHITE || bgColor == Color.TRANSPARENT) {
            rippleColor = ColorStateList.valueOf(
                ColorUtils.setAlphaComponent(currentTextColor, RIPPLE_ALPHA)
            )
        } else {
            ContextThemeWrapper(context, buttonType.styleRes).withStyledAttributes(
                set = null,
                attrs = com.google.android.material.R.styleable.MaterialButton,
                defStyleAttr = 0,
                defStyleRes = buttonType.styleRes
            ) {
                getColorStateList(com.google.android.material.R.styleable.MaterialButton_rippleColor)
                    ?.let { rippleColor = it }
            }
        }
    }

    private fun applyStyle(buttonType: ButtonType) {
        ButtonAttrsFactory.applyAttributes(
            context = context,
            button = this,
            styleRes = buttonType.styleRes,
            attrs = attrs
        )

        applyDefaultRipple()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        applyDefaultRipple()
    }

    enum class ButtonType(val styleRes: Int) {
        FILLED(R.style.Widget_EDTS_UIKit_Button_Filled_Primary_Small),
        FILLED_MEDIUM(R.style.Widget_EDTS_UIKit_Button_Filled_Primary_Medium),
        FILLED_LARGE(R.style.Widget_EDTS_UIKit_Button_Filled_Primary_Large),
        SECONDARY(R.style.Widget_EDTS_UIKit_Button_Outlined_Secondary_Small),
        SECONDARY_MEDIUM(R.style.Widget_EDTS_UIKit_Button_Outlined_Secondary_Medium),
        SECONDARY_LARGE(R.style.Widget_EDTS_UIKit_Button_Outlined_Secondary_Large),
        TERTIARY(R.style.Widget_EDTS_UIKit_Button_Outlined_Tertiary_Small),
        TERTIARY_MEDIUM(R.style.Widget_EDTS_UIKit_Button_Outlined_Tertiary_Medium),
        TERTIARY_LARGE(R.style.Widget_EDTS_UIKit_Button_Outlined_Tertiary_Large),
        TEXT(R.style.Widget_EDTS_UIKit_Button_TextButton_Small),
        TEXT_MEDIUM(R.style.Widget_EDTS_UIKit_Button_TextButton_Medium),
        TEXT_LARGE(R.style.Widget_EDTS_UIKit_Button_TextButton_Large),
    }

    companion object {
        private const val RIPPLE_ALPHA = 30
    }
}
