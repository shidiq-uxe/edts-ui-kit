package id.co.edtslib.uikit.selector

interface SelectorDelegate {
    fun onCheckedChanged(isChecked: Boolean)
    fun onCheckedStateChanged(state: Int)
}