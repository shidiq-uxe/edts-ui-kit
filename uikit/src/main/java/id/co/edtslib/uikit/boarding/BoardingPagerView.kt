package id.co.edtslib.uikit.boarding

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.boarding.adapter.BoardingAdapter
import id.co.edtslib.uikit.databinding.ViewBoardingPagerBinding
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.horizontalBias
import id.co.edtslib.uikit.utils.marginEnd
import id.co.edtslib.uikit.utils.marginHorizontal
import id.co.edtslib.uikit.utils.marginStart
import id.co.edtslib.uikit.utils.seconds
import id.co.edtslib.uikit.utils.setCurrentItem
import id.co.edtslib.uikit.utils.transformer.ScalePageTransformer

class BoardingPagerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val adapter = BoardingAdapter.boardingAdapter()
    var delegate: BoardingPageListener? = null

    private val binding: ViewBoardingPagerBinding =
        ViewBoardingPagerBinding.inflate(LayoutInflater.from(context), this, true)

    // Per Second Integer
    var autoScrollInterval = 0
    private var runnable: Runnable? = null

    var canBackOnFirstPosition = false
    var circular = false
        set(value) {
            field = value
            BoardingAdapter.circular = value
        }

    val root = binding.root
    val indicatorView = binding.indicatorView
    val viewPager = binding.vpContent

    var contentAlignment = BoardingItemAlignment()
        set(value) {
            field = value
            BoardingAdapter.contentAlignment = value
        }

    var indicatorAlignment: IndicatorAlignment = IndicatorAlignment.Center()
        set(value) {
            val parent = binding.root

            field = value
            when (value) {
                is IndicatorAlignment.Center -> {
                    binding.indicatorView.horizontalBias(0.5f)

                    if (value.endMargin > 0) {
                        binding.indicatorView.marginStart(parent, value.endMargin)
                    }
                    if (value.startMargin > 0) {
                        binding.indicatorView.marginEnd(parent, value.startMargin)
                    }
                }
                is IndicatorAlignment.End -> {
                    binding.indicatorView.horizontalBias(1f)

                    if (value.horizontalMargin > 0) {
                        binding.indicatorView.marginHorizontal(parent, value.horizontalMargin)
                    }
                }
                is IndicatorAlignment.Start -> {
                    binding.indicatorView.horizontalBias(0f)

                    if (value.horizontalMargin > 0) {
                        binding.indicatorView.marginHorizontal(parent, value.horizontalMargin)
                    }
                }
                else -> return
            }
        }

    var items: List<Boarding> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                adapter.items = value.toMutableList()
                adapter.notifyDataSetChanged()

                binding.indicatorView.setPageSize(0)
                if (autoScrollInterval > 0) {
                    startAutoScroll()
                }

                viewPager.post {
                    viewPager.setCurrentItem(BoardingAdapter.getInitialPosition(canBackOnFirstPosition), false)
                }
            }
            else {
                removeAutoScroll()
                isVisible = false
            }
        }

    init {
        init(attrs)
    }

    private fun removeAutoScroll() {
        if (runnable != null) {
            binding.vpContent.removeCallbacks(runnable)
        }
        runnable = null
    }

    private fun startAutoScroll() {
        removeAutoScroll()
        if (items.isNotEmpty()) {
            runnable = Runnable {
                val currentItem = binding.vpContent.currentItem
                if (currentItem == items.size - 1) {
                    binding.vpContent.setCurrentItem(
                        item = BoardingAdapter.getInitialPosition(canBackOnFirstPosition),
                        duration = 300L,
                    )
                } else {
                    binding.vpContent.setCurrentItem(
                        item = currentItem + 1,
                        duration = 500L,
                    )
                }

                startAutoScroll()
            }
            binding.vpContent.postDelayed(runnable, autoScrollInterval.seconds)
        }
    }

    private fun init(attrs: AttributeSet?) {
        setup()

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.BoardingPagerView,
                0, 0
            )

            binding.indicatorView.apply {
                setIndicatorGap(
                    a.getDimension(
                        R.styleable.BoardingPagerView_indicator_gap,
                        resources.getDimensionPixelSize(R.dimen.xxxs).toFloat())
                )

                binding.root.doOnLayout {
                    setupWithViewPager(viewPager)
                }
            }

            val defaultWidth = a.getDimension(R.styleable.BoardingPagerView_indicator_default_width, context.dimen(R.dimen.xxs))
            val selectedWidth = a.getDimension(R.styleable.BoardingPagerView_indicator_default_width, context.dimen(R.dimen.m1))

            val selectedHeight = a.getDimension(R.styleable.BoardingPagerView_indicator_slider_height, context.dimen(R.dimen.xxs))

            indicatorView.setSliderWidth(defaultWidth, selectedWidth)
            indicatorView.setSliderHeight(selectedHeight)

            autoScrollInterval = a.getInt(R.styleable.BoardingPagerView_autoScrollInterval, 0)
            circular = a.getBoolean(R.styleable.BoardingPagerView_circular, false)
            canBackOnFirstPosition = a.getBoolean(R.styleable.BoardingPagerView_canBackOnFirstPosition, false)

            val iAlignment = a.getInt(R.styleable.BoardingPagerView_indicator_alignment, 1)
            indicatorAlignment = IndicatorAlignment.entries[iAlignment]

            val verticalOffsetPercentage = a.getFloat(R.styleable.BoardingPagerView_content_vertical_offset_percentage, 0f)
            val horizontalOffsetPercentage = a.getFloat(R.styleable.BoardingPagerView_content_horizontal_offset_percentage, 0f)
            contentAlignment = BoardingItemAlignment(
                horizontalAlignmentPercent = horizontalOffsetPercentage,
                verticalAlignmentPercent = verticalOffsetPercentage
            )

            a.recycle()
        }
    }

    private fun setup() {
        binding.vpContent.adapter = adapter

        binding.vpContent.setPageTransformer(
            ScalePageTransformer(
                minScale = 0.9f,
                minAlpha = 0.8f
            )
        )


        // Todo : Click Impl is Postponed due to Indicator View TouchSize is too Small
        /*binding.indicatorView.delegate = object : BoardingDelegate {
            override fun onSelected(position: Int) {
                val fakePosition = adapter.getFakePosition(position)
                if (binding.vpContent.currentItem != fakePosition) {
                    binding.vpContent.currentItem = fakePosition
                }
            }
        }*/

        binding.vpContent.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                delegate?.onPageSelected(position)
                if (binding.indicatorView.getCurrentPosition() != position) {
                    binding.indicatorView.setCurrentPosition(BoardingAdapter.getRealPosition(position))
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                delegate?.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (autoScrollInterval > 0) {
                    startAutoScroll()
                }
            }


        })
    }
}