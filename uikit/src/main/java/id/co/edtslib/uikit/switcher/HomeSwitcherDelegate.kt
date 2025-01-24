package id.co.edtslib.uikit.switcher

interface HomeSwitcherDelegate {
    fun onSwitchChangedListener(selectedTab: HomeSwitcher.Tab)
    fun onSwitchAnimationEndListener(selectedTab: HomeSwitcher.Tab)
}