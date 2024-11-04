package id.co.edtslib.uikit.progressbar

import android.view.View

interface GradientProgressBarDelegate {
    fun onAnimationUpdateListener(view: View, currentProgressValue: Float, finalProgressValue: Float)
}