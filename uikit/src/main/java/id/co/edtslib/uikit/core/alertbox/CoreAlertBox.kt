package id.co.edtslib.uikit.core.alertbox

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.TouchDelegate
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.alert.AlertBoxDelegate
import id.co.edtslib.uikit.core.button.CoreButton
import id.co.edtslib.uikit.databinding.ViewAlertBinding
import id.co.edtslib.uikit.databinding.ViewCoreAlertBoxBinding
import id.co.edtslib.uikit.utils.TextStyle
import id.co.edtslib.uikit.utils.buildHighlightedMessage
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.inflater
import id.co.edtslib.uikit.utils.html.FontManager
import id.co.edtslib.uikit.utils.html.HtmlRenderer
import id.co.edtslib.uikit.utils.html.HtmlRendererConfig
import id.co.edtslib.uikit.utils.typefaceFromAppearance
import kotlin.math.roundToInt

/** Shared message, action and accessibility behavior. Product appearance is supplied by XML. */
open class CoreAlertBox protected constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    @LayoutRes contentLayout: Int
) : FrameLayout(context, attrs, defStyleAttr) {

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.coreAlertBoxStyle
    ) : this(context, attrs, defStyleAttr, R.layout.view_core_alert_box)

    var delegate: AlertBoxDelegate? = null

    @get:StyleRes
    var alertRecipe: Int
        get() = selectedRecipe
        set(@StyleRes value) {
            selectedRecipe = value
            if (value != 0) applyRecipe(value)
        }

    var text: CharSequence = ""
        set(value) {
            field = value
            renderMessage()
        }

    var isHtml = false
        set(value) {
            field = value
            renderMessage()
        }

    var buttonText: CharSequence = ""
        set(value) {
            field = value
            views.button.text = value
        }

    var isButtonVisible = false
        set(value) {
            field = value
            views.button.isVisible = value
        }

    var isStartIconVisible = true
        set(value) {
            field = value
            views.icon.isVisible = value
            updateContentConstraints()
        }

    var isCloseIconVisible = true
        set(value) {
            field = value
            views.close.isVisible = value
            updateContentConstraints()
        }

    var startIcon: Drawable = ColorDrawable(Color.TRANSPARENT)
        set(value) {
            field = value
            views.icon.setImageDrawable(value)
        }

    var closeIcon: Drawable = context.drawable(R.drawable.ic_close_16)
        set(value) {
            field = value
            when (val close = views.close) {
                is MaterialCheckBox -> close.setButtonDrawable(value)
                is ImageButton -> close.setImageDrawable(value)
            }
        }

    var iconAlignment = IconAlignment.TOP
        set(value) {
            field = value
            updateIconAlignment()
        }

    var actionText: CharSequence = ""
        set(value) {
            field = value
            views.action.text = value
        }

    var isActionVisible = false
        set(value) {
            field = value
            views.action.isVisible = value
            updateContentConstraints()
        }

    @get:StyleRes
    protected open var actionTextAppearance = com.google.android.material.R.style.TextAppearance_Material3_LabelSmall
        set(value) {
            field = value
            views.action.setTextAppearance(value)
            context.typefaceFromAppearance(value)?.let { views.action.typeface = it }
        }

    @get:ColorInt
    protected open var actionTextColor: Int = Color.BLACK
        set(value) {
            field = value
            views.action.setTextColor(value)
            updateActionIcon()
        }

    var actionMaxLines = 1
        set(value) {
            require(value > 0) { "actionMaxLines must be positive" }
            field = value
            views.action.maxLines = value
        }

    var actionEllipsize: TextUtils.TruncateAt? = TextUtils.TruncateAt.END
        set(value) {
            field = value
            views.action.ellipsize = value
        }

    var actionEndIcon: Drawable? = null
        set(value) {
            field = value
            updateActionIcon()
        }

    @get:ColorInt
    var actionEndIconTint: Int? = null
        set(value) {
            field = value
            updateActionIcon()
        }

    /** Gap between the inline action and its icon, in pixels. */
    var actionIconPadding = 4.dp.roundToInt()
        set(value) {
            field = value
            views.action.compoundDrawablePadding = value
        }

    protected open var cornerRadius = 0f
        set(value) {
            field = value
            views.card.shapeAppearanceModel = views.card.shapeAppearanceModel.toBuilder()
                .setAllCornerSizes(value).build()
        }

    protected open var strokeWidth = 0f
        set(value) {
            field = value
            views.card.strokeWidth = value.toInt()
        }

    @get:ColorInt
    protected open var strokeColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            views.card.strokeColor = value
        }

    @get:ColorInt
    protected open var alertBackgroundColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            views.card.setCardBackgroundColor(value)
        }

    @get:StyleRes
    protected open var textAppearance = com.google.android.material.R.style.TextAppearance_Material3_BodySmall
        set(value) {
            field = value
            views.message.setTextAppearance(value)
            if (!views.isLegacy) {
                context.typefaceFromAppearance(value)?.let { views.message.typeface = it }
                context.withStyledAttributes(value, intArrayOf(androidx.appcompat.R.attr.lineHeight)) {
                    if (hasValue(0)) TextViewCompat.setLineHeight(views.message, getDimensionPixelSize(0, 0))
                }
            }
            renderMessage()
        }

    @get:ColorInt
    protected open var textColor: Int = Color.BLACK
        set(value) {
            field = value
            views.message.setTextColor(value)
            renderMessage()
        }

    @get:ColorInt
    protected open var startIconTint: Int? = null
        set(value) {
            field = value
            if (views.isLegacy) {
                value?.let { views.icon.setColorFilter(it) }
            } else {
                // A null tint removes a previous override and restores the drawable's colors.
                ImageViewCompat.setImageTintList(views.icon, value?.let { ColorStateList.valueOf(it) })
            }
        }

    @get:ColorInt
    protected open var closeIconTint: Int = Color.GRAY
        set(value) {
            field = value
            val tint = ColorStateList.valueOf(value)
            when (val close = views.close) {
                is MaterialCheckBox -> close.buttonIconTintList = tint
                is ImageButton -> ImageViewCompat.setImageTintList(close, tint)
            }
        }

    protected val footerButton: CoreButton get() = views.button

    private val views = createViews(contentLayout)
    private var selectedRecipe = 0
    private var isOpticalSpacingEnabled = views.isLegacy
    private var opticalOffset = 2.dp.roundToInt()
    private var contentGap = 8.dp.roundToInt()
    private var emphasisAppearance = 0
    private var isApplyingRecipe = false

    init {
        views.close.contentDescription = context.getString(R.string.alert_box_close)
        views.close.setOnClickListener {
            if (views.isLegacy || isEnabled) delegate?.onCloseClickListener(it)
        }
        views.action.setOnClickListener {
            if (views.isLegacy || isEnabled) delegate?.onActionClickListener(it)
        }
        views.button.setOnClickListener {
            if (views.isLegacy || isEnabled) delegate?.onButtonClickListener(it)
        }
        if (!views.isLegacy) {
            minimumHeight = 48.dp.roundToInt()
            context.withStyledAttributes(attrs, R.styleable.CoreAlertBox, defStyleAttr, 0) {
                selectedRecipe = getResourceId(R.styleable.CoreAlertBox_alertRecipe, 0)
            }
            if (selectedRecipe != 0) {
                applyRecipe(selectedRecipe, attrs)
            } else {
                applyRecipe(R.style.Widget_EDTS_UIKit_CoreAlertBox, attrs, defStyleAttr)
            }
            readContent(attrs, defStyleAttr)
        }
    }

    private fun createViews(@LayoutRes layout: Int): AlertViews {
        if (layout == R.layout.view_alert) {
            val binding = ViewAlertBinding.inflate(context.inflater, this, true)
            val container = binding.tvAlertMessage.parent as ConstraintLayout
            val action = MaterialTextView(context).apply {
                id = View.generateViewId()
                visibility = GONE
                setPadding(4.dp.roundToInt(), 0, 4.dp.roundToInt(), 0)
                isClickable = true
                isFocusable = true
            }
            container.addView(action, ConstraintLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
            return AlertViews(
                binding.root, container, binding.tvAlertMessage, binding.ivAlertIcon,
                binding.ivAlertClose, binding.btnAction, action, true
            )
        }
        val binding = ViewCoreAlertBoxBinding.inflate(context.inflater, this, true)
        return AlertViews(
            binding.root, binding.alertContent, binding.tvAlertMessage, binding.ivAlertIcon,
            binding.ivAlertClose, binding.btnAction, binding.tvAlertAction, false
        )
    }

    private fun readContent(attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(attrs, R.styleable.CoreAlertBox, defStyleAttr, 0) {
            isEnabled = getBoolean(R.styleable.CoreAlertBox_android_enabled, isEnabled)
            text = getText(R.styleable.CoreAlertBox_text) ?: ""
            isHtml = getBoolean(R.styleable.CoreAlertBox_isHtml, false)
            buttonText = getText(R.styleable.CoreAlertBox_buttonText) ?: ""
            isButtonVisible = getBoolean(R.styleable.CoreAlertBox_isButtonVisible, false)
            isStartIconVisible = getBoolean(R.styleable.CoreAlertBox_isStartIconVisible, true)
            isCloseIconVisible = getBoolean(R.styleable.CoreAlertBox_isCloseIconVisible, true)
            iconAlignment = IconAlignment.entries[getInt(R.styleable.CoreAlertBox_startIconVerticalAlignment, 0)
                .coerceIn(0, IconAlignment.entries.lastIndex)]
        }
        readInlineActionAttributes(attrs, defStyleAttr)
    }

    protected fun readInlineActionAttributes(attrs: AttributeSet?, defStyleAttr: Int = 0) {
        context.withStyledAttributes(attrs, R.styleable.CoreAlertBox, defStyleAttr, 0) {
            actionText = getText(R.styleable.CoreAlertBox_actionText) ?: ""
            isActionVisible = getBoolean(R.styleable.CoreAlertBox_isActionVisible, false)
            actionMaxLines = getInt(R.styleable.CoreAlertBox_actionMaxLines, 1).coerceAtLeast(1)
            actionEllipsize = when (getInt(R.styleable.CoreAlertBox_actionEllipsize, 3)) {
                1 -> TextUtils.TruncateAt.START
                2 -> TextUtils.TruncateAt.MIDDLE
                3 -> TextUtils.TruncateAt.END
                else -> null
            }
            actionEndIcon = getDrawable(R.styleable.CoreAlertBox_actionEndIcon)
            if (hasValue(R.styleable.CoreAlertBox_actionEndIconTint)) {
                actionEndIconTint = getColor(R.styleable.CoreAlertBox_actionEndIconTint, actionTextColor)
            }
            actionIconPadding = getDimensionPixelSize(R.styleable.CoreAlertBox_actionIconPadding, actionIconPadding)
        }
    }

    private fun applyRecipe(@StyleRes recipe: Int, attrs: AttributeSet? = null, defStyleAttr: Int = 0) {
        isApplyingRecipe = true
        try {
            context.withStyledAttributes(attrs, R.styleable.CoreAlertBox, defStyleAttr, recipe) {
                cornerRadius = getDimension(R.styleable.CoreAlertBox_cornerRadius, 0f)
                strokeWidth = getDimension(R.styleable.CoreAlertBox_strokeWidth, 0f)
                strokeColor = getColor(R.styleable.CoreAlertBox_strokeColor, Color.TRANSPARENT)
                alertBackgroundColor = getColor(R.styleable.CoreAlertBox_backgroundColor, Color.TRANSPARENT)
                textAppearance = getResourceId(R.styleable.CoreAlertBox_textAppearance, textAppearance)
                textColor = getColor(R.styleable.CoreAlertBox_textColor, textColor)
                getDrawable(R.styleable.CoreAlertBox_startIcon)?.let { startIcon = it }
                startIconTint = if (hasValue(R.styleable.CoreAlertBox_startIconTint)) {
                    getColor(R.styleable.CoreAlertBox_startIconTint, Color.TRANSPARENT)
                } else null
                getDrawable(R.styleable.CoreAlertBox_closeIcon)?.let { closeIcon = it }
                closeIconTint = getColor(R.styleable.CoreAlertBox_closeIconTint, closeIconTint)
                actionTextAppearance = getResourceId(R.styleable.CoreAlertBox_actionTextAppearance, actionTextAppearance)
                actionTextColor = getColor(R.styleable.CoreAlertBox_actionTextColor, textColor)
                emphasisAppearance = getResourceId(R.styleable.CoreAlertBox_alertEmphasisAppearance, 0)
                val horizontal = getDimensionPixelSize(R.styleable.CoreAlertBox_alertPaddingHorizontal, 0)
                val vertical = getDimensionPixelSize(R.styleable.CoreAlertBox_alertPaddingVertical, 0)
                views.content.setPadding(horizontal, vertical, horizontal, vertical)
                contentGap = getDimensionPixelSize(R.styleable.CoreAlertBox_alertContentGap, contentGap)
                val iconSize = getDimensionPixelSize(R.styleable.CoreAlertBox_alertIconSize, 16.dp.roundToInt())
                views.icon.layoutParams = views.icon.layoutParams.apply {
                    width = iconSize
                    height = iconSize
                }
                if (!views.isLegacy) {
                    views.close.layoutParams = views.close.layoutParams.apply {
                        width = iconSize
                        height = iconSize
                    }
                }
                val buttonStyle = getResourceId(R.styleable.CoreAlertBox_alertButtonRecipe, 0)
                if (buttonStyle != 0) views.button.buttonRecipe = buttonStyle
            }
            updateContentConstraints()
        } finally {
            isApplyingRecipe = false
        }
        renderMessage()
    }

    private fun renderMessage() {
        if (isApplyingRecipe) return
        if (!isHtml) {
            views.message.text = text
        } else {
            val parsed = HtmlRenderer(HtmlRendererConfig(), FontManager(context)).render(text.toString(), views.message)
            var styled: CharSequence = parsed
            if (emphasisAppearance != 0) {
                context.withStyledAttributes(emphasisAppearance, intArrayOf(android.R.attr.fontFamily, android.R.attr.textSize)) {
                    styled = buildHighlightedMessage(context, parsedMessage = parsed,
                        highlightedTextAppearance = TextStyle(textColor, getResourceId(0, 0),
                            getDimension(1, views.message.textSize), views.message.lineHeight.toFloat()))
                }
            }
            views.message.text = styled
        }
        views.message.movementMethod = if (views.message.text is android.text.Spanned &&
            (views.message.text as android.text.Spanned).getSpans(0, views.message.text.length,
                android.text.style.ClickableSpan::class.java).isNotEmpty()) LinkMovementMethod.getInstance() else null
    }

    private fun updateActionIcon() {
        val drawable = actionEndIcon?.constantState?.newDrawable()?.mutate() ?: actionEndIcon?.mutate()
        drawable?.setTint(actionEndIconTint ?: actionTextColor)
        views.action.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null)
    }

    private fun updateContentConstraints() {
        if (views.isLegacy && !isActionVisible) {
            val constraints = ConstraintSet().apply { clone(views.content) }
            constraints.connect(views.message.id, ConstraintSet.END, views.close.id, ConstraintSet.START, 8.dp.roundToInt())
            constraints.applyTo(views.content)
        } else {
            val constraints = ConstraintSet().apply { clone(views.content) }
            constraints.connect(views.message.id, ConstraintSet.START,
                if (isStartIconVisible) views.icon.id else ConstraintSet.PARENT_ID,
                if (isStartIconVisible) ConstraintSet.END else ConstraintSet.START,
                if (isStartIconVisible) contentGap else 0)
            constraints.connect(views.message.id, ConstraintSet.END,
                when {
                    isActionVisible -> views.action.id
                    isCloseIconVisible -> views.close.id
                    else -> ConstraintSet.PARENT_ID
                },
                if (isActionVisible || isCloseIconVisible) ConstraintSet.START else ConstraintSet.END,
                if (isActionVisible || isCloseIconVisible) contentGap else 0)
            if (isActionVisible) {
                constraints.connect(views.action.id, ConstraintSet.END,
                    if (isCloseIconVisible) views.close.id else ConstraintSet.PARENT_ID,
                    if (isCloseIconVisible) ConstraintSet.START else ConstraintSet.END,
                    if (isCloseIconVisible) contentGap else 0)
                constraints.connect(views.action.id, ConstraintSet.TOP, views.message.id, ConstraintSet.TOP)
                constraints.connect(views.action.id, ConstraintSet.BOTTOM, views.message.id, ConstraintSet.BOTTOM)
            }
            if (!views.isLegacy) constraints.setMargin(views.button.id, ConstraintSet.TOP, contentGap)
            constraints.applyTo(views.content)
        }
        updateIconAlignment()
    }

    private fun updateIconAlignment() {
        val constraints = ConstraintSet().apply { clone(views.content) }
        constraints.clear(views.icon.id, ConstraintSet.TOP)
        constraints.clear(views.icon.id, ConstraintSet.BOTTOM)
        if (iconAlignment != IconAlignment.BOTTOM) constraints.connect(views.icon.id, ConstraintSet.TOP,
            views.message.id, ConstraintSet.TOP,
            if (iconAlignment == IconAlignment.TOP && isOpticalSpacingEnabled) opticalOffset else 0)
        if (iconAlignment != IconAlignment.TOP) constraints.connect(views.icon.id, ConstraintSet.BOTTOM,
            views.message.id, ConstraintSet.BOTTOM)
        constraints.applyTo(views.content)
    }

    private fun updateTouchTargets() {
        if (views.isLegacy || width == 0 || height == 0) return
        val actions = listOf(views.close, views.action, views.button).filter { it.isVisible }
        val delegates = actions.map { action ->
            val bounds = Rect().also {
                action.getDrawingRect(it)
                offsetDescendantRectToMyCoords(action, it)
            }
            val half = 24.dp.roundToInt()
            val cx = bounds.centerX()
            val cy = bounds.centerY()
            bounds.union(cx - half, cy - half, cx + half, cy + half)
            if (bounds.right > width) bounds.offset(width - bounds.right, 0)
            if (bounds.left < 0) bounds.offset(-bounds.left, 0)
            if (bounds.bottom > height) bounds.offset(0, height - bounds.bottom)
            if (bounds.top < 0) bounds.offset(0, -bounds.top)
            bounds.intersect(0, 0, width, height)
            TouchDelegate(bounds, action)
        }
        touchDelegate = object : TouchDelegate(Rect(), this) {
            override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
                for (delegate in delegates) {
                    val copy = android.view.MotionEvent.obtain(event)
                    val handled = delegate.onTouchEvent(copy)
                    copy.recycle()
                    if (handled) return true
                }
                return false
            }
        }
    }

    /** Adds the legacy 2dp offset when the start icon is top-aligned. */
    protected fun setOpticalSpacingEnabled(enable: Boolean) {
        isOpticalSpacingEnabled = enable
        opticalOffset = if (enable) 2.dp.toInt() else 0
        updateIconAlignment()
    }

    protected fun applyTypeAppearance(@ColorInt background: Int, @ColorInt border: Int, icon: Drawable) {
        views.card.setCardBackgroundColor(background)
        views.card.strokeColor = border
        views.icon.setImageDrawable(icon)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (views.isLegacy) return
        val metrics = views.message.paint.fontMetricsInt
        val lineHeight = maxOf(views.message.lineHeight, metrics.descent - metrics.ascent)
        val desired = (views.message.layout?.lineCount ?: 1) * lineHeight
        if (views.message.minHeight != desired || views.message.lineHeight < lineHeight) {
            TextViewCompat.setLineHeight(views.message, lineHeight)
            views.message.minHeight = desired
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        updateTouchTargets()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (childCount == 0 || views.isLegacy) return
        views.close.isEnabled = enabled
        views.action.isEnabled = enabled
        views.button.isEnabled = enabled
        views.message.isEnabled = enabled
    }

    enum class IconAlignment { TOP, CENTER, BOTTOM }

    private data class AlertViews(
        val card: MaterialCardView,
        val content: ConstraintLayout,
        val message: MaterialTextView,
        val icon: ShapeableImageView,
        val close: View,
        val button: CoreButton,
        val action: MaterialTextView,
        val isLegacy: Boolean
    )
}
