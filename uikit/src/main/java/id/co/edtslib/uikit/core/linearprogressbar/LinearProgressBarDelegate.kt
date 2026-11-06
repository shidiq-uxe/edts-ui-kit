package id.co.edtslib.uikit.core.linearprogressbar

interface LinearProgressBarDelegate {
    fun onAnimationUpdateListener(progressBar: CoreLinearProgressBar, animatedValue: Float, targetProgress: Float)

}