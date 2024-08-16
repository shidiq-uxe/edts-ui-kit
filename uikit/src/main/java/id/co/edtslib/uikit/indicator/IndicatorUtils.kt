package id.co.edtslib.uikit.indicator

fun getCoordinateX(
    indicatorOptions: IndicatorOptions,
    maxDiameter: Float,
    index: Int
): Float {
    val normalIndicatorWidth = indicatorOptions.normalSliderWidth
    return maxDiameter / 2 + (normalIndicatorWidth + indicatorOptions.sliderGap) * index
}

fun getCoordinateY(maxDiameter: Float): Float {
    return maxDiameter / 2
}