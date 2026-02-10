package id.co.edtslib.uikit.pulltorefresh

import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.AppBarLayout
import id.co.edtslib.uikit.header.IRefreshHeader
import id.co.edtslib.uikit.utils.dp
import kotlinx.coroutines.delay

class LiteRefreshLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    private var headerView: View? = null
    private var contentView: View? = null

    var refreshDelegate: LiteRefreshDelegate? = null
    val refreshHeader: IRefreshHeader?
        get() = headerView as? IRefreshHeader

    private var appBarOffset = 0
    private var topInset = 0
    private var headerHeight = 0
    private var currentOffset = 0f
    private val pullResistance = 0.5f

    private val refreshThreshold = 80.dp
    private val maxPullDistance = 120.dp

    init {
        clipChildren = false
        clipToPadding = false

        setupInset()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount >= 2) {
            headerView = getChildAt(0)
            contentView = getChildAt(1)

            setupAppBarListener()
        } else if (childCount == 1) {
            contentView = getChildAt(0)
            headerView = null
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)

        val childWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        val childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)

        headerView?.measure(childWidthSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        contentView?.measure(childWidthSpec, childHeightSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = measuredWidth
        val height = measuredHeight

        headerView?.let {
            headerHeight = it.measuredHeight
            it.layout(0, -headerHeight, width, 0)
        }

        contentView?.layout(0, 0, width, height)
    }

    private var lastY = 0f

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var initialDownY = 0f

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                initialDownY = ev.y
                lastY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = ev.y - initialDownY
                if (dy > touchSlop && dy > 0 && appBarOffset == 0 && contentView?.canScrollVertically(-1) == false) {
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private fun isAppBarExpanded(): Boolean {
        val coordinator = contentView as? ViewGroup ?: return true
        for (i in 0 until coordinator.childCount) {
            val child = coordinator.getChildAt(i)
            if (child is AppBarLayout) {
                return child.top == 0
            }
        }
        return true
    }

    private fun setupAppBarListener() {
        val coordinator = contentView as? ViewGroup ?: return
        for (i in 0 until coordinator.childCount) {
            val child = coordinator.getChildAt(i)
            if (child is AppBarLayout) {
                child.addOnOffsetChangedListener { _, verticalOffset ->
                    appBarOffset = verticalOffset
                    moveViews(currentOffset)
                }
                break
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val dy = (event.y - lastY) * pullResistance
                currentOffset = (currentOffset + dy).coerceIn(0f, maxPullDistance)
                moveViews(currentOffset)
                lastY = event.y
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (currentOffset >= refreshThreshold) {
                    refreshHeader?.onRefreshing()
                    refreshDelegate?.onRefreshing()
                    animateToOffset(refreshThreshold)
                    // TODO: Listener & Delete Handler

                    Handler(Looper.getMainLooper()).postDelayed({
                        finishRefresh()

                    }, 2500)
                } else {
                    animateToOffset(0f)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun setupInset() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            topInset = systemBars.top
            requestLayout()
            insets
        }
    }

    private fun moveViews(offset: Float) {
        // 1. Calculate the Header's Y position
        // Base: If pulling, start from topInset.
        // Add appBarOffset: If the AppBar scrolls up 50px, the header moves up 50px too.
        val headerBaseY = if (offset > 0) offset + topInset else offset
        headerView?.translationY = headerBaseY + appBarOffset

        // 2. Animate the icon scale/rotation based on the pull
        val progress = offset / refreshThreshold
        refreshHeader?.onPull(progress)
        refreshDelegate?.onPull(progress, offset)

        // 3. Move the Content (CoordinatorLayout)
        // Only translate by the pull offset. The CoordinatorLayout handles
        // its own internal scrolling (AppBar collapse) automatically.
        contentView?.translationY = offset
    }

    private fun animateToOffset(target: Float) {
        ValueAnimator.ofFloat(currentOffset, target).apply {
            duration = 250
            addUpdateListener {
                currentOffset = it.animatedValue as Float
                moveViews(currentOffset)
            }
            start()
        }
    }

    fun finishRefresh() {
        animateToOffset(0f)
        refreshHeader?.onFinish()
        refreshDelegate?.onFinish()
    }
}