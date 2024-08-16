package id.co.edtslib.uikit.indicator.drawer

import android.graphics.Canvas

interface DrawerInterface {

    fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    )

    fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ): BaseDrawer.MeasureResult

    fun onDraw(canvas: Canvas)
}