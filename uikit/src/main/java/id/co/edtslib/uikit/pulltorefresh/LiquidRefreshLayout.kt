package id.co.edtslib.uikit.pulltorefresh

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.color
import androidx.core.content.withStyledAttributes

class LiquidRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var headerBackgroundColor = context.color(R.color.primary_30)
    var headerForegroundColor = context.color(R.color.white)
    var headerCircleRadius = 6
    var loadingIcon: Int? = null

    private var maxPullDistance: Float = 0f
    private var maxHeaderHeight: Float = 0f
    private var contentView: View? = null
    private var headerView: LiquidAnimationView? = null

    private var isRefreshing: Boolean = false

    private var initialTouchY: Float = 0f
    private var currentTouchY: Float = 0f

    private var returnToHeaderAnimator: ValueAnimator? = null
    private var returnToTopAnimator: ValueAnimator? = null

    private val slowDownInterpolator = DecelerateInterpolator(10f)

    private var refreshListener: OnRefreshListener? = null

    init {
        initialize(context, attrs)
    }

    private fun initialize(
        context: Context,
        attrs: AttributeSet?
    ) {

        if (childCount > 1) {
            throw RuntimeException("Only one child is allowed")
        }
        setAttributes(attrs)
        maxPullDistance = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 150f,
            context.resources.displayMetrics
        )
        maxHeaderHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 100f,
            context.resources.displayMetrics
        )

        this.post {
            contentView = getChildAt(0)
            addHeaderView()
        }
    }

    private fun setAttributes(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.LiquidRefreshLayout) {

            headerBackgroundColor = getColor(
                R.styleable.LiquidRefreshLayout_backgroundColor,
                headerBackgroundColor
            )
            headerForegroundColor = getColor(
                R.styleable.LiquidRefreshLayout_foregroundColor,
                headerForegroundColor
            )
            headerCircleRadius = getInt(
                R.styleable.LiquidRefreshLayout_circleRadius,
                headerCircleRadius
            )

        }
    }

    private fun addHeaderView() {
        headerView = LiquidAnimationView(context)
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
        layoutParams.gravity = Gravity.TOP
        headerView!!.layoutParams = layoutParams

        addInternalView(headerView!!)
        headerView!!.setAniBackColor(headerBackgroundColor)
        headerView!!.setAniForeColor(headerForegroundColor)
        headerView!!.setRadius(headerCircleRadius)

        setUpContentAnimation()
    }

    private fun setUpContentAnimation() {
        if (contentView == null) return

        returnToHeaderAnimator = ValueAnimator.ofFloat(maxPullDistance, maxHeaderHeight)
        returnToHeaderAnimator!!.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            contentView?.translationY = value
        }
        returnToHeaderAnimator!!.duration = DRAG_RETURN_DURATION

        returnToTopAnimator = ValueAnimator.ofFloat(maxHeaderHeight, 0f)
        returnToTopAnimator!!.addUpdateListener { animation ->
            var value = animation.animatedValue as Float
            value *= slowDownInterpolator.getInterpolation(value / maxHeaderHeight)
            contentView?.translationY = value
            headerView!!.layoutParams.height = value.toInt()
            headerView!!.requestLayout()
        }
        returnToTopAnimator!!.duration = RETURN_TO_TOP_DURATION

        headerView!!.setOnAnimationDoneListener(object : LiquidAnimationView.OnAnimationDoneListener {
            override fun viewAniDone() {
                returnToTopAnimator!!.start()
            }
        })
    }

    private fun addInternalView(child: View) {
        super.addView(child)
    }

    override fun addView(child: View) {
        if (childCount >= 1) {
            throw RuntimeException("Only one child is allowed")
        }

        contentView = child
        super.addView(child)
        setUpContentAnimation()
    }

    private fun canContentScrollUp(): Boolean {
        return contentView?.canScrollVertically(-1) ?: false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isRefreshing) return true

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchY = ev.y
                currentTouchY = initialTouchY
            }
            MotionEvent.ACTION_MOVE -> {
                val currentY = ev.y
                val distanceY = currentY - initialTouchY
                if (distanceY > 0 && !canContentScrollUp()) {
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isRefreshing) return super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                currentTouchY = event.y

                var distanceY = currentTouchY - initialTouchY
                distanceY = Math.min(maxPullDistance * 2, distanceY)
                distanceY = Math.max(0f, distanceY)

                contentView?.let {
                    val offsetY = slowDownInterpolator.getInterpolation(distanceY / 2f / maxPullDistance) * distanceY / 2

                    it.translationY = offsetY
                    headerView!!.layoutParams.height = offsetY.toInt()
                    headerView!!.requestLayout()
                }

                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                contentView?.let {
                    if (it.translationY >= maxHeaderHeight) {
                        returnToHeaderAnimator!!.start()
                        headerView!!.releaseDrag()
                        isRefreshing = true
                        refreshListener?.refreshing()
                    } else {
                        val currentHeight = it.translationY
                        val backToTopAnimator = ValueAnimator.ofFloat(currentHeight, 0f)
                        backToTopAnimator.addUpdateListener { animation ->
                            var value = animation.animatedValue as Float
                            value *= slowDownInterpolator.getInterpolation(value / maxHeaderHeight)
                            it.translationY = value
                            headerView!!.layoutParams.height = value.toInt()
                            headerView!!.requestLayout()
                        }
                        backToTopAnimator.duration = (currentHeight * RETURN_TO_TOP_DURATION / maxHeaderHeight).toLong()
                        backToTopAnimator.start()
                    }
                }
                return true
            }
            else -> return super.onTouchEvent(event)
        }
    }

    fun finishRefreshing() {
        refreshListener?.completeRefresh()
        isRefreshing = false
        headerView!!.setRefreshing(false)
        finishLoadingAnimation()
    }

    private fun finishLoadingAnimation() {
        val currentHeight = contentView!!.translationY
        val backToTopAnimator = ValueAnimator.ofFloat(currentHeight, 0f)
        backToTopAnimator.addUpdateListener { animation ->
            var value = animation.animatedValue as Float
            value *= slowDownInterpolator.getInterpolation(value / maxHeaderHeight)
            contentView!!.translationY = value
            headerView!!.layoutParams.height = value.toInt()
            headerView!!.requestLayout()
        }
        backToTopAnimator.duration = (currentHeight * RETURN_TO_TOP_DURATION / maxHeaderHeight).toLong()
        backToTopAnimator.start()
    }

    fun setOnRefreshListener(listener: OnRefreshListener) {
        this.refreshListener = listener
    }

    interface OnRefreshListener {
        fun completeRefresh()
        fun refreshing()
    }

    companion object {
        private const val RETURN_TO_TOP_DURATION: Long = 200
        private const val DRAG_RETURN_DURATION: Long = 200
    }
}

