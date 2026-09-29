package id.co.edtslib.edtsuikit

import android.os.Bundle
import id.co.edtslib.uikit.core.chipgroup.ChipGroup.ChipGroupOrientation
import id.co.edtslib.uikit.core.chipgroup.ChipGroup.ChipGroupSelectionMode

data class ChipGroupConfig(
    val product: ChipProduct = ChipProduct.CORE,
    val variantIndex: Int = 0,
    val orientation: ChipGroupOrientation = ChipGroupOrientation.HORIZONTAL,
    val itemSpacingHorizontalDp: Int = 8,
    val itemSpacingVerticalDp: Int = 8,
    val spanCount: Int? = null,
    val itemCount: Int = 6,
    val selectionMode: ChipGroupSelectionMode = ChipGroupSelectionMode.MULTIPLE,
) {
    fun orientationLabels(): List<String> = listOf("Horizontal", "Vertical", "Grid")
    fun selectionModeLabels(): List<String> = listOf("Single", "Multiple")
}

private const val CONFIG_PRODUCT = "group_product"
private const val CONFIG_VARIANT_INDEX = "group_variant_index"
private const val CONFIG_ORIENTATION = "orientation"
private const val CONFIG_SPACING_HORIZONTAL = "spacing_horizontal"
private const val CONFIG_SPACING_VERTICAL = "spacing_vertical"
private const val CONFIG_SPAN_COUNT = "span_count"
private const val CONFIG_ITEM_COUNT = "item_count"
private const val CONFIG_SELECTION_MODE = "selection_mode"

fun ChipGroupConfig.toBundle(): Bundle = Bundle().apply {
    putString(CONFIG_PRODUCT, product.name)
    putInt(CONFIG_VARIANT_INDEX, variantIndex)
    putString(CONFIG_ORIENTATION, orientation.name)
    putInt(CONFIG_SPACING_HORIZONTAL, itemSpacingHorizontalDp)
    putInt(CONFIG_SPACING_VERTICAL, itemSpacingVerticalDp)
    spanCount?.let { putInt(CONFIG_SPAN_COUNT, it) }
    putInt(CONFIG_ITEM_COUNT, itemCount)
    putString(CONFIG_SELECTION_MODE, selectionMode.name)
}

fun Bundle.toChipGroupConfig(): ChipGroupConfig = ChipGroupConfig(
    product = getString(CONFIG_PRODUCT)
        ?.let { runCatching { ChipProduct.valueOf(it) }.getOrNull() }
        ?: ChipProduct.CORE,
    variantIndex = getInt(CONFIG_VARIANT_INDEX, 0),
    orientation = getString(CONFIG_ORIENTATION)
        ?.let { runCatching { ChipGroupOrientation.valueOf(it) }.getOrNull() }
        ?: ChipGroupOrientation.HORIZONTAL,
    itemSpacingHorizontalDp = getInt(CONFIG_SPACING_HORIZONTAL, 8),
    itemSpacingVerticalDp = getInt(CONFIG_SPACING_VERTICAL, 8),
    spanCount = if (containsKey(CONFIG_SPAN_COUNT)) getInt(CONFIG_SPAN_COUNT) else null,
    itemCount = getInt(CONFIG_ITEM_COUNT, 6),
    selectionMode = getString(CONFIG_SELECTION_MODE)
        ?.let { runCatching { ChipGroupSelectionMode.valueOf(it) }.getOrNull() }
        ?: ChipGroupSelectionMode.MULTIPLE,
)

internal fun orientationToIndex(orientation: ChipGroupOrientation): Int = when (orientation) {
    ChipGroupOrientation.HORIZONTAL -> 0
    ChipGroupOrientation.VERTICAL -> 1
    ChipGroupOrientation.GRID -> 2
}

internal fun indexToOrientation(index: Int): ChipGroupOrientation = when (index) {
    1 -> ChipGroupOrientation.VERTICAL
    2 -> ChipGroupOrientation.GRID
    else -> ChipGroupOrientation.HORIZONTAL
}

internal fun selectionModeToIndex(mode: ChipGroupSelectionMode): Int = when (mode) {
    ChipGroupSelectionMode.SINGLE -> 0
    ChipGroupSelectionMode.MULTIPLE -> 1
}

internal fun indexToSelectionMode(index: Int): ChipGroupSelectionMode = when (index) {
    0 -> ChipGroupSelectionMode.SINGLE
    else -> ChipGroupSelectionMode.MULTIPLE
}