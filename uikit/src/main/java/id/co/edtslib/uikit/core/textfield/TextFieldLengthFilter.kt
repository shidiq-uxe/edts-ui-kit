package id.co.edtslib.uikit.core.textfield

import android.text.InputFilter
import android.widget.EditText

internal fun EditText.applyTextFieldLengthLimit(limit: Int, replaceExisting: Boolean) {
    val activeFilters = if (replaceExisting || limit <= 0) {
        filters.filterNot { it is InputFilter.LengthFilter }.toTypedArray()
    } else {
        filters
    }
    filters = if (limit > 0) activeFilters + InputFilter.LengthFilter(limit) else activeFilters
}
