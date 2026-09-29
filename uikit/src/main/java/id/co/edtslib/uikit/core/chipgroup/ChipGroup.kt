package id.co.edtslib.uikit.core.chipgroup

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.co.edtslib.uikit.R
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class ChipGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    enum class ChipGroupOrientation {
        HORIZONTAL,
        VERTICAL,
        GRID,
    }

    enum class ChipGroupSelectionMode {
        SINGLE,
        MULTIPLE,
    }

    private val decoration = ChipGroupItemDecorator(
        orientation = ChipGroupOrientation.HORIZONTAL,
        horizontalSpacing = 0,
        verticalSpacing = 0,
        spanCount = null,
    )

    var orientation: ChipGroupOrientation = ChipGroupOrientation.HORIZONTAL
        set(value) {
            field = value
            refreshLayout()
        }

    var spanCount: Int? = null
        set(value) {
            field = value?.takeIf { it > 0 }
            refreshLayout()
        }

    var selectionMode: ChipGroupSelectionMode = ChipGroupSelectionMode.MULTIPLE

    var itemSpacingHorizontal: Int = 0
        set(value) {
            field = value
            refreshLayout()
        }

    var itemSpacingVertical: Int = 0
        set(value) {
            field = value
            refreshLayout()
        }

    var groupPaddingStart: Int = 0
        set(value) {
            field = value
            applyGroupPadding()
        }

    var groupPaddingTop: Int = 0
        set(value) {
            field = value
            applyGroupPadding()
        }

    var groupPaddingEnd: Int = 0
        set(value) {
            field = value
            applyGroupPadding()
        }

    var groupPaddingBottom: Int = 0
        set(value) {
            field = value
            applyGroupPadding()
        }

    var groupPadding: Int = 0
        set(value) {
            field = value
            groupPaddingStart = value
            groupPaddingTop = value
            groupPaddingEnd = value
            groupPaddingBottom = value
        }

    var chipAdapter: ChipGroupAdapter<*>?
        get() = adapter as? ChipGroupAdapter<*>
        set(value) { adapter = value }

    init {
        addItemDecoration(decoration)
        refreshLayout()

        groupPaddingStart = paddingStart
        groupPaddingTop = paddingTop
        groupPaddingEnd = paddingEnd
        groupPaddingBottom = paddingBottom
        attrs?.let { applyXmlAttrs(it, defStyleAttr) }
    }

    private fun applyXmlAttrs(attrs: AttributeSet, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChipListView, defStyleAttr, 0)
        try {
            if (typedArray.hasValue(R.styleable.ChipListView_itemSpacingHorizontal)) { itemSpacingHorizontal = typedArray.getDimensionPixelSize(R.styleable.ChipListView_itemSpacingHorizontal, 0) }
            if (typedArray.hasValue(R.styleable.ChipListView_itemSpacingVertical)) { itemSpacingVertical = typedArray.getDimensionPixelSize(R.styleable.ChipListView_itemSpacingVertical, 0) }
            if (typedArray.hasValue(R.styleable.ChipListView_spanCount)) { spanCount = typedArray.getInt(R.styleable.ChipListView_spanCount, 0) }
            if (typedArray.hasValue(R.styleable.ChipListView_selectionMode)) {
                selectionMode = when (typedArray.getInt(R.styleable.ChipListView_selectionMode, 1)) {
                    0 -> ChipGroupSelectionMode.SINGLE
                    else -> ChipGroupSelectionMode.MULTIPLE
                }
            }
            if (typedArray.hasValue(R.styleable.ChipListView_orientation)) {
                orientation = when (typedArray.getInt(R.styleable.ChipListView_orientation, 0)) {
                    1 -> ChipGroupOrientation.VERTICAL
                    2 -> ChipGroupOrientation.GRID
                    else -> ChipGroupOrientation.HORIZONTAL
                }
            }
        } finally {
            typedArray.recycle()
        }
    }

    private fun applyGroupPadding() {
        setPaddingRelative(groupPaddingStart, groupPaddingTop, groupPaddingEnd, groupPaddingBottom)
    }

    private fun refreshLayout() {
        val newLayoutManager = when (orientation) {
            ChipGroupOrientation.HORIZONTAL -> LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            ChipGroupOrientation.VERTICAL -> LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            ChipGroupOrientation.GRID -> spanCount?.let { GridLayoutManager(context, it) } ?: FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.FLEX_START
            }
        }

        val currentAdapter = adapter
        if (currentAdapter != null) {
            adapter = null
        }

        layoutManager = newLayoutManager
        decoration.update(orientation, itemSpacingHorizontal, itemSpacingVertical, spanCount)
        invalidateItemDecorations()

        if (currentAdapter != null) {
            adapter = currentAdapter
        }
    }
}