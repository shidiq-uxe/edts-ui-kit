package id.co.edtslib.uikit.button

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.view.doOnLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.attachShimmerEffect
import id.co.edtslib.uikit.utils.detachShimmerEffect

class Button @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = R.attr.buttonStyle,
) : MaterialButton(context, attrs, defStyleAttr) {

    // Only call this variable only when shouldShowShimmer is true
    var shimmerFrameLayout: ShimmerFrameLayout? = null

    var shouldShowShimmer = false
        set(value) {
            field = value

            doOnLayout {
                if (value) {
                    shimmerFrameLayout = this.attachShimmerEffect()
                } else {
                    shimmerFrameLayout?.detachShimmerEffect()
                }
            }
        }

    var buttonType: ButtonType = ButtonType.FILLED
        set(value) {
            field = value

            applyStyle(value)
        }

    var pressedScale = DEFAULT_SCALE_VALUE

    init {
        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.Button, defStyleAttr, 0).use {
                buttonType = ButtonType.values()[it.getInt(R.styleable.Button_buttonType, buttonType.ordinal)]
                shouldShowShimmer = it.getBoolean(R.styleable.Button_shouldShowShimmer, shouldShowShimmer)
                pressedScale = it.getFloat(R.styleable.Button_pressedScale, pressedScale)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { motionEvent ->
            if (isEnabled) {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        this.animate().scaleX(pressedScale).scaleY(pressedScale).setDuration(100).start()
                    }
                    MotionEvent.ACTION_UP -> {
                        this.animate().scaleX(RESET_SCALE_VALUE).scaleY(RESET_SCALE_VALUE).setDuration(100).start()
                        performClick()
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        this.animate().scaleX(RESET_SCALE_VALUE).scaleY(RESET_SCALE_VALUE).setDuration(100).start()
                    }

                    else -> {}
                }
            }
        }

        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onDetachedFromWindow() {
        shimmerFrameLayout = null

        super.onDetachedFromWindow()
    }

    private fun applyStyle(buttonType: ButtonType) {
        ButtonAttrsFactory.applyAttributes(
            context = context,
            button = this,
            styleRes = buttonType.styleRes,
            attrs = attrs
        )
    }

    enum class ButtonType(val styleRes: Int) {
        FILLED(R.style.Widget_EDTS_UIKit_Button_Filled),
        FILLED_MEDIUM(R.style.Widget_EDTS_UIKit_Button_Filled_Medium),
        SECONDARY(R.style.Widget_EDTS_UIKit_Button_Outlined_Secondary),
        SECONDARY_MEDIUM(R.style.Widget_EDTS_UIKit_Button_Outlined_Secondary_Medium),
        VARIANT(R.style.Widget_EDTS_UIKit_Button_Filled_Variant),
        VARIANT_MEDIUM(R.style.Widget_EDTS_UIKit_Button_Filled_Variant_Medium),
        TEXT(R.style.Widget_EDTS_UIKit_Button_TextButton),
        TEXT_MEDIUM(R.style.Widget_EDTS_UIKit_Button_TextButton_Medium),
    }

    companion object {
        private const val DEFAULT_SCALE_VALUE = 0.95f
        private const val RESET_SCALE_VALUE = 1f
    }
}