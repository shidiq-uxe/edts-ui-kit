package id.co.edtslib.uikit.core.chip

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewChipBinding
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.deviceWidth
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.inflater
import com.google.android.material.color.MaterialColors
import android.graphics.drawable.InsetDrawable
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import androidx.appcompat.content.res.AppCompatResources
import id.co.edtslib.uikit.utils.px

open class CoreChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr) {

    private val binding = ViewChipBinding.inflate(this.context.inflater, this, true)
    private var startIconBaseDrawable: Drawable? = null
    private var endIconBaseDrawable: Drawable? = null
    private val defaultStartIconSizePx = binding.ivStartIcon.layoutParams.width
    private val defaultEndIconSizePx = binding.ivEndIcon.layoutParams.width

    val chipCard = binding.cardContainer
    var delegate: ChipDelegate? = null

    var chipColor: Int? = null
        set(value) {
            field = value
            refreshAppearance()
        }

    var chipTextColor: Int? = null
        set(value) {
            field = value
            refreshAppearance()
        }

    var chipStartIconSize: Int? = null
        set(value) {
            field = value
            refreshAppearance()
        }

    var chipEndIconSize: Int? = null
        set(value) {
            field = value
            refreshAppearance()
        }

    var chipStartIconColor: Int? = null
        set(value) {
            field = value
            refreshAppearance()
        }

    var chipEndIconColor: Int? = null
        set(value) {
            field = value
            refreshAppearance()
        }

    var chipBorderColor: Int? = null
        set(value) {
            field = value
            refreshAppearance()
        }

    var chipStartIconBackgroundColor: Int? = null
        set(value) {
            field = value
            refreshAppearance()
        }

    var chipEndIconBackgroundColor: Int? = null
        set(value) {
            field = value
            refreshAppearance()
        }

    var chipCornerRadius: Float? = null
        set(value) {
            field = value
            applyCornerRadius(value ?: chipVariant?.preset?.cornerRadius)
        }

    var chipBorderWidth: Int? = null
        set(value) {
            field = value
            refreshAppearance()
        }

    var chipTextAppearance: Int? = null
        set(value) {
            field = value
            applyTextAppearance(value ?: chipVariant?.preset?.textAppearance)
        }

    var text: String? = null
        set(value) {
            field = value
            binding.tvTitle.text = value.orEmpty()
        }

    var isStartIconVisible: Boolean = false
        set(value) {
            field = value
            refreshAppearance()
        }

    var isEndIconVisible: Boolean = false
        set(value) {
            field = value
            refreshAppearance()
        }

    var chipStartIconBackgroundVisible: Boolean? = null
        set(value) {
            field = value
            refreshAppearance()
        }

    var chipEndIconBackgroundVisible: Boolean? = null
        set(value) {
            field = value
            refreshAppearance()
        }

    var isStartIconActionable: Boolean = false
        set(value) {
            field = value
            refreshIconActionability(binding.ivStartIcon, value, isStartIcon = true)
        }

    var isEndIconActionable: Boolean = false
        set(value) {
            field = value
            refreshIconActionability(binding.ivEndIcon, value, isStartIcon = false)
        }

    fun setStartIcon(@DrawableRes resId: Int) {
        startIconBaseDrawable = AppCompatResources.getDrawable(context, resId)
        refreshAppearance()
    }

    fun setEndIcon(@DrawableRes resId: Int) {
        endIconBaseDrawable = AppCompatResources.getDrawable(context, resId)
        refreshAppearance()
    }

    protected var chipVariant: ChipVariant? = null
        set(value) {
            field = value
            value?.let { applyVariant(it) }
        }

    @ChipStateAnnotation
    var state: Int = CHIP_STATE_IDLE
        set(value) {
            field = value
            refreshAppearance()
        }

    init {
        binding.root.updateLayoutParams {
            width = LayoutParams.WRAP_CONTENT
        }
        chipVariant = CoreChipVariant
        bindClickAction()
        applyXmlAttrs(attrs, defStyleAttr)
    }

