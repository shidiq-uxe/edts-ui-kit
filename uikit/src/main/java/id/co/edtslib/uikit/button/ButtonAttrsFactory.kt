package id.co.edtslib.uikit.button

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import androidx.annotation.StyleRes
import androidx.core.content.withStyledAttributes
import androidx.core.view.updatePadding
import com.google.android.material.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.ShapeAppearanceModel

// Todo : Refactor needed ASAP, Since some of MaterialButton Attrs is flagged as private.
object ButtonAttrsFactory {

    @SuppressLint("ResourceType", "PrivateResource")
    fun applyAttributes(
        context: Context,
        button: MaterialButton,
        @StyleRes styleRes: Int,
        attrs: AttributeSet? = null
    ) {
        val themedContext = ContextThemeWrapper(context, styleRes)
        themedContext.withStyledAttributes(
            set = attrs,
            attrs = R.styleable.MaterialButton,
            defStyleAttr = 0,
            defStyleRes = styleRes
        ) {
            with(button) {
                backgroundTintList = getColorStateList(R.styleable.MaterialButton_backgroundTint)
                iconTint = getColorStateList(R.styleable.MaterialButton_iconTint)
                rippleColor = getColorStateList(R.styleable.MaterialButton_rippleColor)
                strokeColor = getColorStateList(R.styleable.MaterialButton_strokeColor)
                strokeWidth = getDimensionPixelSize(R.styleable.MaterialButton_strokeWidth, strokeWidth)
                insetTop = getDimensionPixelSize(R.styleable.MaterialButton_android_insetTop, insetTop)
                insetBottom = getDimensionPixelSize(R.styleable.MaterialButton_android_insetBottom, insetBottom)
                iconPadding = getDimensionPixelSize(R.styleable.MaterialButton_iconPadding, iconPadding)
                iconSize = getDimensionPixelSize(R.styleable.MaterialButton_iconSize, iconSize)
                cornerRadius = getDimensionPixelSize(R.styleable.MaterialButton_cornerRadius, cornerRadius)

                val shapeAppearanceResId = getResourceId(R.styleable.MaterialButton_shapeAppearance, 0)
                val shapeAppearanceOverlayResId = getResourceId(R.styleable.MaterialButton_shapeAppearanceOverlay, 0)
                if (shapeAppearanceResId != 0) {
                    shapeAppearanceModel = ShapeAppearanceModel.builder(themedContext, shapeAppearanceResId, shapeAppearanceOverlayResId).build()
                }
            }
        }

        if (attrs != null) {
            val backgroundTintAttr = intArrayOf(android.R.attr.backgroundTint)
            context.withStyledAttributes(attrs, backgroundTintAttr, 0, 0) {
                if (hasValue(0)) {
                    button.backgroundTintList = getColorStateList(0)
                }
            }

        }


        themedContext.withStyledAttributes(
            set = attrs,
            attrs = intArrayOf(
                android.R.attr.textAppearance,
                android.R.attr.textColor,
                android.R.attr.padding,
                android.R.attr.paddingLeft,
                android.R.attr.paddingTop,
                android.R.attr.paddingRight,
                android.R.attr.paddingBottom,
                R.attr.iconSize
            ),
            defStyleAttr = 0,
            defStyleRes = styleRes
        ) {
            getResourceId(0, -1).takeIf { it != -1 }?.let { textAppearanceResId ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    button.setTextAppearance(textAppearanceResId)
                } else {
                    button.setTextAppearance(themedContext, textAppearanceResId)
                }
            }

            getColorStateList(1)?.let { textColor ->
                button.setTextColor(textColor)
            }

            getDimensionPixelSize(2, -1).takeIf { it != -1 }?.let { padding ->
                button.setPadding(padding, padding, padding, padding)
            }

            getDimensionPixelSize(3, -1).takeIf { it != -1 }?.let { padding ->
                button.updatePadding(left = padding)
            }

            getDimensionPixelSize(4, -1).takeIf { it != -1 }?.let { padding ->
                button.updatePadding(top = padding)
            }

            getDimensionPixelSize(5, -1).takeIf { it != -1 }?.let { padding ->
                button.updatePadding(right = padding)
            }

            getDimensionPixelSize(6, -1).takeIf { it != -1 }?.let { padding ->
                button.updatePadding(bottom = padding)
            }

            getDimensionPixelSize(7, -1).takeIf { it != -1 }?.let { iconSize ->
                button.iconSize = iconSize
            }
        }
    }
}