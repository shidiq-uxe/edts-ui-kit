package id.co.edtslib.uikit.recipes.chip.poinku

import android.content.Context
import android.util.AttributeSet
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.chip.CoreChip

class PoinkuChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoreChip(context, attrs, defStyleAttr) {

    init {
        chipVariant = PoinkuChipVariant
        applyXmlVariant(attrs, defStyleAttr)
    }

    private fun applyXmlVariant(attrs: AttributeSet?, defStyleAttr: Int) {
        attrs ?: return
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Chip, defStyleAttr, 0)
        try {
            if (!typedArray.hasValue(R.styleable.Chip_chipVariant)) return
            when (typedArray.getResourceId(R.styleable.Chip_chipVariant, 0)) {
                R.style.Widget_EDTS_UIKit_Chip_Poinku -> chipVariant = PoinkuChipVariant
            }
        } finally {
            typedArray.recycle()
        }
    }
}