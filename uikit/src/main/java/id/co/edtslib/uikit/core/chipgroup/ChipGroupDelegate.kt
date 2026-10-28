package id.co.edtslib.uikit.core.chipgroup

import id.co.edtslib.uikit.core.chip.CoreChip

interface ChipGroupDelegate<T> {
    fun onChipClick(chip: CoreChip, item: T, position: Int)
    fun onChipStartIconClick(chip: CoreChip, item: T, position: Int) {}
    fun onChipEndIconClick(chip: CoreChip, item: T, position: Int) {}
}