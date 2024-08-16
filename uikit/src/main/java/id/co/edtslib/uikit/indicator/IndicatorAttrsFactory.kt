package id.co.edtslib.uikit.indicator

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import id.co.edtslib.uikit.R

object IndicatorAttrsFactory {

    fun initAttrs(context: Context, attrs: AttributeSet?, indicatorOptions: IndicatorOptions) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.IndicatorView)
            val indicatorSlideMode = typedArray.getInt(R.styleable.IndicatorView_indicator_slide_mode, 4)
            val indicatorStyle = typedArray.getInt(R.styleable.IndicatorView_indicator_style, 4)
            val checkedColor = typedArray.getColor(
                R.styleable.IndicatorView_indicator_checked_color,
                ContextCompat.getColor(context, R.color.primary_30)
            )
            val normalColor = typedArray.getColor(
                R.styleable.IndicatorView_indicator_default_color,
                ContextCompat.getColor(context, R.color.black_30)
            )
            val orientation = typedArray.getInt(R.styleable.IndicatorView_indicator_orientation, 0)

            val defaultSize = context.resources.getDimension(R.dimen.xxs)
            val selectedSize = context.resources.getDimension(R.dimen.m1)

            val defaultWidth = typedArray.getDimension(R.styleable.IndicatorView_indicator_default_width, defaultSize)
            val selectedWidth = typedArray.getDimension(R.styleable.IndicatorView_indicator_gap, selectedSize)

            val sliderGap = context.resources.getDimension(R.dimen.xxxs)

            val defaultSliderHeight = context.resources.getDimension(R.dimen.xxs)
            val sliderHeight = typedArray.getDimension(R.styleable.IndicatorView_indicator_slider_height, defaultSliderHeight)

            indicatorOptions.apply {
                this.checkedSliderColor = checkedColor
                this.normalSliderColor = normalColor
                this.orientation = orientation
                this.indicatorStyle = indicatorStyle
                this.slideMode = indicatorSlideMode
                this.normalSliderWidth = defaultWidth
                this.checkedSliderWidth = selectedWidth
                this.sliderGap = sliderGap
                this.sliderHeight = sliderHeight
            }
            typedArray.recycle()
        }
    }
}
