package id.co.edtslib.uikit.utils

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import id.co.edtslib.uikit.R

fun Activity.setLightStatusBar() {
    window?.statusBarColor = color(R.color.white)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window?.decorView?.windowInsetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )
    } else {
        window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}

fun Activity.setDarkStatusBar() {
    window?.statusBarColor = colorAttr(android.R.attr.colorPrimary)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window?.decorView?.windowInsetsController?.setSystemBarsAppearance(
            0,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )
    } else {
        window?.decorView?.systemUiVisibility = 0
    }
}