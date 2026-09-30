package id.co.edtslib.uikit.core.chipgroup

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

internal class ChipGroupItemDecorator(
    private var orientation: ChipGroup.ChipGroupOrientation,
    private var horizontalSpacing: Int,
    private var verticalSpacing: Int,
    private var spanCount: Int?,
) : RecyclerView.ItemDecoration() {

    fun update(
        orientation: ChipGroup.ChipGroupOrientation,
        horizontalSpacingPx: Int,
        verticalSpacingPx: Int,
        spanCount: Int?,
    ) {
        this.orientation = orientation
        this.horizontalSpacing = horizontalSpacingPx
        this.verticalSpacing = verticalSpacingPx
        this.spanCount = spanCount
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return
        val itemCount = state.itemCount

        when (orientation) {
            ChipGroup.ChipGroupOrientation.HORIZONTAL -> {
                outRect.right = if (position < itemCount - 1) horizontalSpacing else 0
            }
            ChipGroup.ChipGroupOrientation.VERTICAL -> {
                outRect.bottom = if (position < itemCount - 1) verticalSpacing else 0
            }
            ChipGroup.ChipGroupOrientation.GRID -> {
                val span = spanCount
                if (span != null) {
                    val column = position % span
                    outRect.left = column * horizontalSpacing / span
                    outRect.right = horizontalSpacing - (column + 1) * horizontalSpacing / span
                    if (position >= span) {
                        outRect.top = verticalSpacing
                    }
                } else {
                    outRect.right = horizontalSpacing
                    outRect.bottom = verticalSpacing
                }
            }
        }
    }
}