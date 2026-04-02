package id.co.edtslib.uikit.coachmark

interface CoachmarkDelegate {
    fun onNextClickClickListener(currentIndex: Int)
    fun onSkipClickListener(currentIndex: Int)
    fun onDismissListener()
}