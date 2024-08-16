package id.co.edtslib.uikit.indicator.drawer

import android.graphics.Canvas
import id.co.edtslib.uikit.indicator.IndicatorOptions

class DashDrawer internal constructor(indicatorOptions: IndicatorOptions) : RectDrawer(
    indicatorOptions
) {
    override fun drawRect(
        canvas: Canvas,
        rx: Float,
        ry: Float
    ) {
        canvas.drawRect(mRectF, mPaint)
    }
}
