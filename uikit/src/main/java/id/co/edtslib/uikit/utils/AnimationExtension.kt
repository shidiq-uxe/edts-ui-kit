package id.co.edtslib.uikit.utils

import android.view.View
import android.view.animation.ScaleAnimation

fun View.scaleUp(duration: Long = 300, pivotX: Float = 0.5f, pivotY: Float = 0.5f) {
    val scaleAnimation = ScaleAnimation(
        0.9f, 1.0f, // Start and end values for the X axis scaling
        0.9f, 1.0f, // Start and end values for the Y axis scaling
        ScaleAnimation.RELATIVE_TO_SELF, pivotX, // Pivot point of X scaling
        ScaleAnimation.RELATIVE_TO_SELF, pivotY // Pivot point of Y scaling
    ).apply {
        this.duration = duration
        fillAfter = true // If fillAfter is true, the transformation that this animation performed will persist when it is finished
    }
    startAnimation(scaleAnimation)
}

fun View.scaleDown(duration: Long = 300, pivotX: Float = 0.5f, pivotY: Float = 0.5f) {
    val scaleAnimation = ScaleAnimation(
        1.0f, 0.9f,
        1.0f, 0.9f,
        ScaleAnimation.RELATIVE_TO_SELF, pivotX,
        ScaleAnimation.RELATIVE_TO_SELF, pivotY
    ).apply {
        this.duration = duration
        fillAfter = true
    }
    startAnimation(scaleAnimation)
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