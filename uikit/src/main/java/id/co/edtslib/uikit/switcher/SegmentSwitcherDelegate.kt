package id.co.edtslib.uikit.switcher

interface SegmentSwitcherDelegate {
    fun onSwitchChangedListener(position: Int, tab: TabItem)
    fun onSwitchAnimationEndListener(position: Int, tab: TabItem)
}
