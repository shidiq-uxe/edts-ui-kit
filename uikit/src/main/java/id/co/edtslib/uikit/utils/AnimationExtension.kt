package id.co.edtslib.uikit.utils

import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.ScaleAnimation

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