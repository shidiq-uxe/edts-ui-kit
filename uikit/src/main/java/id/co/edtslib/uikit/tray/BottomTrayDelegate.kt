package id.co.edtslib.uikit.tray

import android.content.DialogInterface
import android.view.View

interface BottomTrayDelegate {
    fun onShow(dialogInterface: DialogInterface)
    fun onDismiss(dialogInterface: DialogInterface)
    fun onStateChanged(bottomSheet: View, newState: Int)
    fun onSlide(bottomSheet: View, slideOffset: Float)
}