package id.co.edtslib.uikit.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import id.co.edtslib.uikit.utils.hapticfeedback.HapticFeedback

private val SCRIM_STATUS_TAG = View.generateViewId()
private val SCRIM_NAV_TAG = View.generateViewId()
private val SCRIM_ACTION_BAR_OVERLAY_TAG = View.generateViewId()

fun Activity.setStatusBarIcons(isLight: Boolean) {
    WindowCompat.getInsetsController(window, window.decorView)
        .isAppearanceLightStatusBars = isLight
}

fun Activity.setSystemBarStyle(
    statusBarStyle: SystemBarStyle = SystemBarStyle.Light(Color.WHITE),
    navigationBarStyle: SystemBarStyle = SystemBarStyle.Light(Color.WHITE)
) {
    setStatusBarIcons(isLight = statusBarStyle is SystemBarStyle.Light)
    setSystemBarScrims(
        statusBarColor = statusBarStyle.scrimColor,
        navigationBarColor = navigationBarStyle.scrimColor
    )
}

/**
 * Sets scrim (background) colors behind the status bar and navigation bar.
 *
 * [Window.setStatusBarColor] and [Window.setNavigationBarColor] are deprecated
 * and ineffective on API 35+ when edge-to-edge is active. This injects colored
 * [View]s into [android.R.id.content] at index 0 (behind the actual content),
 * sized to match system bar insets via [ViewCompat.setOnApplyWindowInsetsListener].
 *
 * Call this **after** [androidx.activity.enableEdgeToEdge] and **after**
 * [androidx.appcompat.app.AppCompatActivity.setContentView].
 *
 * @param statusBarColor  Background color for the status bar area, or null to skip.
 * @param navigationBarColor  Background color for the navigation bar area, or null to skip.
 */
fun Activity.setSystemBarScrims(
    @ColorInt statusBarColor: Int? = null,
    @ColorInt navigationBarColor: Int? = null
) {
    val content = window?.decorView
        ?.findViewById<FrameLayout>(android.R.id.content) ?: return

    if (statusBarColor != null) {
        var scrim = content.findViewWithTag<View>(SCRIM_STATUS_TAG)
        if (scrim == null) {
            scrim = View(this).apply {
                id = SCRIM_STATUS_TAG
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, 0
                )
            }
            content.addView(scrim)
        }
        scrim.setBackgroundColor(statusBarColor)
    }

    if (navigationBarColor != null) {
        var scrim = content.findViewWithTag<View>(SCRIM_NAV_TAG)
        if (scrim == null) {
            scrim = View(this).apply {
                id = SCRIM_NAV_TAG
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, 0
                ).also { it.gravity = Gravity.BOTTOM }
            }
            content.addView(scrim)
        }
        scrim.setBackgroundColor(navigationBarColor)
    }

    ViewCompat.setOnApplyWindowInsetsListener(content) { v, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

        v.findViewById<View>(SCRIM_STATUS_TAG)?.let { s ->
            val lp = s.layoutParams
            if (lp.height != bars.top) {
                lp.height = bars.top
                s.requestLayout()
            }
        }

        v.findViewById<View>(SCRIM_NAV_TAG)?.let { s ->
            val lp = s.layoutParams
            if (lp.height != bars.bottom) {
                lp.height = bars.bottom
                s.requestLayout()
            }
        }

        insets
    }
}

/**
 * Injects a colored overlay into the DecorView that covers the elevation shadow
 * bleeding above the action bar into the status bar area.
 *
 * This must be called **after** [setContentView] so the action bar is already
 * part of the decor view hierarchy. The overlay is sized to the status bar height
 * and drawn above the action bar in z-order, masking any unwanted top shadow.
 */
fun Activity.setActionBarTopScrim(@ColorInt color: Int) {
    val decor = window?.decorView as? FrameLayout ?: return

    var overlay = decor.findViewWithTag<View>(SCRIM_ACTION_BAR_OVERLAY_TAG)
    if (overlay == null) {
        overlay = View(this).apply {
            id = SCRIM_ACTION_BAR_OVERLAY_TAG
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, 0
            )
        }
        decor.addView(overlay)
    }
    overlay.setBackgroundColor(color)

    ViewCompat.setOnApplyWindowInsetsListener(decor) { _, insets ->
        val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        val lp = overlay.layoutParams
        if (lp.height != top) {
            lp.height = top
            overlay.requestLayout()
        }
        insets
    }
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