package id.co.edtslib.uikit.pulltorefresh

import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import id.co.edtslib.uikit.header.IRefreshHeader
import id.co.edtslib.uikit.utils.dp
import kotlin.math.abs

/**
 * Header followed by one content view. Hosts own loading completion through [finishRefresh].
 * Insets belong to the host so a layout below a toolbar does not add the status bar twice.
 */
class LiteRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    var refreshDelegate: LiteRefreshDelegate? = null
    val refreshHeader: IRefreshHeader?
        get() = headerView as? IRefreshHeader

    /** Programmatic changes update the indicator; only an accepted pull calls onRefreshing. */
    var isRefreshing: Boolean = false
        set(value) {
            if (field == value) {
                if (!value && currentOffset > 0f) animateToOffset(0f)
                return
            }
            field = value
            if (value) {
                refreshHeader?.onRefreshing()
                animateToOffset(refreshThreshold)
            } else {
                refreshHeader?.onFinish()
                refreshDelegate?.onFinish()
                animateToOffset(0f)
            }
        }

    /** Override when the content is a container around the scrolling child. */
    var canChildScrollUp: (() -> Boolean)? = null

    private var headerView: View? = null
    private var contentView: View? = null
    private var offsetAnimator: ValueAnimator? = null
    private var currentOffset = 0f
    private var initialX = 0f
    private var initialY = 0f
    private var isDragging = false
    private var isDetaching = false
    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val refreshThreshold = 80.dp
    private val maxPullDistance = 120.dp
    private val pullResistance = 0.5f
    private val isMotionEnabled: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ValueAnimator.areAnimatorsEnabled()
        } else {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
        }

    init {
        clipChildren = true
        clipToPadding = true
    }

    fun finishRefresh() {
        isRefreshing = false
    }

    private fun moveViews(offset: Float) {
        currentOffset = offset
        headerView?.translationY = offset
        contentView?.translationY = offset
        refreshHeader?.onPull(offset / refreshThreshold)
        refreshDelegate?.onPull(offset / refreshThreshold, offset)
    }

    private fun animateToOffset(target: Float) {
        offsetAnimator?.cancel()
        if (!isMotionEnabled || !isAttachedToWindow || !isShown || isDetaching) {
            moveViews(target)
            return
        }
        offsetAnimator = ValueAnimator.ofFloat(currentOffset, target).apply {
            duration = 250L
            addUpdateListener { moveViews(it.animatedValue as Float) }
            start()
        }
    }

    private fun cancelPull() {
        isDragging = false
        activePointerId = MotionEvent.INVALID_POINTER_ID
        parent?.requestDisallowInterceptTouchEvent(false)
        if (!isRefreshing) animateToOffset(0f)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        require(childCount == 2 && getChildAt(0) is IRefreshHeader) {
            "LiteRefreshLayout requires a refresh header followed by one content view"
        }
        headerView = getChildAt(0).apply { elevation = 12.dp }
        contentView = getChildAt(1)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec))
        val childWidth = MeasureSpec.makeMeasureSpec(measuredWidth - paddingLeft - paddingRight, MeasureSpec.EXACTLY)
        headerView?.measure(childWidth, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        contentView?.measure(
            childWidth,
            MeasureSpec.makeMeasureSpec(measuredHeight - paddingTop - paddingBottom, MeasureSpec.EXACTLY),
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val childRight = measuredWidth - paddingRight
        headerView?.let { it.layout(paddingLeft, paddingTop - it.measuredHeight, childRight, paddingTop) }
        contentView?.layout(paddingLeft, paddingTop, childRight, measuredHeight - paddingBottom)
        moveViews(currentOffset)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || isRefreshing) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = event.getPointerId(0)
                initialX = event.x
                initialY = event.y
                isDragging = false
                offsetAnimator?.cancel()
            }
            MotionEvent.ACTION_MOVE -> {
                val index = event.findPointerIndex(activePointerId)
                if (index < 0) return false
                val dy = event.getY(index) - initialY
                val dx = event.getX(index) - initialX
                val canScroll = canChildScrollUp?.invoke() ?: (contentView?.canScrollVertically(-1) == true)
                if (dy > touchSlop && dy > abs(dx) && !canScroll) {
                    isDragging = true
                    initialY += touchSlop
                    parent?.requestDisallowInterceptTouchEvent(true)
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> cancelPull()
            MotionEvent.ACTION_POINTER_UP -> cancelPull()
        }
        return isDragging
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || isRefreshing) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val index = event.findPointerIndex(activePointerId)
                if (index < 0 || !isDragging) return false
                moveViews(((event.getY(index) - initialY) * pullResistance).coerceIn(0f, maxPullDistance))
                return true
            }
            MotionEvent.ACTION_UP -> {
                val shouldRefresh = isDragging && currentOffset >= refreshThreshold
                cancelPull()
                if (shouldRefresh) {
                    isRefreshing = true
                    refreshDelegate?.onRefreshing()
                }
                return true
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP -> {
                cancelPull()
                return true
            }
        }
        return isDragging
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (!isShown) {
            offsetAnimator?.cancel()
            isDragging = false
            if (!isRefreshing) moveViews(0f)
        }
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility != View.VISIBLE) {
            offsetAnimator?.cancel()
            isDragging = false
            if (!isRefreshing) moveViews(0f)
        }
    }

    override fun onDetachedFromWindow() {
        isDetaching = true
        offsetAnimator?.cancel()
        offsetAnimator = null
        isDragging = false
        activePointerId = MotionEvent.INVALID_POINTER_ID
        isRefreshing = false
        moveViews(0f)
        super.onDetachedFromWindow()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isDetaching = false
    }
}
