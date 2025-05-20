package id.co.edtslib.uikit.alert

import android.view.View

interface AlertBoxDelegate {
    fun onCloseClickListener(view: View)
    fun onButtonClickListener(view: View)
}