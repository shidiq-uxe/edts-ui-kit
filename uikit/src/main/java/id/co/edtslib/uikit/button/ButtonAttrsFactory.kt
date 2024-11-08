package id.co.edtslib.uikit.button

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import androidx.annotation.StyleRes
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
        val styledAttributes = themedContext.obtainStyledAttributes(styleRes, R.styleable.MaterialButton)

        with(button) {
            backgroundTintList = styledAttributes.getColorStateList(R.styleable.MaterialButton_backgroundTint)
            iconTint = styledAttributes.getColorStateList(R.styleable.MaterialButton_iconTint)
            rippleColor = styledAttributes.getColorStateList(R.styleable.MaterialButton_rippleColor)
            strokeColor = styledAttributes.getColorStateList(R.styleable.MaterialButton_strokeColor)
            strokeWidth = styledAttributes.getDimensionPixelSize(R.styleable.MaterialButton_strokeWidth, strokeWidth)
            insetTop = styledAttributes.getDimensionPixelSize(R.styleable.MaterialButton_android_insetTop, insetTop)
            insetBottom = styledAttributes.getDimensionPixelSize(R.styleable.MaterialButton_android_insetBottom, insetBottom)
            // insetLeft = styledAttributes.getDimensionPixelSize(R.styleable.MaterialButton_android_insetLeft, insetLeft)
            // insetRight = styledAttributes.getDimensionPixelSize(R.styleable.MaterialButton_android_insetRight, insetRight)
            iconPadding = styledAttributes.getDimensionPixelSize(R.styleable.MaterialButton_iconPadding, iconPadding)
            iconSize = styledAttributes.getDimensionPixelSize(R.styleable.MaterialButton_iconSize, iconSize)
            cornerRadius = styledAttributes.getDimensionPixelSize(R.styleable.MaterialButton_cornerRadius, cornerRadius)

            val shapeAppearanceResId = styledAttributes.getResourceId(R.styleable.MaterialButton_shapeAppearance, 0)
            val shapeAppearanceOverlayResId = styledAttributes.getResourceId(R.styleable.MaterialButton_shapeAppearanceOverlay, 0)
            if (shapeAppearanceResId != 0) {
                shapeAppearanceModel = ShapeAppearanceModel.builder(themedContext, shapeAppearanceResId, shapeAppearanceOverlayResId).build()
            }
        }

        styledAttributes.recycle()

        if (attrs != null) {
            themedContext.obtainStyledAttributes(styleRes, intArrayOf(
                android.R.attr.padding,
                android.R.attr.textAppearance,
                android.R.attr.textColor
            )).use { paddingAttributes ->
                paddingAttributes.getDimensionPixelSize(0, -1).takeIf { it != -1 }?.let { padding ->
                    button.setPadding(padding, padding, padding, padding)
                }

                paddingAttributes.getResourceId(1, -1).takeIf { it != -1 }?.let { textAppearanceResId ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        button.setTextAppearance(textAppearanceResId)
                    } else {
                        button.setTextAppearance(themedContext, textAppearanceResId)
                    }
                }

                paddingAttributes.getColorStateList(2)?.let { textColor ->
                    button.setTextColor(textColor)
                }
            }
        }
    }
}

