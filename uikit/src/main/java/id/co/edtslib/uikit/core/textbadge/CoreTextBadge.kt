package id.co.edtslib.uikit.core.textbadge

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
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
import androidx.core.content.withStyledAttributes
import androidx.core.widget.TextViewCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.font

open class CoreTextBadge @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr) {

    var text: String? = "Text Badge"
        set(value) {
            field = value
            textView.text = value
            visibility = if (value.isNullOrEmpty()) GONE else VISIBLE
            requestLayout()
        }

    @ColorInt
    var textColor = context.color(R.color.foundation_color_neutral_white)
        set(value) {
            field = value
            applyTextStyle()
            refreshAppearance()
        }

    @ColorInt
    var badgeColor = context.color(R.color.foundation_color_primary_blue_30)
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
    var iconColor: Int? = context.color(R.color.foundation_color_neutral_white)
        set(value) {
            field = value
            refreshAppearance()
        }

    var relativeWidth: Int? = null
        set(value) {
            field = value?.coerceIn(0, 100)
            requestLayout()
        }

    var borderWidth: Float = 0f
        set(value) {
            field = value
            refreshAppearance()
        }

    @ColorInt
    var borderColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            refreshAppearance()
        }

    var shadowOpacity: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            refreshAppearance()
        }

    var shadowOffsetX: Float = 0f
        set(value) {
            field = value
            refreshAppearance()
        }

    var shadowOffsetY: Float = 0f
        set(value) {
            field = value
            refreshAppearance()
        }

    var shadowRadius: Float = 0f
        set(value) {
            field = value
            refreshAppearance()
        }

    @ColorInt
    var shadowColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            refreshAppearance()
        }

    var iconPadding: Int = 4.dp.toInt()
        set(value) {
            field = value
            updateIcon()
        }

    private var iconDrawable: Drawable? = null
    private var gradientDrawable: GradientDrawable? = null
    private var topLeftRadius = resources.getDimension(R.dimen.xxxs)
    private var topRightRadius = resources.getDimension(R.dimen.xxxs)
    private var bottomLeftRadius = resources.getDimension(R.dimen.xxxs)
    private var bottomRightRadius = resources.getDimension(R.dimen.xxxs)
    private var horizontalPadding = resources.getDimensionPixelSize(R.dimen.xxxs)
    private var verticalPadding = resources.getDimensionPixelSize(R.dimen.dimen_1)
    private var leftPadding = resources.getDimensionPixelSize(R.dimen.xxxs)
    private var rightPadding = resources.getDimensionPixelSize(R.dimen.xxxs)
    private var topPadding = resources.getDimensionPixelSize(R.dimen.dimen_1)
    private var bottomPadding = resources.getDimensionPixelSize(R.dimen.dimen_1)

    protected var badgeShape: BadgeShape = DefaultShape()
        set(value) {
            field = value
            setCornerRadius(
                value.topLeftRadius,
                value.topRightRadius,
                value.bottomLeftRadius,
                value.bottomRightRadius
            )
            refreshAppearance()
        }

    protected var hasBadgeShapeFromXml = false
    protected var hasBadgeVariantFromXml = false

    protected var badgeVariant: BadgeVariant? = null
        set(value) {
            field = value
            value?.let { applyVariant(it) }
        }

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
        gravity = Gravity.CENTER_VERTICAL
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

        readRecipeStyle(attrs, defStyleAttr)
        readAttributes(attrs)
    }

    fun setCoreShape(shape: BadgeShape) {
        badgeShape = shape
    }

    open fun badgeShapeFromStyle(@StyleRes resId: Int): BadgeShape? = when (resId) {
        R.style.Widget_EDTS_UIKit_TextBadge_Shape_Default -> DefaultShape()
        else -> null
    }

    open fun variantFromStyle(@StyleRes resId: Int): BadgeVariant? = null

    private fun applyVariant(variant: BadgeVariant) {
        badgeShape = variant.shape
        val preset = variant.preset
        iconSize = preset.iconSize
        iconPadding = preset.drawablePadding.dp.toInt()
        badgeTextAppearance = preset.textAppearance
        applyPadding(
            left = preset.leftPadding ?: preset.horizontalPadding,
            top = preset.topPadding ?: preset.verticalPadding,
            right = preset.rightPadding ?: preset.horizontalPadding,
            bottom = preset.bottomPadding ?: preset.verticalPadding
        )
        borderWidth = preset.borderWidth.dp

        variant.palette?.let { palette ->
            if (palette.gradientColors != null) {
                val resolvedColors = palette.gradientColors.map { context.color(it) }.toIntArray()
                setGradientBackground(resolvedColors, palette.gradientOrientation)
            } else {
                palette.backgroundColorRes?.let { badgeColor = context.color(it) }
            }
            palette.textColorRes?.let { textColor = context.color(it) }
            iconColor = palette.iconColorRes?.let { context.color(it) }
            palette.borderColorRes?.let { borderColor = context.color(it) }
        }

        refreshAppearance()
    }

    private fun readRecipeStyle(attrs: AttributeSet?, defStyleAttr: Int) {
        val styleArray = context.obtainStyledAttributes(attrs, R.styleable.TextBadge, defStyleAttr, 0)
        val styleRes = styleArray.getResourceId(R.styleable.TextBadge_coreTextBadgeStyle, 0)
        styleArray.recycle()
        val recipeStyle = if (styleRes != 0) styleRes else R.style.Widget_EDTS_UIKit_TextBadge_Core
        val themedContext = ContextThemeWrapper(context, recipeStyle)
        themedContext.withStyledAttributes(set = attrs, attrs = R.styleable.TextBadge, defStyleAttr = 0, defStyleRes = recipeStyle) {
            getColorStateList(R.styleable.TextBadge_backgroundColor)?.let { badgeColor = it.defaultColor }
            getColorStateList(R.styleable.TextBadge_textColor)?.let { textColor = it.defaultColor }
            getColorStateList(R.styleable.TextBadge_iconTint)?.let { iconColor = it.defaultColor }
            getResourceId(R.styleable.TextBadge_textAppearance, -1).takeIf { it != -1 }?.let { badgeTextAppearance = it }
            getDimension(R.styleable.TextBadge_strokeWidth, -1f).takeIf { it != -1f }?.let { borderWidth = it }
            getColor(R.styleable.TextBadge_strokeColor, Color.TRANSPARENT).let { borderColor = it }
            getFloat(R.styleable.TextBadge_badgeShadowOpacity, 0f).let { shadowOpacity = it }
            getDimension(R.styleable.TextBadge_badgeShadowOffsetX, 0f).let { shadowOffsetX = it }
            getDimension(R.styleable.TextBadge_badgeShadowOffsetY, 0f).let { shadowOffsetY = it }
            getDimension(R.styleable.TextBadge_badgeShadowRadius, 0f).let { shadowRadius = it }
            getColor(R.styleable.TextBadge_badgeShadowColor, Color.TRANSPARENT).let { shadowColor = it }
            getDimensionPixelSize(R.styleable.TextBadge_iconPadding, -1).takeIf { it != -1 }?.let { iconPadding = it }

            val allRadius = getDimension(R.styleable.TextBadge_cornerRadius, -1f)
            if (allRadius != -1f) {
                applyCornerRadius(allRadius, allRadius, allRadius, allRadius)
            } else {
                val fallback = resources.getDimension(R.dimen.xxxs)
                applyCornerRadius(
                    getDimension(R.styleable.TextBadge_topLeftCornerRadius, fallback),
                    getDimension(R.styleable.TextBadge_topRightCornerRadius, fallback),
                    getDimension(R.styleable.TextBadge_bottomLeftCornerRadius, fallback),
                    getDimension(R.styleable.TextBadge_bottomRightCornerRadius, fallback)
                )
            }
        }
        applyTextStyle()
        refreshAppearance()
    }

    private fun readAttributes(attrs: AttributeSet?) {
        attrs ?: return
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TextBadge, 0, 0)
        try {
            if (typedArray.hasValue(R.styleable.TextBadge_badgeShape)) {
                hasBadgeShapeFromXml = true
                badgeShapeFromStyle(typedArray.getResourceId(R.styleable.TextBadge_badgeShape, 0))?.let {
                    badgeShape = it
                }
            }
            if (typedArray.hasValue(R.styleable.TextBadge_badgeVariant)) {
                hasBadgeVariantFromXml = true
                variantFromStyle(typedArray.getResourceId(R.styleable.TextBadge_badgeVariant, 0))?.let {
                    badgeVariant = it
                }
            }
            val enabledArray = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.enabled))
            try {
                isEnabled = enabledArray.getBoolean(0, true)
            } finally {
                enabledArray.recycle()
            }
            typedArray.getString(R.styleable.TextBadge_badgeText)?.let { text = it } ?: run { text = "Text Badge" }
            if (typedArray.hasValue(R.styleable.TextBadge_textAppearance)) {
                badgeTextAppearance = typedArray.getResourceId(R.styleable.TextBadge_textAppearance, -1)
            }
            if (typedArray.hasValue(R.styleable.TextBadge_textColor)) {
                textColor = typedArray.getColor(R.styleable.TextBadge_textColor, 0)
            }
            if (typedArray.hasValue(R.styleable.TextBadge_startIconVisible)) {
                iconVisible = typedArray.getBoolean(R.styleable.TextBadge_startIconVisible, false)
            }
            if (typedArray.hasValue(R.styleable.TextBadge_iconTint)) {
                iconColor = typedArray.getColor(R.styleable.TextBadge_iconTint, 0)
            }
            if (typedArray.hasValue(R.styleable.TextBadge_iconSize)) {
                iconSize = typedArray.getInt(R.styleable.TextBadge_iconSize, R.dimen.xs)
            }
            if (typedArray.hasValue(R.styleable.TextBadge_backgroundColor)) {
                badgeColor = typedArray.getColor(R.styleable.TextBadge_backgroundColor, 0)
            }
            if (typedArray.hasValue(R.styleable.TextBadge_relativeWidth)) {
                relativeWidth = typedArray.getInt(R.styleable.TextBadge_relativeWidth, -1)
            }
            typedArray.getDimension(R.styleable.TextBadge_strokeWidth, -1f).takeIf { it != -1f }?.let { borderWidth = it }
            if (typedArray.hasValue(R.styleable.TextBadge_strokeColor)) {
                borderColor = typedArray.getColor(R.styleable.TextBadge_strokeColor, Color.TRANSPARENT)
            }
            typedArray.getFloat(R.styleable.TextBadge_badgeShadowOpacity, 0f).let { shadowOpacity = it }
            typedArray.getDimension(R.styleable.TextBadge_badgeShadowOffsetX, 0f).let { shadowOffsetX = it }
            typedArray.getDimension(R.styleable.TextBadge_badgeShadowOffsetY, 0f).let { shadowOffsetY = it }
            typedArray.getDimension(R.styleable.TextBadge_badgeShadowRadius, 0f).let { shadowRadius = it }
            typedArray.getColor(R.styleable.TextBadge_badgeShadowColor, Color.TRANSPARENT).let { shadowColor = it }
            typedArray.getDimensionPixelSize(R.styleable.TextBadge_iconPadding, -1).takeIf { it != -1 }?.let { iconPadding = it }

            val androidPaddingAttrs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intArrayOf(
                    android.R.attr.padding,
                    android.R.attr.paddingHorizontal,
                    android.R.attr.paddingVertical,
                    android.R.attr.paddingLeft,
                    android.R.attr.paddingTop,
                    android.R.attr.paddingRight,
                    android.R.attr.paddingBottom,
                    android.R.attr.paddingStart,
                    android.R.attr.paddingEnd
                )
            } else {
                intArrayOf(
                    android.R.attr.padding,
                    android.R.attr.paddingLeft,
                    android.R.attr.paddingTop,
                    android.R.attr.paddingRight,
                    android.R.attr.paddingBottom,
                    android.R.attr.paddingStart,
                    android.R.attr.paddingEnd
                )
            }
            val androidPaddingArray = context.obtainStyledAttributes(attrs, androidPaddingAttrs)
            try {
                val hasAndroidPadding = androidPaddingAttrs.indices.any { androidPaddingArray.hasValue(it) }
                if (hasAndroidPadding) {
                    leftPadding = paddingLeft
                    topPadding = paddingTop
                    rightPadding = paddingRight
                    bottomPadding = paddingBottom
                }
            } finally {
                androidPaddingArray.recycle()
            }
            updatePadding()

            setIcon(typedArray.getResourceId(R.styleable.TextBadge_startIcon, R.drawable.ic_placeholder_medium_24))

            val hasRadius = listOf(
                R.styleable.TextBadge_cornerRadius,
                R.styleable.TextBadge_topLeftCornerRadius,
                R.styleable.TextBadge_topRightCornerRadius,
                R.styleable.TextBadge_bottomLeftCornerRadius,
                R.styleable.TextBadge_bottomRightCornerRadius
            ).any { typedArray.hasValue(it) }
            if (hasRadius) {
                val allRadius = typedArray.getDimension(R.styleable.TextBadge_cornerRadius, -1f)
                if (allRadius != -1f) {
                    applyCornerRadius(allRadius, allRadius, allRadius, allRadius)
                } else {
                    val topLeft = typedArray.getDimension(R.styleable.TextBadge_topLeftCornerRadius, -1f)
                    val topRight = typedArray.getDimension(R.styleable.TextBadge_topRightCornerRadius, -1f)
                    val bottomLeft = typedArray.getDimension(R.styleable.TextBadge_bottomLeftCornerRadius, -1f)
                    val bottomRight = typedArray.getDimension(R.styleable.TextBadge_bottomRightCornerRadius, -1f)
                    if (listOf(topLeft, topRight, bottomLeft, bottomRight).any { it != -1f }) {
                        applyCornerRadius(
                            if (topLeft != -1f) topLeft else resources.getDimension(R.dimen.xxxs),
                            if (topRight != -1f) topRight else resources.getDimension(R.dimen.xxxs),
                            if (bottomLeft != -1f) bottomLeft else resources.getDimension(R.dimen.xxxs),
                            if (bottomRight != -1f) bottomRight else resources.getDimension(R.dimen.xxxs)
                        )
                    } else {
                        applyCornerRadius(resources.getDimension(R.dimen.xxxs), resources.getDimension(R.dimen.xxxs), resources.getDimension(R.dimen.xxxs), resources.getDimension(R.dimen.xxxs))
                    }
                }
            }
        } finally {
            typedArray.recycle()
        }
    }

    fun setIcon(@DrawableRes drawableRes: Int) {
        iconDrawable = ContextCompat.getDrawable(context, drawableRes)
        updateIcon()
    }

    fun setIcon(drawable: Drawable?) {
        iconDrawable = drawable
        updateIcon()
    }

    fun setCornerRadius(all: Float) {
        setCornerRadius(all, all, all, all)
    }

    fun setCornerRadius(topLeft: Float, topRight: Float, bottomLeft: Float, bottomRight: Float) {
        applyCornerRadius(topLeft.dp, topRight.dp, bottomLeft.dp, bottomRight.dp)
    }

    private fun applyCornerRadius(topLeft: Float, topRight: Float, bottomLeft: Float, bottomRight: Float) {
        topLeftRadius = topLeft
        topRightRadius = topRight
        bottomLeftRadius = bottomLeft
        bottomRightRadius = bottomRight

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

    fun setGradientBackground(@ColorInt colors: IntArray, orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT) {
        buildGradientDrawable(colors, orientation)
        refreshAppearance()
    }

    private fun buildGradientDrawable(colors: IntArray, orientation: GradientDrawable.Orientation) {
        gradientDrawable = GradientDrawable(orientation, colors).apply {
            cornerRadii = currentCornerRadii()
        }
    }

    fun setBadgePadding(horizontal: Int, vertical: Int) {
        horizontalPadding = horizontal.dp.toInt()
        verticalPadding = vertical.dp.toInt()
        applyPadding(left = horizontal, top = vertical, right = horizontal, bottom = vertical)
    }

    fun setBadgePadding(left: Int, top: Int, right: Int, bottom: Int) {
        applyPadding(left = left, top = top, right = right, bottom = bottom)
    }

    private fun applyPadding(left: Int, top: Int, right: Int, bottom: Int) {
        leftPadding = left.dp.toInt()
        topPadding = top.dp.toInt()
        rightPadding = right.dp.toInt()
        bottomPadding = bottom.dp.toInt()
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

    private fun updatePadding() {
        setPadding(
            leftPadding,
            topPadding,
            rightPadding,
            bottomPadding
        )
    }

    private fun applyTextStyle() {
        TextViewCompat.setTextAppearance(textView, badgeTextAppearance)
        textView.setTextColor(textColor)
    }

    private fun updateIcon(@ColorInt color: Int? = resolveIconColor()) {
        val drawable = if (iconVisible) {
            iconDrawable?.mutate()?.apply {
                setTintList(color?.let(ColorStateList::valueOf))
                setBounds(0, 0, iconSize.dp.toInt(), iconSize.dp.toInt())
            }
        } else null

        textView.setCompoundDrawables(drawable, null, null, null)
        textView.compoundDrawablePadding = if (iconVisible) iconPadding else 0
    }

    @ColorInt
    private fun resolveIconColor(): Int? {
        if (isEnabled) return iconColor
        val disabled = badgeVariant?.disabledPalette ?: badgeShape.disabledPalette
        return disabled.iconColorRes?.let { context.color(it) } ?: iconColor
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
                gradientDrawable?.setStroke(borderWidth.toInt(), borderColor)
                background = gradientDrawable
            } else {
                shapeDrawable.fillColor = ColorStateList.valueOf(badgeColor)
                shapeDrawable.setStroke(borderWidth, borderColor)
                background = shapeDrawable
            }
        } else {
            val disabled = badgeVariant?.disabledPalette ?: badgeShape.disabledPalette

            disabled.textColorRes?.let { textView.setTextColor(context.color(it)) }
            disabled.iconColorRes?.let { updateIcon(context.color(it)) }

            val disabledBorder = disabled.borderColorRes?.let { context.color(it) } ?: borderColor
            if (disabled.gradientColors != null) {
                val resolved = disabled.gradientColors.map { context.color(it) }.toIntArray()
                buildGradientDrawable(resolved, disabled.gradientOrientation)
                gradientDrawable?.setStroke(borderWidth.toInt(), disabledBorder)
                background = gradientDrawable
            } else {
                disabled.backgroundColorRes?.let {
                    shapeDrawable.fillColor = ColorStateList.valueOf(context.color(it))
                }
                shapeDrawable.setStroke(borderWidth, disabledBorder)
                background = shapeDrawable
            }
        }

        applyShadow()
    }

    private fun applyShadow() {
        val hasShadow = shadowRadius > 0f &&
                (shadowOpacity > 0f || shadowColor != Color.TRANSPARENT) &&
                (shadowOffsetX != 0f || shadowOffsetY != 0f || shadowRadius > 0f)
        if (!hasShadow) {
            if (layerType != LAYER_TYPE_NONE) {
                setLayerType(LAYER_TYPE_NONE, null)
            }
            return
        }

        val alpha = (shadowOpacity * 255).toInt().coerceIn(0, 255)
        val effectiveColor = Color.argb(
            alpha,
            Color.red(shadowColor),
            Color.green(shadowColor),
            Color.blue(shadowColor)
        )
        val paint = Paint().apply {
            isAntiAlias = true
            setShadowLayer(shadowRadius, shadowOffsetX, shadowOffsetY, effectiveColor)
        }
        setLayerType(LAYER_TYPE_SOFTWARE, paint)
        invalidate()
    }
}