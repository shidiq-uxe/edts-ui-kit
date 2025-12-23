package id.co.edtslib.uikit.viewgroup

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import id.co.edtslib.uikit.R

class GreyScaleConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var disabled = false
        set(value) {
            if (field != value) {
                field = value
                if (enableAnimation) {
                    animateToState(value)
                } else {
                    grayscaleProgress = if (value) 1f else 0f
                    invalidate()
                }
            }
        }

    var enableAnimation = false
    var animationDuration = 300L
    var autoDisableTouch = true
    var exemptViewIds = setOf<Int>()

    private var grayscaleProgress = 0f
    private var currentAnimator: ValueAnimator? = null
    private val paint = Paint()
    private val colorMatrix = ColorMatrix()

    init {
        updateColorMatrix()

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.GreyScaleConstraintLayout)
            try {
                disabled = typedArray.getBoolean(R.styleable.GreyScaleConstraintLayout_disabled, false)
                enableAnimation = typedArray.getBoolean(R.styleable.GreyScaleConstraintLayout_enableAnimation, true)
                animationDuration = typedArray.getInt(R.styleable.GreyScaleConstraintLayout_animationDuration, 300).toLong()
                autoDisableTouch = typedArray.getBoolean(R.styleable.GreyScaleConstraintLayout_autoDisableTouch, true)

                val exemptIdsString = typedArray.getString(R.styleable.GreyScaleConstraintLayout_exemptViewIds)
                exemptIdsString?.let { ids ->
                    exemptViewIds = ids.split(",")
                        .mapNotNull { id ->
                            val resId = resources.getIdentifier(id.trim(), "id", context.packageName)
                            if (resId != 0) resId else null
                        }
                        .toSet()
                }
            } finally {
                typedArray.recycle()
            }
        }

        grayscaleProgress = if (disabled) 1f else 0f
    }

    private fun updateColorMatrix() {
        if (grayscaleProgress > 0f) {
            colorMatrix.setSaturation(1f - grayscaleProgress)
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        } else {
            paint.colorFilter = null
        }
    }

    private fun animateToState(toDisabled: Boolean) {
        currentAnimator?.cancel()

        currentAnimator = ValueAnimator.ofFloat(grayscaleProgress, if (toDisabled) 1f else 0f).apply {
            duration = animationDuration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                grayscaleProgress = animator.animatedValue as Float
                updateColorMatrix()
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (grayscaleProgress > 0f && exemptViewIds.isNotEmpty()) {
            drawExemptViews(canvas, exempt = true)

            val layerId = canvas.saveLayer(null, paint)
            drawExemptViews(canvas, exempt = false)
            canvas.restoreToCount(layerId)
        } else if (grayscaleProgress > 0f) {
            val layerId = canvas.saveLayer(null, paint)
            super.dispatchDraw(canvas)
            canvas.restoreToCount(layerId)
        } else {
            super.dispatchDraw(canvas)
        }
    }

    override fun draw(canvas: Canvas) {
        if (grayscaleProgress > 0f && exemptViewIds.isEmpty()) {
            val layerId = canvas.saveLayer(null, paint)
            super.draw(canvas)
            canvas.restoreToCount(layerId)
        } else {
            super.draw(canvas)
        }
    }

    private fun drawExemptViews(canvas: Canvas, exempt: Boolean) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val isExempt = exemptViewIds.contains(child.id)

            if (isExempt == exempt) {
                canvas.save()
                canvas.translate(child.left.toFloat(), child.top.toFloat())
                child.draw(canvas)
                canvas.restore()
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return (disabled && autoDisableTouch) || super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (disabled && autoDisableTouch) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        currentAnimator?.cancel()
        currentAnimator = null
    }

    fun setDisabledWithAnimation(disabled: Boolean, animate: Boolean = enableAnimation) {
        val previousSetting = enableAnimation
        enableAnimation = animate
        this.disabled = disabled
        enableAnimation = previousSetting
    }

    fun addExemptView(viewId: Int) {
        exemptViewIds = exemptViewIds + viewId
        if (disabled) invalidate()
    }

    fun removeExemptView(viewId: Int) {
        exemptViewIds = exemptViewIds - viewId
        if (disabled) invalidate()
    }

    fun clearExemptViews() {
        exemptViewIds = emptySet()
        if (disabled) invalidate()
    }
}