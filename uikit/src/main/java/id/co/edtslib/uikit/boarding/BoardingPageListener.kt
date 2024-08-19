package id.co.edtslib.uikit.boarding

interface BoardingPageListener {
    fun onPageSelected(position: Int)
    fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)
}