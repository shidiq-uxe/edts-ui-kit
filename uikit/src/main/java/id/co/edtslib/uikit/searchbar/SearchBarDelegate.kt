package id.co.edtslib.uikit.searchbar

import android.view.View

interface SearchBarDelegate {
    fun onCloseIconClicked(view: View)

    fun onFocusChange(view: View, hasFocus: Boolean)

    fun onTextChange(text: String)
}