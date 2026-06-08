package id.co.edtslib.uikit.badge

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.font

class TextBadge @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr) {

    enum class BadgeShape {
        STATUS,
        HIGHLIGHT
    }

    enum class BadgeSize {
        SMALL,
        MEDIUM,
        LARGE
    }

    var badgeShape: BadgeShape = BadgeShape.STATUS
        set(value) {
            field = value
            when (value) {
                BadgeShape.STATUS -> {
                    setCornerRadius(4f, 4f, 4f, 4f)
                    setBadgePadding(horizontal = 4, vertical = 1)
                }
                BadgeShape.HIGHLIGHT  -> {
                    setCornerRadius(0f, 0f, 0f, 8f)
                    setBadgePadding(horizontal = 4, vertical = 2)
                }
            }
            refreshAppearance()
        }

    var badgeSize: BadgeSize = BadgeSize.MEDIUM
        set(value) {
            field = value
            when (value) {
                BadgeSize.SMALL -> {
                    iconSize = 10
                    badgeTextAppearance = R.style.TextAppearance_Inter_SemiBold_TextBadge
                    setBadgePadding(horizontal = 4, vertical = 2)
                }
                BadgeSize.MEDIUM -> {
                    iconSize = 12
                    badgeTextAppearance = R.style.TextAppearance_Inter_SemiBold_B4
                    setBadgePadding(horizontal = 4, vertical = 1)
                }
                BadgeSize.LARGE -> {
                    iconSize = 12
                    badgeTextAppearance = R.style.TextAppearance_Inter_SemiBold_B4
                    setBadgePadding(horizontal = 4, vertical = 3)
                }
            }
        }

    var text: String? = "Text Badge"
        set(value) {
            field = value
            textView.text = value
            visibility = if (value.isNullOrEmpty()) GONE else VISIBLE
            requestLayout()
        }

    @ColorInt
    var textColor = context.color(R.color.warning_primary)
        set(value) {
            field = value
            applyTextStyle()
            refreshAppearance()
        }

    @ColorInt
    var badgeColor = context.color(R.color.warning_background)
        set(value) {
            field = value
            gradientDrawable = null
            refreshAppearance()
        }

    @StyleRes
    var badgeTextAppearance = R.style.TextAppearance_Inter_SemiBold_B4
        set(value) {
            field = value
            applyTextStyle()
        }

    var fontFamily = context.font(R.font.inter_semibold)
        set(value) {
            field = value
            textView.typeface = value
        }

    var iconVisible = false
        set(value) {
            field = value
            updateIcon()
        }

    var iconSize: Int = 12
        set(value) {
            field = value
            updateIcon()
        }

    @ColorInt
    var iconColor = context.color(R.color.warning_primary)
        set(value) {
            field = value
            refreshAppearance()
        }

    var relativeWidth: Int? = null
        set(value) {
            field = value?.coerceIn(0, 100)
            requestLayout()
        }

    private var iconDrawable: Drawable? = null
    private var gradientDrawable: GradientDrawable? = null
    private var topLeftRadius = resources.getDimension(R.dimen.xxxs)
    private var topRightRadius = resources.getDimension(R.dimen.xxxs)
    private var bottomLeftRadius = resources.getDimension(R.dimen.xxxs)
    private var bottomRightRadius = resources.getDimension(R.dimen.xxxs)
    private var horizontalPadding = resources.getDimensionPixelSize(R.dimen.xxxs)
    private var verticalPadding = resources.getDimensionPixelSize(R.dimen.dimen_1)

