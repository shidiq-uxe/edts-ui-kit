package id.co.edtslib.uikit.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import id.co.edtslib.uikit.R

private val dividerDefaultDimension = R.dimen.dimen_0

class DividerItemDecoration(
    private val context: Context,
    private val orientation: Int,
    @DrawableRes private val drawableRes: Int = 0,
    @DimenRes private val topInset: Int = dividerDefaultDimension,
    @DimenRes private val leftInset: Int = dividerDefaultDimension,
    @DimenRes private val rightInset: Int = dividerDefaultDimension,
    @DimenRes private val bottomInset: Int = dividerDefaultDimension
) : DividerItemDecoration(context, orientation) {

    constructor(
        context: Context,
        orientation: Int,
        @DimenRes inset: Int = dividerDefaultDimension,
        @DrawableRes drawableRes: Int = 0
    ) : this(context, orientation, drawableRes, inset, inset, inset, inset)

    constructor(
        context: Context,
        orientation: Int,
        @DrawableRes drawableRes: Int = 0,
        @DimenRes verticalInset: Int = dividerDefaultDimension,
        @DimenRes horizontalInset: Int = dividerDefaultDimension
    ) : this(context, orientation, drawableRes, verticalInset, horizontalInset, horizontalInset, verticalInset)

    private val divider: Drawable? get() = when {
        drawableRes != 0 ->  AppCompatResources.getDrawable(context, drawableRes)

        topInset != dividerDefaultDimension || leftInset != dividerDefaultDimension
                || rightInset != dividerDefaultDimension || bottomInset != dividerDefaultDimension ->
            context.obtainStyledAttributes(ATTRS) .let { typedArray ->
                typedArray.getDrawable(0).let { dividerDrawable ->
                    val topDimension = context.resources.getDimensionPixelSize(topInset)
                    val leftDimension = context.resources.getDimensionPixelSize(leftInset)
                    val rightDimension = context.resources.getDimensionPixelSize(rightInset)
                    val bottomDimension = context.resources.getDimensionPixelSize(bottomInset)

                    InsetDrawable(
                        dividerDrawable,
                        leftDimension,
                        topDimension,
                        rightDimension,
                        bottomDimension
                    ).also {
                        typedArray.recycle()
                    }
                }
            }

        else -> context.obtainStyledAttributes(ATTRS).let { typedArray ->
            typedArray.getDrawable(0).also {
                typedArray.recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)

        repeat(parent.childCount) {
            divider?.draw(canvas)
        }
    }

    companion object {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)
    }
}