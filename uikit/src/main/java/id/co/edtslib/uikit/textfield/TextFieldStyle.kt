package id.co.edtslib.uikit.textfield

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import androidx.annotation.StyleRes
import androidx.core.content.res.use
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.ShapeAppearanceModel
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.drawable

enum class TextFieldStyle(
    val styleResId: Int,
    val editTextStyleResId: Int = R.style.Widget_EDTS_UIKit_TextInputEditText_LabelInside
) {
    LABEL_INSIDE(R.style.Widget_EDTS_UIKit_TextInputLayout_LabelInside),
    FLAT_START(R.style.Widget_EDTS_UIKit_TextInputLayout_LabelInside_Flat_Start),
    FLAT_END(R.style.Widget_EDTS_UIKit_TextInputLayout_LabelInside_Flat_End),
    WITHOUT_LABEL(R.style.Widget_EDTS_UIKit_TextInputLayout_WithoutLabel),
    OTP(R.style.Widget_EDTS_UIKit_TextInputLayout_Otp)
}

internal object TextFieldAttrsFactory {
    @SuppressLint("ResourceType", "PrivateResource")
    fun applyAttributes(
        context: Context,
        textField: TextField,
        @StyleRes styleRes: Int,
        @StyleRes editTextStyleResId: Int,
        attrs: AttributeSet? = null
    ) {
        val themedContext = ContextThemeWrapper(context, styleRes)
        val styledAttributes = themedContext.obtainStyledAttributes(styleRes, com.google.android.material.R.styleable.TextInputLayout)

        with(textField) {
            boxBackgroundColor = styledAttributes.getColor(com.google.android.material.R.styleable.TextInputLayout_boxBackgroundColor, boxBackgroundColor)
            boxStrokeWidth = styledAttributes.getDimensionPixelSize(com.google.android.material.R.styleable.TextInputLayout_boxStrokeWidth, boxStrokeWidth)
            boxStrokeWidthFocused = styledAttributes.getDimensionPixelSize(com.google.android.material.R.styleable.TextInputLayout_boxStrokeWidthFocused, boxStrokeWidthFocused)
            setCounterOverflowTextAppearance(styledAttributes.getResourceId(com.google.android.material.R.styleable.TextInputLayout_counterTextAppearance, R.style.TextAppearance_Inter_Regular_B3))
            counterOverflowTextColor = styledAttributes.getColorStateList(com.google.android.material.R.styleable.TextInputLayout_counterOverflowTextColor)
            setCounterTextAppearance(styledAttributes.getResourceId(com.google.android.material.R.styleable.TextInputLayout_counterTextAppearance, R.style.TextAppearance_Inter_Regular_B3))
            counterTextColor = styledAttributes.getColorStateList(com.google.android.material.R.styleable.TextInputLayout_counterTextColor)
            setEndIconTintList(styledAttributes.getColorStateList(com.google.android.material.R.styleable.TextInputLayout_endIconTint))
            errorIconDrawable = styledAttributes.getDrawable(com.google.android.material.R.styleable.TextInputLayout_errorIconDrawable)
            setErrorTextAppearance(styledAttributes.getResourceId(com.google.android.material.R.styleable.TextInputLayout_errorTextAppearance, R.style.TextAppearance_Inter_Regular_Error))
            isExpandedHintEnabled = styledAttributes.getBoolean(com.google.android.material.R.styleable.TextInputLayout_expandedHintEnabled, isExpandedHintEnabled)
            setHelperTextTextAppearance(styledAttributes.getResourceId(com.google.android.material.R.styleable.TextInputLayout_helperTextTextAppearance, R.style.TextAppearance_Inter_Regular_B3))
            setHintTextAppearance(styledAttributes.getResourceId(com.google.android.material.R.styleable.TextInputLayout_hintTextAppearance, R.style.TextAppearance_Inter_Semibold_H3))
            hintTextColor = styledAttributes.getColorStateList(com.google.android.material.R.styleable.TextInputLayout_hintTextColor)
            // Todo : android:textColorHint
            setPlaceholderTextAppearance(styledAttributes.getResourceId(com.google.android.material.R.styleable.TextInputLayout_placeholderTextAppearance, R.style.TextAppearance_Inter_Regular_B1))
            placeholderTextColor = styledAttributes.getColorStateList(com.google.android.material.R.styleable.TextInputLayout_placeholderTextColor)
            setPrefixTextAppearance(styledAttributes.getResourceId(com.google.android.material.R.styleable.TextInputLayout_prefixTextAppearance, R.style.TextAppearance_Inter_Regular_B1))
            styledAttributes.getColorStateList(com.google.android.material.R.styleable.TextInputLayout_prefixTextColor)?.let { setPrefixTextColor(it) }
            setStartIconTintList(styledAttributes.getColorStateList(com.google.android.material.R.styleable.TextInputLayout_startIconTint))
            setSuffixTextAppearance(styledAttributes.getResourceId(com.google.android.material.R.styleable.TextInputLayout_suffixTextAppearance, R.style.TextAppearance_Inter_Regular_B1))
            styledAttributes.getColorStateList(com.google.android.material.R.styleable.TextInputLayout_suffixTextColor)?.let { setSuffixTextColor(it) }
            boxCollapsedPaddingTop = styledAttributes.getDimensionPixelSize(com.google.android.material.R.styleable.TextInputLayout_boxCollapsedPaddingTop, boxCollapsedPaddingTop)

            /*val shapeAppearanceResId = styledAttributes.getResourceId(com.google.android.material.R.styleable.MaterialButton_shapeAppearance, 0)
            val shapeAppearanceOverlayResId = styledAttributes.getResourceId(com.google.android.material.R.styleable.MaterialButton_shapeAppearanceOverlay, 0)
            if (shapeAppearanceResId != 0) {
                shapeAppearanceModel = ShapeAppearanceModel.builder(themedContext, shapeAppearanceResId, shapeAppearanceOverlayResId).build()
            }*/
        }

        styledAttributes.recycle()

        if (attrs != null) {
            themedContext.obtainStyledAttributes(editTextStyleResId, intArrayOf(
                android.R.attr.background,
                android.R.attr.maxLines,
                android.R.attr.textAppearance
            )).use { editTextStyleAttributes ->
                editTextStyleAttributes.getResourceId(0, -1).takeIf { it != -1 }?.let { backgroundResource ->
                    textField.editText?.background = context.drawable(backgroundResource)
                }

                editTextStyleAttributes.getInteger(1, -1).takeIf { it != -1 }?.let { maxLines ->
                    textField.editText?.maxLines = maxLines
                }

                editTextStyleAttributes.getResourceId(1, -1).takeIf { it != -1 }?.let { textAppearanceResId ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        textField.editText?.setTextAppearance(textAppearanceResId)
                    } else {
                        textField.editText?.setTextAppearance(themedContext, textAppearanceResId)
                    }
                }
            }
        }
    }
}
