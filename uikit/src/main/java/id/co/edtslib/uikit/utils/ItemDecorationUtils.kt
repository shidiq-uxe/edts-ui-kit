package id.co.edtslib.uikit.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
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

private const val startIndex = 0

fun gridMarginItemDecoration(
    spanCount: Int,
    spacing: MarginItem,
    includeEdge: Boolean
) = object : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column: Int = position % spanCount

        if (includeEdge) {
            outRect.left =
                spacing.left - column * spacing.left / spanCount // spacing - column * ((1f / spanCount) * spacing)
            outRect.right =
                (column + 1) * spacing.right / spanCount // (column + 1) * ((1f / spanCount) * spacing)
            if (position < spanCount) { // top edge
                outRect.top = spacing.first
            }
            outRect.bottom = spacing.last // item bottom
        } else {
            outRect.left =
                column * spacing.left / spanCount // column * ((1f / spanCount) * spacing)
            outRect.right =
                spacing.right - (column + 1) * spacing.right / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= spanCount) {
                outRect.top = spacing.top // item top
            }
        }
    }
}

fun linearMarginItemDecoration(
    orientation: Int,
    margin: MarginItem
) = object : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        with(outRect) {
            val childCount = parent.adapter?.itemCount ?: 0
            val childAdapterPosition = parent.getChildAdapterPosition(view)

            val isOnStartIndex = margin.first != startIndex && childAdapterPosition == startIndex
            val isOnLastIndex =
                margin.last != startIndex && childAdapterPosition == childCount.minus(1)

            if (orientation == LinearLayoutManager.VERTICAL) {
                top = if (isOnStartIndex) margin.first else margin.top
                bottom = if (isOnLastIndex) margin.last else margin.bottom
                left = margin.left
                right = margin.right
            } else {
                left = if (isOnStartIndex) margin.first else margin.left
                right = if (isOnLastIndex) margin.last else margin.right
                top = margin.top
                bottom = margin.bottom
            }
        }
    }
}

fun RecyclerView.attachGridItemDecoration(
    spanCount: Int,
    includeEdge: Boolean,
    margin: MarginItem
): RecyclerView.ItemDecoration {
    return gridMarginItemDecoration(spanCount, margin, includeEdge).apply {
        addItemDecoration(this)
    }
}

fun RecyclerView.attachLinearMarginItemDecoration(
    orientation: Int,
    margin: MarginItem
): RecyclerView.ItemDecoration {
    return linearMarginItemDecoration(orientation, margin).apply {
        addItemDecoration(this)
    }
}

data class MarginItem(
    var top: Int = 0,
    var left: Int = 0,
    var right: Int = 0,
    var bottom: Int = 0,
    var first: Int = 0,
    var last: Int = 0
)