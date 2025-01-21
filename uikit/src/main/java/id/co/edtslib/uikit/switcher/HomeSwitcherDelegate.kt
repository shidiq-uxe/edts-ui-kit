package id.co.edtslib.uikit.switcher

interface HomeSwitcherDelegate {
    fun setOnSwitchChangedListener(selectedTab: HomeSwitcher.Tab)
}