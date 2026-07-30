package id.co.edtslib.edtsuikit.helper

import android.view.View
import androidx.fragment.app.FragmentManager
import id.co.edtslib.uikit.tray.BottomSheetTray

/**
 * Standard config bottom sheet for guideline activities.
 *
 * Usage in any GuidelinesBaseActivity subclass:
 * 1. Set `override val hasConfigMenu = true`
 * 2. Override `onConfigMenuClicked()` to call your `showConfigSheet()`
 * 3. In `showConfigSheet()`, inflate your custom bottom sheet layout,
 *    wire up your controls, then call [show]:
 *
 * ```
 * override val hasConfigMenu = true
 *
 * override fun onConfigMenuClicked() = showConfigSheet()
 *
 * private fun showConfigSheet() {
 *     val binding = BottomSheetMyComponentConfigBinding.inflate(layoutInflater, null, false)
 *     // ... wire controls to your config state ...
 *     GuidelineConfigSheet(supportFragmentManager, "Configure My Component", binding.root).show()
 * }
 * ```
 */
class GuidelineConfigSheet(
    private val fragmentManager: FragmentManager,
    private val title: String,
    private val contentView: View,
    private val tag: String = CONFIG_SHEET_TAG,
) {
    private var tray: BottomSheetTray? = null

    fun show() {
        tray = BottomSheetTray.newInstance(
            title = title,
            contentLayout = contentView
        ).apply {
            dismissOnOutsideTouch = true
            isCancelableOnTouchOutside = true
            dragHandleVisibility = true
            titleDividerVisibility = true
            shouldShowClose = true
        }
        tray?.show(fragmentManager, tag)
    }

    fun dismiss() {
        tray?.dismiss()
    }

    companion object {
        const val CONFIG_SHEET_TAG = "CONFIG_SHEET"
    }
}
