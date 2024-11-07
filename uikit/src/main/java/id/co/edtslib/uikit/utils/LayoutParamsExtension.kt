package id.co.edtslib.uikit.utils

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlin.math.roundToInt

infix fun View.startToStartOf(target: View) {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        connect(id, ConstraintSet.START, target.id, ConstraintSet.START)
        applyTo(parent as ConstraintLayout)
    }
}

infix fun View.startToEndOf(target: View) {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        connect(id, ConstraintSet.START, target.id, ConstraintSet.END)
        applyTo(parent as ConstraintLayout)
    }
}

infix fun View.endToEndOf(target: View) {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        connect(id, ConstraintSet.END, target.id, ConstraintSet.END)
        applyTo(parent as ConstraintLayout)
    }
}

infix fun View.endToStartOf(target: View) {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        connect(id, ConstraintSet.END, target.id, ConstraintSet.START)
        applyTo(parent as ConstraintLayout)
    }
}

infix fun View.topToTopOf(target: View) {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        connect(id, ConstraintSet.TOP, target.id, ConstraintSet.TOP)
        applyTo(parent as ConstraintLayout)
    }
}

infix fun View.topToBottomOf(target: View) {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        connect(id, ConstraintSet.TOP, target.id, ConstraintSet.BOTTOM)
        applyTo(parent as ConstraintLayout)
    }
}

infix fun View.bottomToBottomOf(target: View) {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        connect(id, ConstraintSet.BOTTOM, target.id, ConstraintSet.BOTTOM)
        applyTo(parent as ConstraintLayout)
    }
}

infix fun View.bottomToTopOf(target: View) {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        connect(id, ConstraintSet.BOTTOM, target.id, ConstraintSet.TOP)
        applyTo(parent as ConstraintLayout)
    }
}

infix fun View.horizontalWeight(weight: Float) {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        setHorizontalWeight(id, weight)
        applyTo(parent as ConstraintLayout)
    }
}

infix fun View.verticalWeight(weight: Float) {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        setVerticalWeight(id, weight)
        applyTo(parent as ConstraintLayout)
    }
}

fun View.disconnectTop() {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        clear(id, ConstraintSet.TOP)
        applyTo(parent as ConstraintLayout)
    }
}

fun View.disconnectBottom() {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        clear(id, ConstraintSet.BOTTOM)
        applyTo(parent as ConstraintLayout)
    }
}

fun View.disconnectStart() {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        clear(id, ConstraintSet.START)
        applyTo(parent as ConstraintLayout)
    }
}

fun View.disconnectEnd() {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        clear(id, ConstraintSet.END)
        applyTo(parent as ConstraintLayout)
    }
}

fun View.disconnectVerticalChain() {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        removeFromVerticalChain(id)
        applyTo(parent as ConstraintLayout)
    }
}

fun View.disconnectHorizontalChain() {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        removeFromHorizontalChain(id)
        applyTo(parent as ConstraintLayout)
    }
}

fun View.disconnectChains() {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        removeFromVerticalChain(id)
        removeFromHorizontalChain(id)
        applyTo(parent as ConstraintLayout)
    }
}

fun View.setRatio( ratio: String) {
    ConstraintSet().apply {
        clone(parent as ConstraintLayout)
        setDimensionRatio(id, ratio)
        applyTo(parent as ConstraintLayout)
    }
}

fun View.marginStart(parentConstraint: ViewGroup, marginValue: Float) {
    val params = when (parentConstraint) {
        is LinearLayout -> {
            layoutParams as LinearLayout.LayoutParams
        }

        is FrameLayout -> {
            layoutParams as FrameLayout.LayoutParams
        }

        is RelativeLayout -> {
            layoutParams as RelativeLayout.LayoutParams
        }

        is ConstraintLayout -> {
            layoutParams as ConstraintLayout.LayoutParams
        }

        else -> {
            null
        }
    }
    params?.let {
        it.marginStart = marginValue.roundToInt()
    }
}

fun View.marginEnd(parentConstraint: ViewGroup, marginValue: Float) {
    val params = when (parentConstraint) {
        is LinearLayout -> {
            layoutParams as LinearLayout.LayoutParams
        }

        is FrameLayout -> {
            layoutParams as FrameLayout.LayoutParams
        }

        is RelativeLayout -> {
            layoutParams as RelativeLayout.LayoutParams
        }

        is ConstraintLayout -> {
            layoutParams as ConstraintLayout.LayoutParams
        }

        else -> {
            null
        }
    }
    params?.let {
        it.marginEnd = marginValue.roundToInt()
    }
}

fun View.marginTop(parentConstraint: ViewGroup, marginValue: Float) {
    val params = when (parentConstraint) {
        is LinearLayout -> {
            layoutParams as LinearLayout.LayoutParams
        }

        is FrameLayout -> {
            layoutParams as FrameLayout.LayoutParams
        }

        is RelativeLayout -> {
            layoutParams as RelativeLayout.LayoutParams
        }

        is ConstraintLayout -> {
            layoutParams as ConstraintLayout.LayoutParams
        }

        else -> {
            null
        }
    }
    params?.let {
        it.topMargin = marginValue.roundToInt()
    }
}

fun View.marginBottom(parentConstraint: ViewGroup, marginValue: Float) {
    val params = when (parentConstraint) {
        is LinearLayout -> {
            layoutParams as LinearLayout.LayoutParams
        }

        is FrameLayout -> {
            layoutParams as FrameLayout.LayoutParams
        }

        is RelativeLayout -> {
            layoutParams as RelativeLayout.LayoutParams
        }

        is ConstraintLayout -> {
            layoutParams as ConstraintLayout.LayoutParams
        }

        else -> {
            null
        }
    }
    params?.let {
        it.bottomMargin = marginValue.roundToInt()
    }
}

fun View.marginHorizontal(constraintViewGroup: ConstraintLayout, horizontalMargin: Float) {
    this.marginStart(constraintViewGroup, horizontalMargin)
    this.marginEnd(constraintViewGroup, horizontalMargin)
}

fun View.marginVertical(constraintViewGroup: ConstraintLayout, horizontalMargin: Float) {
    this.marginTop(constraintViewGroup, horizontalMargin)
    this.marginTop(constraintViewGroup, horizontalMargin)
}

fun View.verticalBias(verticalOffsetPercent: Float) {
    (layoutParams as ConstraintLayout.LayoutParams).verticalBias = verticalOffsetPercent
}

fun View.horizontalBias(horizontalOffsetPercent: Float) {
    (layoutParams as ConstraintLayout.LayoutParams).horizontalBias = horizontalOffsetPercent
}
