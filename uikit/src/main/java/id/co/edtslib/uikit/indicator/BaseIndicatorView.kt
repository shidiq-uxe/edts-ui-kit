package id.co.edtslib.uikit.indicator

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

open class BaseIndicatorView constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : View(context, attrs, defStyleAttr), IndicatorInterface {

    var mIndicatorOptions: IndicatorOptions

    private var mViewPager2: ViewPager2? = null
    private var mRecyclerView: RecyclerView? = null
    private var mScrollListener: RecyclerView.OnScrollListener? = null

    private val mOnPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            this@BaseIndicatorView.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            this@BaseIndicatorView.onPageSelected(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            this@BaseIndicatorView.onPageScrollStateChanged(state)
        }
    }

    init {
        mIndicatorOptions = IndicatorOptions()
    }

    fun onPageSelected(position: Int) {
        if (getSlideMode() == IndicatorSlideMode.NORMAL) {
            setCurrentPosition(position)
            setSlideProgress(0f)
            invalidate()
        }
    }

    fun onPageScrolled(
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int
    ) {
        if (getSlideMode() != IndicatorSlideMode.NORMAL && getPageSize() > 1) {
            scrollSlider(position, positionOffset)
            invalidate()
        }
    }


    fun onPageScrollStateChanged(state: Int) {

    }

    private fun scrollSlider(
        position: Int,
        positionOffset: Float
    ) {
        if (mIndicatorOptions.slideMode == IndicatorSlideMode.SCALE
            || mIndicatorOptions.slideMode == IndicatorSlideMode.COLOR
        ) {
            setCurrentPosition(position)
            setSlideProgress(positionOffset)
        } else {
            if (position % getPageSize() == getPageSize() - 1) {
                if (positionOffset < 0.5) {
                    setCurrentPosition(position)
                    setSlideProgress(0f)
                } else {
                    setCurrentPosition(0)
                    setSlideProgress(0f)
                }
            } else {
                setCurrentPosition(position)
                setSlideProgress(positionOffset)
            }
        }
    }

    fun setupWithRecyclerView(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        mRecyclerView = recyclerView
        mRecyclerView?.adapter = adapter

        // Set the page size based on the adapter item count
        setPageSize(adapter.itemCount)

        // Remove any existing scroll listeners
        mScrollListener?.let { mRecyclerView?.removeOnScrollListener(it) }

        // Add a scroll listener to track scroll events
        mScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                layoutManager?.let {
                    val position = it.findFirstVisibleItemPosition()
                    val offset = (it.findViewByPosition(position)?.left ?: 0).toFloat() / recyclerView.width
                    onPageScrolled(position, offset, 0)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                    layoutManager?.let {
                        val position = it.findFirstCompletelyVisibleItemPosition()
                        onPageSelected(position)
                    }
                }
                onPageScrollStateChanged(newState)
            }
        }

        // Register the scroll listener
        mRecyclerView?.addOnScrollListener(mScrollListener!!)
        requestLayout()
        invalidate()
    }

    override fun notifyDataChanged() {
        setupViewPager()
        requestLayout()
        invalidate()
    }

    private fun setupViewPager() {
        mViewPager2?.let {
            mViewPager2?.unregisterOnPageChangeCallback(mOnPageChangeCallback)
            mViewPager2?.registerOnPageChangeCallback(mOnPageChangeCallback)
            mViewPager2?.adapter?.let {
                setPageSize(mViewPager2!!.adapter!!.itemCount)
            }
        }
    }

    fun getNormalSlideWidth(): Float {
        return mIndicatorOptions.normalSliderWidth
    }

    fun setNormalSlideWidth(normalSliderWidth: Float) {
        mIndicatorOptions.normalSliderWidth = normalSliderWidth
    }

    fun getCheckedSlideWidth(): Float {
        return mIndicatorOptions.checkedSliderWidth
    }

    fun setCheckedSlideWidth(@Px checkedSliderWidth: Float) {
        mIndicatorOptions.checkedSliderWidth = checkedSliderWidth
    }

    val checkedSliderWidth: Float
        get() = mIndicatorOptions.checkedSliderWidth

    fun setCurrentPosition(currentPosition: Int) {
        mIndicatorOptions.currentPosition = currentPosition
    }

    fun getCurrentPosition(): Int {
        return mIndicatorOptions.currentPosition
    }

    fun getIndicatorGap(): Float {
        return mIndicatorOptions.sliderGap
    }

    fun setIndicatorGap(indicatorGap: Float) {
        mIndicatorOptions.sliderGap = indicatorGap
    }

    fun setCheckedColor(@ColorInt normalColor: Int) {
        mIndicatorOptions.checkedSliderColor = normalColor
    }

    fun getCheckedColor(): Int {
        return mIndicatorOptions.checkedSliderColor
    }

    fun setNormalColor(@ColorInt normalColor: Int) {
        mIndicatorOptions.normalSliderColor = normalColor
    }

    fun getSlideProgress(): Float {
        return mIndicatorOptions.slideProgress
    }

    fun setSlideProgress(slideProgress: Float) {
        mIndicatorOptions.slideProgress = slideProgress
    }

    fun getPageSize(): Int {
        return mIndicatorOptions.pageSize
    }

    fun setPageSize(pageSize: Int): BaseIndicatorView {
        mIndicatorOptions.pageSize = pageSize
        return this
    }

    fun setSliderColor(
        @ColorInt normalColor: Int,
        @ColorInt selectedColor: Int
    ): BaseIndicatorView {
        mIndicatorOptions.setSliderColor(normalColor, selectedColor)
        return this
    }

    /**
     * set width for indicator slider,unit is px.
     *
     * @param sliderWidth the indicator slider width,checked width equals unchecked width.
     */
    fun setSliderWidth(@Px sliderWidth: Float): BaseIndicatorView {
        mIndicatorOptions.setSliderWidth(sliderWidth)
        return this
    }

    /**
     * set width for indicator slider ,unit is px.
     *
     * @param normalSliderWidth unchecked slider radius
     * @param selectedSliderWidth checked slider radius
     */
    fun setSliderWidth(
        @Px normalSliderWidth: Float,
        @Px selectedSliderWidth: Float
    ): BaseIndicatorView {
        mIndicatorOptions.setSliderWidth(normalSliderWidth, selectedSliderWidth)
        return this
    }

    fun setSliderGap(sliderGap: Float): BaseIndicatorView {
        mIndicatorOptions.sliderGap = sliderGap
        return this
    }

    fun getSlideMode(): Int {
        return mIndicatorOptions.slideMode
    }

    fun setSlideMode(@IndicatorSlideModeAnnotation slideMode: Int): BaseIndicatorView {
        mIndicatorOptions.slideMode = slideMode
        return this
    }

    fun setIndicatorStyle(@IndicatorStyleAnnotation indicatorStyle: Int): BaseIndicatorView {
        mIndicatorOptions.indicatorStyle = indicatorStyle
        return this
    }

    fun setSliderHeight(sliderHeight: Float): BaseIndicatorView {
        mIndicatorOptions.sliderHeight = sliderHeight
        return this
    }

    fun setupWithViewPager(viewPager2: ViewPager2) {
        mViewPager2 = viewPager2
        notifyDataChanged()
    }

    fun showIndicatorWhenOneItem(showIndicatorWhenOneItem: Boolean) {
        mIndicatorOptions.showIndicatorOneItem = showIndicatorWhenOneItem
    }

    override fun setIndicatorOptions(options: IndicatorOptions) {
        mIndicatorOptions = options
    }
}