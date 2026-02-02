package id.co.edtslib.uikit.button

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.withStyledAttributes
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewMultilineFloatingActionButtonBinding
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.inflater

class MultiLineFloatingActionButton @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = com.google.android.material.R.attr.cardViewStyle,
): MaterialCardView(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    private val binding = ViewMultilineFloatingActionButtonBinding.inflate(context.inflater, this, true)

    // Todo : Color State List Support

    var buttonColor = context.color(R.color.button_cart_fab_default)
        set(value) {
            field = value
            setButtonColor(value)
        }

    var cornerSize = context.dimen(R.dimen.xxxs)
        set(value) {
            field = value
            setCornerSize(value)
        }

    var iconTint = context.color(R.color.black_70)
        set(value) {
            field = value
            if (binding.ivIcon.drawable != null) {
                binding.ivIcon.imageTintList = ColorStateList.valueOf(value)
            }
        }

    var icon = context.drawable(R.drawable.ic_cart).apply { setTint(iconTint) }.mutate()
        set(value) {
            field = value
            binding.ivIcon.setImageDrawable(value)
        }

    var titleText = ""
        set(value) {
            field = value
            binding.tvTitle.text = value
        }

    var supportingText = ""
        set(value) {
            field = value
            binding.tvSupporting.text = value
        }

    var highlightText = ""
        set(value) {
            field = value
            binding.tvHighlightTitle.text = value
        }

    var supportingHighlightText = ""
        set(value) {
            field = value
            binding.tvHighlightSupporting.text = value
        }


    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        return FloatingActionButton.Behavior(context, attrs)
    }

    init {
        setupProperties()
        initAttributes()
    }


    private fun setupProperties() {
        setButtonColor()
        setCornerSize()
        setStroke()
    }

    private fun initAttributes() {
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.MultiLineFloatingActionButton, 0,0) {
                buttonColor = this.getColor(
                    R.styleable.MultiLineFloatingActionButton_buttonColor,
                    buttonColor
                )

                cornerSize = this.getDimension(
                    R.styleable.MultiLineFloatingActionButton_cornerSize,
                    cornerSize
                )

                titleText = this.getString(
                    R.styleable.MultiLineFloatingActionButton_titleText
                ) ?: titleText

                supportingText = this.getString(
                    R.styleable.MultiLineFloatingActionButton_supportingText
                ) ?: supportingText

                highlightText = this.getString(
                    R.styleable.MultiLineFloatingActionButton_highlightText
                ) ?: highlightText
            }
        }
    }

    @JvmName("mlfab_buttonColor")
    fun setButtonColor(@ColorInt color: Int = buttonColor) {
        setCardBackgroundColor(color)
    }

    @JvmName("mlfab_cornerSize")
    fun setCornerSize(size: Float = cornerSize) {
        shapeAppearanceModel = shapeAppearanceModel.toBuilder()
            .setAllCornerSizes(size)
            .build()
    }


    @JvmName("mlfab_stroke")
    fun setStroke(
        width: Float = 0f,
        @ColorInt color: Int = Color.TRANSPARENT
    ) {
        strokeColor = color
        strokeWidth = width.toInt()

    }

}