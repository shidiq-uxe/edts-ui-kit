package id.co.edtslib.uikit.recipes.chip.klik

import android.content.Context
import android.util.AttributeSet
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.chip.CoreChip

class KlikChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoreChip(context, attrs, defStyleAttr) {

    init {
        chipVariant = KlikChipVariant.Suggestion
        applyXmlVariant(attrs, defStyleAttr)
    }

    var klikChipVariant: KlikChipVariant
        get() = chipVariant as? KlikChipVariant ?: KlikChipVariant.Suggestion
        set(value) { chipVariant = value }

    private fun applyXmlVariant(attrs: AttributeSet?, defStyleAttr: Int) {
        attrs ?: return
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Chip, defStyleAttr, 0)
        try {
            if (!typedArray.hasValue(R.styleable.Chip_chipVariant)) return
            when (typedArray.getResourceId(R.styleable.Chip_chipVariant, 0)) {
                R.style.Widget_EDTS_UIKit_Chip_Klik_Suggestion -> klikChipVariant = KlikChipVariant.Suggestion
                R.style.Widget_EDTS_UIKit_Chip_Klik_Filters -> klikChipVariant = KlikChipVariant.Filters
            }
        } finally {
            typedArray.recycle()
        }
    }
}