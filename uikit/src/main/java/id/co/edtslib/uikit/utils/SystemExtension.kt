package id.co.edtslib.uikit.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.hapticfeedback.HapticFeedback

@Suppress("DEPRECATION")
fun Activity.setLightStatusBar(@ColorInt scrimColor: Int = Color.WHITE) {
    window?.statusBarColor = scrimColor
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window?.decorView?.windowInsetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )
    } else {
        window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}

@Suppress("DEPRECATION")
fun Activity.setDarkStatusBar(@ColorInt scrimColor: Int = colorAttr(android.R.attr.colorPrimary)) {
    window?.statusBarColor = scrimColor
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window?.decorView?.windowInsetsController?.setSystemBarsAppearance(
            0,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )
    } else {
        window?.decorView?.systemUiVisibility = 0
    }
}

fun Activity.setSystemBarStyle(
    statusBarStyle: SystemBarStyle = SystemBarStyle.Light(Color.WHITE),
    navigationBarStyle: SystemBarStyle = SystemBarStyle.Light(Color.WHITE)
) {
    setDarkStatusBar(statusBarStyle.scrimColor)
    window?.navigationBarColor = navigationBarStyle.scrimColor
}

sealed class SystemBarStyle(open val scrimColor: Int) {
    data class Light(@ColorInt override val scrimColor: Int) : SystemBarStyle(scrimColor)
    data class Dark(@ColorInt override val scrimColor: Int) : SystemBarStyle(scrimColor)
}

private val Context?.vibrator get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = this?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    vibratorManager.defaultVibrator
} else {
    @Suppress("DEPRECATION")
    this?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
}

fun Context?.vibratePhone(rule: HapticFeedback) {
    if (Build.VERSION.SDK_INT >= 26) {
        vibrator.vibrate(VibrationEffect.createOneShot(rule.duration, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(200)
    }
}

fun Window.setScreenBrightness(brightness: Float) {
    attributes = attributes.apply {
        screenBrightness = brightness
    }
}

fun Window.resetScreenBrightness() {
    attributes = attributes.apply {
        screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    }
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}