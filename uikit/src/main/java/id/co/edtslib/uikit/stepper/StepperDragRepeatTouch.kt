package id.co.edtslib.uikit.stepper

import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs

/** Owns one pointer after long press; normal taps and pre-activation scrolling stay with the children. */
internal class StepperDragRepeatTouch(
    private val owner: View,
    private val config: () -> StepperDragRepeatConfig,
    private val findTarget: (MotionEvent) -> Target?,
    private val cancelChildTouch: (MotionEvent) -> Unit,
    private val canChange: (Int) -> Boolean,
    private val changeCount: (Int) -> Boolean,
    private val onActivated: () -> Unit,
    private val onFeedback: (Feedback?) -> Unit,
) {

    val isConsuming: Boolean
        get() = isActive || shouldSuppressUntilRelease

    private val density = owner.resources.displayMetrics.density
    private val touchSlop = ViewConfiguration.get(owner.context).scaledTouchSlop
    private var target: Target? = null
    private var lastEvent: MotionEvent? = null
    private var downX = 0f
    private var downY = 0f
    private var anchorY = 0f
    private var isActive = false
    private var shouldSuppressUntilRelease = false
    private var direction = 0
    private var repeatAnchorTime = 0L
    private val activateRunnable = Runnable { activate() }
    private val repeatRunnable = Runnable { repeat() }

    fun handle(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            finish()
            target = findTarget(event)
            if (target != null) {
                downX = event.rawX
                downY = event.rawY
                remember(event)
                owner.postDelayed(activateRunnable, ViewConfiguration.getLongPressTimeout().toLong())
            }
            return false
        }
        val wasConsuming = isConsuming
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                if (target != null) {
                    remember(event)
                    if (isActive) updateRepeating()
                    else if (abs(event.rawX - downX) > touchSlop || abs(event.rawY - downY) > touchSlop) finish()
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (target != null || wasConsuming) {
                    cancelChildTouch(event)
                    cancel()
                    shouldSuppressUntilRelease = true
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> finish()
        }
        return wasConsuming
    }

    /** Stops callbacks immediately while consuming the remainder of an already captured gesture. */
    fun cancel() {
        val wasConsuming = isConsuming
        finish()
        shouldSuppressUntilRelease = wasConsuming
    }

    fun detach() {
        finish()
    }

    fun refreshBounds() {
        if (isActive) updateRepeating()
    }

    private fun activate() {
        val button = target?.button ?: return
        val event = lastEvent ?: return
        if (!owner.isAttachedToWindow || !owner.isShown || !owner.isEnabled || !button.isEnabled) {
            finish()
            return
        }
        anchorY = event.rawY
        cancelChildTouch(event)
        isActive = true
        button.isPressed = true
        owner.parent?.requestDisallowInterceptTouchEvent(true)
        onActivated()
        updateRepeating()
        // Activation itself deliberately leaves the quantity unchanged.
    }

    private fun remember(event: MotionEvent) {
        lastEvent?.recycle()
        lastEvent = MotionEvent.obtain(event)
    }

    private fun updateRepeating() {
        owner.removeCallbacks(repeatRunnable)
        val event = lastEvent ?: return
        val displacement = (anchorY - event.rawY) / density
        val interval = config().repeatIntervalMillis(displacement)
        val nextDirection = when {
            interval == null -> 0
            displacement > 0f && target?.allowsIncrease == true -> 1
            displacement < 0f -> -1
            else -> 0
        }
        val now = SystemClock.uptimeMillis()
        if (nextDirection != direction) repeatAnchorTime = now
        direction = nextDirection
        val canRepeat = direction != 0 && canChange(direction)
        val speed = interval?.let { 1000f / it } ?: 0f
        val settings = config()
        val speedRange = settings.maximumChangesPerSecond - settings.minimumChangesPerSecond
        val speedProgress = if (speedRange > 0f) (speed - settings.minimumChangesPerSecond) / speedRange else 0f
        onFeedback(Feedback(
            allowsIncrease = target?.allowsIncrease == true,
            direction = direction,
            speedLevel = if (canRepeat) 1 + (speedProgress * 3).toInt().coerceIn(0, 2) else 0,
            isBlocked = direction != 0 && !canRepeat,
        ))
        if (!canRepeat) {
            // No queued changes accumulate at zero, a cap, or the stationary dead zone.
            repeatAnchorTime = now
            return
        }
        owner.postDelayed(repeatRunnable, (repeatAnchorTime + requireNotNull(interval) - now).coerceAtLeast(0L))
    }

    private fun repeat() {
        if (!isActive || !owner.isAttachedToWindow || !owner.isShown || !owner.isEnabled) {
            cancel()
            return
        }
        if (direction == 0 || !canChange(direction) || !changeCount(direction)) return
        repeatAnchorTime = SystemClock.uptimeMillis()
        updateRepeating()
    }

    private fun finish() {
        owner.removeCallbacks(activateRunnable)
        owner.removeCallbacks(repeatRunnable)
        // Pending ordinary taps still need their native pressed/prepressed state on ACTION_UP.
        if (isActive) target?.button?.isPressed = false
        if (isActive) owner.parent?.requestDisallowInterceptTouchEvent(false)
        target = null
        lastEvent?.recycle()
        lastEvent = null
        isActive = false
        shouldSuppressUntilRelease = false
        direction = 0
        repeatAnchorTime = 0L
        onFeedback(null)
    }

    data class Target(val button: View, val allowsIncrease: Boolean)

    data class Feedback(
        val allowsIncrease: Boolean,
        val direction: Int,
        val speedLevel: Int,
        val isBlocked: Boolean,
    )
}
