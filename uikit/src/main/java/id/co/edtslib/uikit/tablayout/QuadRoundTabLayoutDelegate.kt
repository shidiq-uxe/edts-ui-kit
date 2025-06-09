package id.co.edtslib.uikit.tablayout

interface QuadRoundTabLayoutDelegate {
    fun onTabSelected(position: Int, view: QuadRoundTabLayout.QuadRoundTabView, item: QuadRoundTabLayout.TabItem)
}