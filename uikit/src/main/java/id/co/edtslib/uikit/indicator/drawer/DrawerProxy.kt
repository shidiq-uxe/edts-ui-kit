package id.co.edtslib.uikit.indicator.drawer

import android.graphics.Canvas
import id.co.edtslib.uikit.indicator.IndicatorOptions

class DrawerProxy(indicatorOptions: IndicatorOptions) : DrawerInterface {

    private lateinit var mIDrawer: DrawerInterface

    init {
        init(indicatorOptions)
    }

    private fun init(indicatorOptions: IndicatorOptions) {
        mIDrawer = DrawerFactory.createDrawer(indicatorOptions)
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ): BaseDrawer.MeasureResult {
        return mIDrawer.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        mIDrawer.onDraw(canvas)
    }

    fun setIndicatorOptions(indicatorOptions: IndicatorOptions) {
        init(indicatorOptions)
    }
}