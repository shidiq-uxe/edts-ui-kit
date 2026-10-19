package id.co.edtslib.uikit.tray

import android.content.Context
import androidx.core.content.res.use
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.areSystemAnimationsEnabled

/** Uses the sheet's drag-to-hidden motion, then closes the window without another exit. */
internal class TrayDialog(
    context: Context,
    theme: Int,
    private val isCustomMotionEnabled: () -> Boolean,
    private val shouldAnimateDismiss: () -> Boolean
) : BottomSheetDialog(context, theme) {

    private var isDirectDismissPending = false

    fun configureMotion() {
        configureTrayMotion(isCustomMotionEnabled())
    }

    override fun onStart() {
        super.onStart()
        isDirectDismissPending = false
        configureMotion()
    }

    override fun cancel() {
        configureMotion()
        if (isDirectDismissPending && behavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            // A direct dismiss must not become a cancellation when Material reaches STATE_HIDDEN.
            super.dismiss()
            return
        }
        if (dismissWithAnimation) behavior.isHideable = true
        super.cancel()
    }

    override fun dismiss() {
        configureMotion()
        if (dismissWithAnimation && isShowing && shouldAnimateDismiss() &&
            behavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            isDirectDismissPending = true
            behavior.isHideable = true
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            super.dismiss()
        }
    }
}

/** Also applies to BottomSheetTray subclasses that supply their own BottomSheetDialog. */
internal fun BottomSheetDialog.configureTrayMotion(isCustomMotionEnabled: Boolean) {
    val isMotionEnabled = context.areSystemAnimationsEnabled()
    dismissWithAnimation = isCustomMotionEnabled && isMotionEnabled
    val inheritedWindowAnimations = context.obtainStyledAttributes(
        intArrayOf(android.R.attr.windowAnimationStyle)
    ).use { it.getResourceId(0, 0) }
    window?.setWindowAnimations(when {
        !isMotionEnabled -> 0
        !isCustomMotionEnabled -> inheritedWindowAnimations
        else -> R.style.Animation_EDTS_UIKit_BottomSheetDialog
    })
}
