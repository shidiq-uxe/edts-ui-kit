package id.co.edtslib.uikit.utils

import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.ScaleAnimation
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import id.co.edtslib.uikit.R

fun View.scaleUpAnimation(duration: Long = 300, pivotX: Float = 0.5f, pivotY: Float = 0.5f) =  ScaleAnimation(
    0.9f, 1.0f, // Start and end values for the X axis scaling
    0.9f, 1.0f, // Start and end values for the Y axis scaling
    ScaleAnimation.RELATIVE_TO_SELF, pivotX, // Pivot point of X scaling
    ScaleAnimation.RELATIVE_TO_SELF, pivotY // Pivot point of Y scaling
).apply {
    this.duration = duration
    fillAfter = true // If fillAfter is true, the transformation that this animation performed will persist when it is finished
}

fun View.scaleDownAnimation(duration: Long = 300, pivotX: Float = 0.5f, pivotY: Float = 0.5f) =  ScaleAnimation(
    1.0f, 0.9f,
    1.0f, 0.9f,
    ScaleAnimation.RELATIVE_TO_SELF, pivotX,
    ScaleAnimation.RELATIVE_TO_SELF, pivotY
).apply {
    this.duration = duration
    fillAfter = true
}


fun View.resetScale(duration: Long = 300) {
    val scaleAnimation = ScaleAnimation(
        scaleX, 1.0f,
        scaleY, 1.0f,
        ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
        ScaleAnimation.RELATIVE_TO_SELF, 0.5f
    ).apply {
        this.duration = duration
        fillAfter = true
    }
    startAnimation(scaleAnimation)
}

fun View.vibrateAnimation(duration: Long = 300) {
    val shake = ObjectAnimator.ofFloat(this, "translationX", 0f, 10f, -10f, 10f, -10f, 5f, -5f, 0f)
    shake.duration = duration
    shake.start()
}

// Extension function to animate the error text fade-in and upward translation
fun View.animateErrorIn(
    duration: Long = 300,
    translationYStart: Float = 20f,
    alphaStart: Float = 0f,
    alphaEnd: Float = 1f
) {
    alpha = alphaStart
    translationY = translationYStart
    animate()
        .alpha(alphaEnd)
        .translationY(0f)
        .setDuration(duration)
        .start()
}

// Extension function to animate the error text fade-out and downward translation
fun View.animateErrorOut(
    duration: Long = 300,
    translationYEnd: Float = 20f,
    alphaEnd: Float = 0f,
    onAnimationEnd: (() -> Unit)? = null
) {
    animate()
        .alpha(alphaEnd)
        .translationY(translationYEnd)
        .setDuration(duration)
        .withEndAction {
            onAnimationEnd?.invoke()
        }
        .start()
}

// Extension function to animate the error text fade-in and upward translation
fun View.fade(
    duration: Long = 200,
    alphaStart: Float = 0f,
    alphaEnd: Float = 1f
): ViewPropertyAnimator {
    alpha = alphaStart
    return animate()
        .alpha(alphaEnd)
        .setDuration(duration).also {
            it.start()
        }
}

class FloatingAnimationHelper(private val context: Context) {
    /**
     * Show floating animation from a source view
     * @param anchor The view to animate from (typically the clicked button)
     * @param icon Resource ID for the icon drawable
     * @param text Text to display (e.g., "+1")
     * @param duration Animation duration in milliseconds
     */
    fun showFloatingAnimation(
        anchor: View,
        icon: Int? = null,
        text: String = "+1",
        duration: Long = 1500L,
        floatDistance: Float = 200f,
        onEnd: () -> Unit = {}
    ) {
        val parent = anchor.rootView as? ViewGroup ?: return

        val floatingView = createFloatingView(icon, text)

        val location = IntArray(2)
        anchor.getLocationInWindow(location)

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            leftMargin = location[0] + (anchor.width / 2) - 50
            topMargin = location[1] - 50
        }

        if (parent is FrameLayout) {
            parent.addView(floatingView, params)
        } else {
            val overlay = FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            parent.addView(overlay)
            overlay.addView(floatingView, params)
        }

        animateFloating(floatingView, floatDistance, duration) {
            (floatingView.parent as? ViewGroup)?.removeView(floatingView)
            onEnd.invoke()
        }
    }

    private fun createFloatingView(icon: Int?, text: String): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
        }

        icon?.let {
            val iconSize = context.dimenPixelSize(R.dimen.s) ?: 16.dp.toInt()

            val iconView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
                    marginEnd = 8
                }
                setImageResource(it)
            }
            container.addView(iconView)
        }

        val textView = TextView(context).apply {
            this.text = text
            setTextAppearance(R.style.TextAppearance_Inter_Bold_B3)
            setTextColor(context.color(R.color.primary_30))
        }
        container.addView(textView)

        return container
    }

    private fun animateFloating(
        view: View,
        distance: Float,
        duration: Long,
        onEnd: () -> Unit
    ) {
        val translateY = ObjectAnimator.ofFloat(view, "translationY", 0f, -distance).apply {
            this.duration = duration
            interpolator = AccelerateInterpolator()
        }

        val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
            this.duration = (duration * 0.6).toLong()
            startDelay = duration / 6
        }

        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f).apply {
            this.duration = duration / 2
        }
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f).apply {
            this.duration = duration / 2
        }

        AnimatorSet().apply {
            playTogether(translateY, fadeOut, scaleX, scaleY)
            addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onEnd()
                }
                override fun onAnimationCancel(animation: android.animation.Animator) {
                    onEnd()
                }
                override fun onAnimationRepeat(animation: android.animation.Animator) {}
            })
            start()
        }
    }
}

fun View.showFloatingAnimation(
    icon: Int? = null,
    text: String = "+1",
    duration: Long = 900L,
    floatDistance: Float = 200f,
    onEnd: () -> Unit = {}
) {
    FloatingAnimationHelper(context).showFloatingAnimation(
        anchor = this,
        icon = icon,
        text = text,
        duration = duration,
        floatDistance = floatDistance,
        onEnd = onEnd
    )
}