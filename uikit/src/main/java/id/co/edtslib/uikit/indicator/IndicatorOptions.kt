package id.co.edtslib.uikit.indicator

import android.content.res.Resources
import android.graphics.Color

class IndicatorOptions {

    @IndicatorOrientationAnnotation
    var orientation = IndicatorOrientation.INDICATOR_HORIZONTAL

    @IndicatorStyleAnnotation
    var indicatorStyle: Int = 0

    @IndicatorSlideModeAnnotation
    var slideMode: Int = 0

    /**
     * Pager Size : The Size of page that would be displayed
     */
    var pageSize: Int = 0

    /**
     * Indicator Color : Color of Default indicator
     */
    var normalSliderColor: Int = 0

    /**
     * Checked Slider Color
     */
    var checkedSliderColor: Int = 0

    /**
     * Spacing/Margin/Gap Between each Indicator
     */
    var sliderGap: Float = 0f

    var sliderHeight: Float = 0f
        get() = if (field > 0) field else normalSliderWidth / 2

    var normalSliderWidth: Float = 0f

    var checkedSliderWidth: Float = 0f

    /**
     * Current Position of Displayed Item
     */
    var currentPosition: Int = 0

    /**
     * Percentage of Slider Item Progress
     */
    var slideProgress: Float = 0f

    var showIndicatorOneItem: Boolean = false

    init {
        normalSliderWidth = (0.5f + 8 * Resources.getSystem().displayMetrics.density)
        checkedSliderWidth = normalSliderWidth
        sliderGap = normalSliderWidth
        normalSliderColor = Color.parseColor("#8C18171C")
        checkedSliderColor = Color.parseColor("#8C6C6D72")
        slideMode = IndicatorSlideMode.NORMAL
    }

    fun setCheckedColor(checkedColor: Int) {
        this.checkedSliderColor = checkedColor
    }

    fun setSliderWidth(
        normalIndicatorWidth: Float,
        checkedIndicatorWidth: Float
    ) {
        this.normalSliderWidth = normalIndicatorWidth
        this.checkedSliderWidth = checkedIndicatorWidth
    }

    fun setSliderWidth(sliderWidth: Float) {
        setSliderWidth(sliderWidth, sliderWidth)
    }

    fun setSliderColor(
        normalColor: Int,
        checkedColor: Int
    ) {
        this.normalSliderColor = normalColor
        this.checkedSliderColor = checkedColor
    }
}