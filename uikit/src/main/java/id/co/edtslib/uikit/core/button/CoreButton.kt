package id.co.edtslib.uikit.core.button

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.StyleRes
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.TextViewCompat
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.attachShimmerEffect
import id.co.edtslib.uikit.utils.detachShimmerEffect
import id.co.edtslib.uikit.utils.typefaceFromAppearance
import kotlin.math.max

/**
 * Shared button behavior. A recipe is an XML widget style, resolved in this view's themed context.
 * Use either the start/end slots or MaterialButton's single-icon API, not both on the same button.
 */
open class CoreButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.coreButtonStyle
) : MaterialButton(context, attrs, defStyleAttr) {

    @get:StyleRes
    var buttonRecipe: Int = 0
        set(@StyleRes value) {
            field = value
            if (value != 0) applyRecipe(value)
        }

    var startIcon: Drawable? = null
        set(value) {
            field = value?.mutate()
            updateSlotIcons()
        }

    var endIcon: Drawable? = null
        set(value) {
            field = value?.mutate()
            updateSlotIcons()
        }

    // Only call this variable only when shouldShowShimmer is true
    var shimmerFrameLayout: ShimmerFrameLayout? = null

    var shouldShowShimmer = false
        set(value) {
            field = value
            scheduleShimmerUpdate()
        }

    var pressedScale = DEFAULT_SCALE_VALUE

    var isDestructive: Boolean = false
        set(value) {
            field = value
            refreshDrawableState()
        }

    private var hasSlotIcons = false
    private var isChangingShimmerParent = false
    private var detachedShimmerFrameLayout: ShimmerFrameLayout? = null
    private var recipeLineHeight: Int? = null
    private var recipeTextSize = 0f
    private val shimmerUpdate = Runnable { updateShimmer() }

    init {
        this.context.withStyledAttributes(attrs, R.styleable.CoreButton, defStyleAttr, 0) {
            buttonRecipe = getResourceId(R.styleable.CoreButton_buttonRecipe, 0)
        }
        if (buttonRecipe != 0) {
            // Explicit view attributes override the recipe only during inflation.
            applyRecipe(buttonRecipe, attrs)
            this.context.withStyledAttributes(attrs, R.styleable.CoreButton, defStyleAttr, 0) {
                startIcon = getDrawable(R.styleable.CoreButton_startIcon)
                endIcon = getDrawable(R.styleable.CoreButton_endIcon)
                shouldShowShimmer = getBoolean(R.styleable.CoreButton_shouldShowShimmer, false)
                isDestructive = getBoolean(R.styleable.CoreButton_isDestructive, false)
                pressedScale = getFloat(R.styleable.CoreButton_pressedScale, pressedScale)
            }
        }
    }

    private fun applyRecipe(@StyleRes recipe: Int, attrs: AttributeSet? = null) {
        ButtonStyleResolver.applyAttributes(context, this, recipe, attrs)
        context.withStyledAttributes(attrs, R.styleable.CoreButton, 0, recipe) {
            minWidth = getDimensionPixelSize(R.styleable.CoreButton_android_minWidth, 0)
            minHeight = getDimensionPixelSize(R.styleable.CoreButton_android_minHeight, 0)
            includeFontPadding = getBoolean(R.styleable.CoreButton_android_includeFontPadding, false)
            isAllCaps = getBoolean(R.styleable.CoreButton_android_textAllCaps, false)
            pressedScale = getFloat(R.styleable.CoreButton_pressedScale, pressedScale)
            cornerRadius = getDimensionPixelSize(R.styleable.CoreButton_cornerRadius, cornerRadius)
            // Material's radius getter can retain a value after a shape appearance replaces its model.
            shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                .setAllCornerSizes(cornerRadius.toFloat())
                .build()
            val animator = getResourceId(R.styleable.CoreButton_android_stateListAnimator, 0)
            stateListAnimator = if (animator != 0) {
                android.animation.AnimatorInflater.loadStateListAnimator(context, animator)
            } else {
                null
            }
            elevation = getDimension(R.styleable.CoreButton_android_elevation, 0f)
        }
        context.withStyledAttributes(attrs, intArrayOf(android.R.attr.textAppearance), 0, recipe) {
            val appearance = getResourceId(0, 0)
            if (appearance != 0) {
                context.typefaceFromAppearance(appearance)?.let { typeface = it }
                context.withStyledAttributes(
                    appearance,
                    intArrayOf(androidx.appcompat.R.attr.lineHeight, android.R.attr.textSize)
                ) {
                    recipeLineHeight = if (hasValue(0)) getDimensionPixelSize(0, 0) else null
                    recipeTextSize = getDimension(1, textSize)
                    recipeLineHeight?.let { TextViewCompat.setLineHeight(this@CoreButton, it) }
                }
            }
        }
        context.withStyledAttributes(attrs, intArrayOf(android.R.attr.textSize), 0, recipe) {
            if (hasValue(0)) setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, getDimension(0, textSize))
        }
        updateSlotIcons()
        requestLayout()
    }

    private fun prepareSlot(drawable: Drawable?): Drawable? = drawable?.apply {
        val size = iconSize.takeIf { it > 0 } ?: intrinsicWidth.coerceAtLeast(0)
        setBounds(0, 0, size, size)
        DrawableCompat.setTintList(this, iconTint)
        state = drawableState
    }

    private fun updateSlotIcons() {
        if (startIcon == null && endIcon == null && !hasSlotIcons) return
        hasSlotIcons = startIcon != null || endIcon != null
        compoundDrawablePadding = iconPadding
        super.setCompoundDrawablesRelative(prepareSlot(startIcon), null, prepareSlot(endIcon), null)
    }

    private fun scheduleShimmerUpdate() {
        removeCallbacks(shimmerUpdate)
        if (isAttachedToWindow && !isChangingShimmerParent) post(shimmerUpdate)
    }

    private fun updateShimmer() {
        if (!isAttachedToWindow || isChangingShimmerParent) return
        isChangingShimmerParent = true
        try {
            if (shouldShowShimmer) {
                if (shimmerFrameLayout == null) shimmerFrameLayout = attachShimmerEffect()
                shimmerFrameLayout?.startShimmer()
            } else {
                shimmerFrameLayout?.detachShimmerEffect()
                shimmerFrameLayout = null
            }
        } finally {
            isChangingShimmerParent = false
        }
    }

    override fun setCompoundDrawablesRelative(
        start: Drawable?,
        top: Drawable?,
        end: Drawable?,
        bottom: Drawable?
    ) {
        if (startIcon != null || endIcon != null) {
            super.setCompoundDrawablesRelative(prepareSlot(startIcon), top, prepareSlot(endIcon), bottom)
        } else {
            super.setCompoundDrawablesRelative(start, top, end, bottom)
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        prepareSlot(startIcon)
        prepareSlot(endIcon)
    }

    override fun setIconSize(iconSize: Int) {
        super.setIconSize(iconSize)
        updateSlotIcons()
    }

    override fun setIconTint(iconTint: android.content.res.ColorStateList?) {
        super.setIconTint(iconTint)
        updateSlotIcons()
    }

    override fun setIconPadding(iconPadding: Int) {
        super.setIconPadding(iconPadding)
        updateSlotIcons()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val lineBox = recipeLineHeight ?: return
        // TextView's last line ignores lineSpacingExtra on older Android versions.
        // Match the XML line box while allowing larger fonts and multiline labels to grow.
        val fontHeight = paint.fontMetricsInt.let { it.descent - it.ascent }
        val textLineHeight = if (resources.configuration.fontScale > 1f || textSize > recipeTextSize + 0.5f) {
            max(lineBox, fontHeight)
        } else {
            lineBox
        }
        val textHeight = if (text.isNullOrEmpty()) 0 else textLineHeight * (layout?.lineCount ?: 1)
        val iconHeight = max(startIcon?.bounds?.height() ?: 0, endIcon?.bounds?.height() ?: 0)
            .coerceAtLeast(icon?.bounds?.height() ?: 0)
        val desiredHeight = max(minHeight, max(textHeight, iconHeight) + paddingTop + paddingBottom)
        setMeasuredDimension(measuredWidthAndState, resolveSizeAndState(desiredHeight, heightMeasureSpec, 0))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (parent === detachedShimmerFrameLayout) shimmerFrameLayout = detachedShimmerFrameLayout
        detachedShimmerFrameLayout = null
        scheduleShimmerUpdate()
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val state = super.onCreateDrawableState(extraSpace + 1)
        if (isDestructive) mergeDrawableStates(state, STATE_DESTRUCTIVE)
        return state
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (isEnabled) {
                when (it.actionMasked) {
                    MotionEvent.ACTION_DOWN -> animate().scaleX(pressedScale).scaleY(pressedScale)
                        .setDuration(PRESS_DURATION).start()
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> animate().scaleX(1f).scaleY(1f)
                        .setDuration(PRESS_DURATION).start()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (!enabled) {
            animate().cancel()
            scaleX = 1f
            scaleY = 1f
            isPressed = false
        }
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(shimmerUpdate)
        if (!isChangingShimmerParent) {
            shimmerFrameLayout?.stopShimmer()
            detachedShimmerFrameLayout = shimmerFrameLayout
            shimmerFrameLayout = null
        }
        animate().cancel()
        scaleX = 1f
        scaleY = 1f
        super.onDetachedFromWindow()
    }

    companion object {
        private const val DEFAULT_SCALE_VALUE = 0.95f
        private const val PRESS_DURATION = 100L
        private val STATE_DESTRUCTIVE = intArrayOf(R.attr.state_destructive)
    }
}
