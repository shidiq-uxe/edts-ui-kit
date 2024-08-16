package id.co.edtslib.uikit.indicator.drawer

import id.co.edtslib.uikit.indicator.IndicatorOptions
import id.co.edtslib.uikit.indicator.IndicatorStyle

internal object DrawerFactory {
    fun createDrawer(indicatorOptions: IndicatorOptions): DrawerInterface {
        return when (indicatorOptions.indicatorStyle) {
            IndicatorStyle.DASH -> DashDrawer(indicatorOptions)
            IndicatorStyle.ROUND_RECT -> RoundRectDrawer(indicatorOptions)
            else -> CircleDrawer(indicatorOptions)
        }
    }
}