    private val shapeDrawable =
        MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(badgeColor)
        }

    private val textView = AppCompatTextView(this.context).apply {
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setTextAppearance(badgeTextAppearance)
        }
        setTypeface(fontFamily)
        setTextColor(textColor)
        includeFontPadding = false
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }

    init {
        background = shapeDrawable
        updatePadding()
        addView(textView)

        initAttributes(attrs)
    }

    fun setIcon(@DrawableRes drawableRes: Int) {
        iconDrawable = ContextCompat.getDrawable(context, drawableRes)
        updateIcon()
    }

    fun setIcon(drawable: Drawable?) {
        iconDrawable = drawable
        updateIcon()
    }

    fun setGradientBackground(@ColorInt colors: IntArray, orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT) {
        gradientDrawable = GradientDrawable(orientation, colors).apply {
            cornerRadii = currentCornerRadii()
        }
        refreshAppearance()
    }

    fun setCornerRadius(all: Float) {
        setCornerRadius(all, all, all, all)
    }

    fun setCornerRadius(topLeft: Float, topRight: Float, bottomLeft: Float, bottomRight: Float) {
        topLeftRadius = topLeft.dp
        topRightRadius = topRight.dp
        bottomLeftRadius = bottomLeft.dp
        bottomRightRadius = bottomRight.dp

        shapeDrawable.shapeAppearanceModel =
            ShapeAppearanceModel.builder()
                .setTopLeftCornerSize(topLeftRadius)
                .setTopRightCornerSize(topRightRadius)
                .setBottomLeftCornerSize(bottomLeftRadius)
                .setBottomRightCornerSize(bottomRightRadius)
                .build()

        gradientDrawable?.cornerRadii = currentCornerRadii()
        invalidate()
    }

    fun setBadgePadding(horizontal: Int, vertical: Int) {
        horizontalPadding = horizontal.dp.toInt()
        verticalPadding = vertical.dp.toInt()
        updatePadding()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val relative = relativeWidth
        if (relative != null) {
            val parentWidth = (parent as? View)?.measuredWidth ?: 0
            val targetWidth = (parentWidth * relative / 100)
            val forcedWidthSpec = MeasureSpec.makeMeasureSpec(targetWidth, MeasureSpec.EXACTLY)
            super.onMeasure(forcedWidthSpec, heightMeasureSpec)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        refreshAppearance()
    }

    private fun initAttributes(attrs: AttributeSet?) {
        attrs ?: return
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TextBadge, 0, 0)
        try {
            typedArray.getInt(R.styleable.TextBadge_badgeSize, BadgeSize.MEDIUM.ordinal).let { badgeSize = enumValues<BadgeSize>()[it] }
            typedArray.getInt(R.styleable.TextBadge_badgeShape, BadgeShape.STATUS.ordinal).let { badgeShape = enumValues<BadgeShape>()[it] }
            typedArray.getString(R.styleable.TextBadge_badgeText)?.let { text = it } ?: run { text = "Text Badge" }
            typedArray.getResourceId(R.styleable.TextBadge_textAppearance, -1).takeIf { it != -1 }?.let { badgeTextAppearance = it }
            typedArray.getColor(R.styleable.TextBadge_textColor, context.color(R.color.text_badge_foreground_green)).let { textColor = it }
            typedArray.getBoolean(R.styleable.TextBadge_startIconVisible, false).let { iconVisible = it }
            typedArray.getColor(R.styleable.TextBadge_startIconColor, context.color(R.color.text_badge_foreground_green)).let { iconColor = it }
            typedArray.getInt(R.styleable.TextBadge_startIconSize, 12).let { iconSize = it }
            typedArray.getColor(R.styleable.TextBadge_backgroundColor, context.color(R.color.text_badge_background_green)).let { badgeColor = it }
            typedArray.getInt(R.styleable.TextBadge_paddingHorizontal, -1).takeIf { it != -1 }?.let { horizontalPadding = it.dp.toInt() }
            typedArray.getInt(R.styleable.TextBadge_paddingVertical, -1).takeIf { it != -1 }?.let { verticalPadding = it.dp.toInt() }
            typedArray.getInt(R.styleable.TextBadge_relativeWidth, -1).takeIf { it != -1 }?.let { relativeWidth = it }

            setIcon(typedArray.getResourceId(R.styleable.TextBadge_startIcon, R.drawable.ic_placeholder_medium_24))

            val allRadius = typedArray.getDimension(R.styleable.TextBadge_cornerRadius, -1f)
            if (allRadius != -1f) {
                setCornerRadius(allRadius)
            } else {
                val topLeft = typedArray.getDimension(R.styleable.TextBadge_topLeftCornerRadius, -1f)
                val topRight = typedArray.getDimension(R.styleable.TextBadge_topRightCornerRadius, -1f)
                val bottomLeft = typedArray.getDimension(R.styleable.TextBadge_bottomLeftCornerRadius, -1f)
                val bottomRight = typedArray.getDimension(R.styleable.TextBadge_bottomRightCornerRadius, -1f)
                if (listOf(topLeft, topRight, bottomLeft, bottomRight).any { it != -1f }) {
                    setCornerRadius(
                        if (topLeft != -1f) topLeft else resources.getDimension(R.dimen.xxxs),
                        if (topRight != -1f) topRight else resources.getDimension(R.dimen.xxxs),
                        if (bottomLeft != -1f) bottomLeft else resources.getDimension(R.dimen.xxxs),
                        if (bottomRight != -1f) bottomRight else resources.getDimension(R.dimen.xxxs)
                    )
                } else {
                    setCornerRadius(resources.getDimension(R.dimen.xxxs))
                }
            }
        } finally {
            typedArray.recycle()
        }
    }

    private fun updatePadding() {
        setPadding(
            horizontalPadding,
            verticalPadding,
            horizontalPadding,
            verticalPadding
        )
    }

    private fun applyTextStyle() {
        TextViewCompat.setTextAppearance(textView, badgeTextAppearance)
        textView.setTextColor(textColor)
    }

    private fun updateIcon(@ColorInt color: Int = iconColor) {
        val defDrawablePadding = resources.getDimensionPixelSize(R.dimen.xxxs)
        val drawable = if (iconVisible) {
            iconDrawable?.mutate()?.apply {
                setTint(color)
                setBounds(0, 0, iconSize.dp.toInt(), iconSize.dp.toInt())
            }
        } else null

        textView.setCompoundDrawables(drawable, null, null, null)
        textView.compoundDrawablePadding = if (iconVisible) defDrawablePadding else 0
    }

    private fun currentCornerRadii(): FloatArray {
        return floatArrayOf(
            topLeftRadius,
            topLeftRadius,

            topRightRadius,
            topRightRadius,

            bottomRightRadius,
            bottomRightRadius,

            bottomLeftRadius,
            bottomLeftRadius
        )
    }

    private fun refreshAppearance() {
        if (isEnabled) {
            textView.setTextColor(textColor)
            updateIcon(iconColor)

            if (gradientDrawable != null) {
                background = gradientDrawable
            } else {
                shapeDrawable.fillColor = ColorStateList.valueOf(badgeColor)
                background = shapeDrawable
            }
        } else {
            textView.setTextColor(context.color(R.color.white))
            updateIcon(context.color(R.color.white))

            val disabledColor = when (badgeShape) {
                BadgeShape.STATUS -> context.color(R.color.black_50)
                BadgeShape.HIGHLIGHT -> context.color(R.color.black_40)
            }

            shapeDrawable.fillColor = ColorStateList.valueOf(disabledColor)
            background = shapeDrawable
        }
    }
}