package id.co.edtslib.uikit.pulltorefresh

interface LiteRefreshDelegate {
    fun onPull(percent: Float, offset: Float)
    fun onRefreshing()
    fun onFinish()
}