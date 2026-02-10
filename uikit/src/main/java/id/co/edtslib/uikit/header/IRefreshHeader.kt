package id.co.edtslib.uikit.header

interface IRefreshHeader {
    fun onPull(percent: Float)
    fun onRefreshing()
    fun onFinish()
}