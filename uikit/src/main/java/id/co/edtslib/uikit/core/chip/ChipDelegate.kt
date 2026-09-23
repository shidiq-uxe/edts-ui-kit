package id.co.edtslib.uikit.core.chip

import android.view.View

interface ChipDelegate {
    fun onClick(view: View, @ChipStateAnnotation state: Int)
    fun onStartIconClick(view: View, @ChipStateAnnotation state: Int) {}
    fun onEndIconClick(view: View, @ChipStateAnnotation state: Int) {}
}