    private fun applyXmlAttrs(attrs: AttributeSet?, defStyleAttr: Int) {
        attrs ?: return
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Chip, defStyleAttr, 0)
        try {
            if (typedArray.hasValue(R.styleable.Chip_text)) {
                text = typedArray.getString(R.styleable.Chip_text)
            }
            if (typedArray.hasValue(R.styleable.Chip_isStartIconVisible)) {
                isStartIconVisible = typedArray.getBoolean(R.styleable.Chip_isStartIconVisible, false)
            }
            if (typedArray.hasValue(R.styleable.Chip_isEndIconVisible)) {
                isEndIconVisible = typedArray.getBoolean(R.styleable.Chip_isEndIconVisible, false)
            }
            if (typedArray.hasValue(R.styleable.Chip_isStartIconActionable)) {
                isStartIconActionable = typedArray.getBoolean(R.styleable.Chip_isStartIconActionable, false)
            }
            if (typedArray.hasValue(R.styleable.Chip_isEndIconActionable)) {
                isEndIconActionable = typedArray.getBoolean(R.styleable.Chip_isEndIconActionable, false)
            }
            if (typedArray.hasValue(R.styleable.Chip_startIcon)) {
                val resId = typedArray.getResourceId(R.styleable.Chip_startIcon, 0)
                if (resId != 0) setStartIcon(resId)
            }
            if (typedArray.hasValue(R.styleable.Chip_endIcon)) {
                val resId = typedArray.getResourceId(R.styleable.Chip_endIcon, 0)
                if (resId != 0) setEndIcon(resId)
            }
            if (typedArray.hasValue(R.styleable.Chip_chipState)) {
                state = typedArray.getInt(R.styleable.Chip_chipState, CHIP_STATE_IDLE)
            }
            if (typedArray.hasValue(R.styleable.Chip_chipColor)) {
                chipColor = typedArray.getColor(R.styleable.Chip_chipColor, 0)
            } else if (typedArray.hasValue(R.styleable.Chip_backgroundColor)) {
                chipColor = typedArray.getColor(R.styleable.Chip_backgroundColor, 0)
            }
            if (typedArray.hasValue(R.styleable.Chip_textColor)) {
                chipTextColor = typedArray.getColor(R.styleable.Chip_textColor, 0)
            }
            if (typedArray.hasValue(R.styleable.Chip_startIconColor)) {
                chipStartIconColor = typedArray.getColor(R.styleable.Chip_startIconColor, 0)
            }
            if (typedArray.hasValue(R.styleable.Chip_endIconColor)) {
                chipEndIconColor = typedArray.getColor(R.styleable.Chip_endIconColor, 0)
            }
            if (typedArray.hasValue(R.styleable.Chip_strokeColor)) {
                chipBorderColor = typedArray.getColor(R.styleable.Chip_strokeColor, 0)
            }
            if (typedArray.hasValue(R.styleable.Chip_startIconBackgroundColor)) {
                chipStartIconBackgroundColor = typedArray.getColor(R.styleable.Chip_startIconBackgroundColor, 0)
            }
            if (typedArray.hasValue(R.styleable.Chip_endIconBackgroundColor)) {
                chipEndIconBackgroundColor = typedArray.getColor(R.styleable.Chip_endIconBackgroundColor, 0)
            }
            if (typedArray.hasValue(R.styleable.Chip_cornerRadius)) {
                val radiusPx = typedArray.getDimension(R.styleable.Chip_cornerRadius, 0f)
                chipCornerRadius = radiusPx / resources.displayMetrics.density
            }
            if (typedArray.hasValue(R.styleable.Chip_strokeWidth)) {
                val widthPx = typedArray.getDimension(R.styleable.Chip_strokeWidth, 0f)
                chipBorderWidth = (widthPx / resources.displayMetrics.density).toInt()
            }
            if (typedArray.hasValue(R.styleable.Chip_textAppearance)) {
                val resId = typedArray.getResourceId(R.styleable.Chip_textAppearance, 0)
                if (resId != 0) chipTextAppearance = resId
            }
        } finally {
            typedArray.recycle()
        }
    }

    private fun applyVariant(variant: ChipVariant) {
        applyWidth(variant.preset.widthPercent)
        applyCornerRadius(chipCornerRadius ?: variant.preset.cornerRadius)
        applyTextAppearance(chipTextAppearance ?: variant.preset.textAppearance)
        applyPadding(variant.preset.horizontalPaddingDp, variant.preset.verticalPaddingDp)
        refreshAppearance()
    }

    private fun applyTextAppearance(@StyleRes textAppearance: Int?) {
        textAppearance?.let { binding.tvTitle.setTextAppearance(it) }
    }

    private fun applyPadding(horizontalPaddingDp: Int?, verticalPaddingDp: Int?) {
        if (horizontalPaddingDp == null && verticalPaddingDp == null) return
        val horizontalPx = horizontalPaddingDp?.dp?.toInt() ?: chipCard.paddingLeft
        val verticalPx = verticalPaddingDp?.dp?.toInt() ?: chipCard.paddingTop
        chipCard.setPadding(horizontalPx, verticalPx, horizontalPx, verticalPx)
    }

    private fun applyWidth(widthPercent: Double?) {
        binding.root.updateLayoutParams {
            width = if (widthPercent != null) {
                (context.deviceWidth * widthPercent).toInt()
            } else {
                LayoutParams.WRAP_CONTENT
            }
        }
    }

    private var cardCornerRadiusPx: Float = 0f
    private var isAutoCornerRadius = false
    private var isCornerRadiusRefreshPending = false

    private val autoCornerRadiusLayoutListener =
        OnLayoutChangeListener { view, _, top, _, bottom, _, _, _, _ ->
            val height = bottom - top
            val newRadius = height / 2f
            if (height > 0 && newRadius != cardCornerRadiusPx) {
                cardCornerRadiusPx = newRadius
                if (!isCornerRadiusRefreshPending) {
                    isCornerRadiusRefreshPending = true
                    view.post {
                        isCornerRadiusRefreshPending = false
                        refreshAppearance()
                    }
                }
            }
        }

    private fun applyCornerRadius(cornerRadius: Float?) {
        if (cornerRadius != null) {
            if (isAutoCornerRadius) {
                chipCard.removeOnLayoutChangeListener(autoCornerRadiusLayoutListener)
                isAutoCornerRadius = false
            }
            cardCornerRadiusPx = cornerRadius.dp
        } else {
            if (!isAutoCornerRadius) {
                chipCard.addOnLayoutChangeListener(autoCornerRadiusLayoutListener)
                isAutoCornerRadius = true
            }
            if (chipCard.height > 0) {
                cardCornerRadiusPx = chipCard.height / 2f
            }
        }
    }

    private fun refreshAppearance() {
        val variant = chipVariant ?: return
        val preset = variant.preset
        val palette = variant.paletteFor(state)

        applyBackground(palette, preset)

        val textColor = chipTextColor ?: palette.textColorRes?.let { context.color(it) }
        textColor?.let { binding.tvTitle.setTextColor(it) }

        binding.ivStartIcon.isVisible = isStartIconVisible
        binding.ivEndIcon.isVisible = isEndIconVisible

        applyIcon(
            binding.ivStartIcon, chipStartIconSize?.dp?.toInt() ?: preset.startIconSizeDp?.dp?.toInt() ?: defaultStartIconSizePx, startIconBaseDrawable, isStartIconVisible,
            chipStartIconBackgroundVisible ?: palette.isStartIconBackgroundVisible,
            chipStartIconBackgroundColor, palette.startIconBackgroundColorRes, palette.startIconPaddingDp
        )

        applyIcon(
            binding.ivEndIcon, chipEndIconSize?.dp?.toInt() ?: preset.endIconSizeDp?.dp?.toInt() ?: defaultEndIconSizePx, endIconBaseDrawable, isEndIconVisible,
            chipEndIconBackgroundVisible ?: palette.isEndIconBackgroundVisible,
            chipEndIconBackgroundColor, palette.endIconBackgroundColorRes, palette.endIconPaddingDp
        )

        val startIconColor = chipStartIconColor ?: palette.startIconColorRes?.let { context.color(it) }
        startIconColor?.let { binding.ivStartIcon.setColorFilter(it) }

        val endIconColor = chipEndIconColor ?: palette.endIconColorRes?.let { context.color(it) }
        endIconColor?.let { binding.ivEndIcon.setColorFilter(it) }
    }

    private fun applyBackground(palette: ChipPalette, preset: ChipPreset) {
        chipCard.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cardCornerRadiusPx

            when {
                chipColor != null -> setColor(chipColor!!)
                palette.gradientColors != null -> {
                    orientation = palette.gradientOrientation
                    colors = palette.gradientColors.map { context.color(it) }.toIntArray()
                }
                palette.backgroundColorRes != null -> setColor(context.color(palette.backgroundColorRes))
            }

            val borderColor = chipBorderColor ?: palette.borderColorRes?.let { context.color(it) }
            val borderWidth = chipBorderWidth ?: preset.borderWidth
            if (borderColor != null && borderWidth > 0) {
                setStroke(borderWidth.dp.toInt(), borderColor)
            }
        }
    }

    private fun refreshIconActionability(iconView: ImageView, isActionable: Boolean, isStartIcon: Boolean) {
        if (isActionable) {
            val rippleColor = MaterialColors.getColor(
                iconView, com.google.android.material.R.attr.colorControlHighlight, context.color(R.color.black_30)
            )
            iconView.foreground = RippleDrawable(
                ColorStateList.valueOf(rippleColor),
                null,
                InsetDrawable(
                    GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(context.color(R.color.foundation_color_neutral_white))
                    },
                    ICON_PADDING_DP.dp.toInt()
                )
            )
            iconView.isClickable = true
            iconView.isFocusable = true
            iconView.setOnClickListener { view ->
                if (isStartIcon) {
                    delegate?.onStartIconClick(view, state)
                } else {
                    delegate?.onEndIconClick(view, state)
                }
            }
        } else {
            iconView.foreground = null
            iconView.setOnClickListener(null)
            iconView.isClickable = false
            iconView.isFocusable = false
        }
    }

    private fun applyIcon(
        iconView: ImageView,
        baseIconSizePx: Int,
        baseDrawable: Drawable?,
        isIconVisible: Boolean,
        isBackgroundVisible: Boolean,
        overrideColor: Int?,
        @ColorRes backgroundColorRes: Int?,
        paddingDp: Int?
    ) {
        val outwardPaddingDp = paddingDp ?: ICON_PADDING_DP
        val outwardPaddingPx = outwardPaddingDp.dp.toInt()
        val newSizePx = baseIconSizePx + (outwardPaddingPx * 2)

        iconView.updateLayoutParams {
            width = newSizePx
            height = newSizePx
        }

        val iconInsetPx = (outwardPaddingDp + DEFAULT_ICON_PADDING_DP).dp.toInt()
        iconView.setImageDrawable(baseDrawable?.let { InsetDrawable(it, iconInsetPx) })

        val showIconBg = isIconVisible && isBackgroundVisible
        val bgColor = overrideColor ?: backgroundColorRes?.let { context.color(it) }
        iconView.background = if (showIconBg && bgColor != null) {
            ColorDrawable(bgColor)
        } else {
            null
        }
    }

    private fun bindClickAction() {
        binding.cardContainer.setOnClickListener { view ->
            delegate?.onClick(view, state)
        }
    }

    companion object {
        private const val ICON_PADDING_DP = 2
        private const val DEFAULT_ICON_PADDING_DP = 1
    }
}