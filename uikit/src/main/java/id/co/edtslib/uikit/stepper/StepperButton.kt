package id.co.edtslib.uikit.stepper

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.os.Build
import android.os.Looper
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewStepperBinding
import id.co.edtslib.uikit.utils.areSystemAnimationsEnabled
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.hideKeyboard
import id.co.edtslib.uikit.utils.inflater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class StepperButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val binding = ViewStepperBinding.inflate(context.inflater, this)

    var buttonTouchTargetMode = StepperButtonTouchTargetMode.COMPACT
        set(value) {
            if (field == value) return
            cancelDragRepeat()
            field = value
            requestLayout()
        }

    /** Uses platform feedback and honors system/user settings; programmatic updates stay silent. */
    var isStepperHapticFeedbackEnabled = false

    /** Shows a temporary direction/speed hint and active outline during an acquired drag. */
    var isDragFeedbackEnabled = false
        set(value) {
            field = value
            if (!value && dragFeedbackDelegate.isInitialized()) dragFeedback.hide(immediately = true)
        }

    /** Optional long-press vertical repetition. Off by default for existing callers. */
    var isDragRepeatEnabled = false
        set(value) {
            if (field == value) return
            field = value
            if (!value) cancelDragRepeat()
        }

    var dragRepeatConfig = StepperDragRepeatConfig()
        set(value) {
            if (field == value) return
            field = value
            cancelDragRepeat()
        }

    /** Keeps both controls visible at zero, without the legacy expansion/collapse animation. */
    var isAlwaysExpanded = false
        set(value) {
            if (field == value) return
            field = value
            removeCallbacks(showExpandedRunnable)
            removeCallbacks(showCollapsedRunnable)
            countAnimator.cancel()
            currentAnimator?.cancel()
            currentAnimator = null
            isAnimating = false
            isExpanded = value || count > 0
            container.isSelected = isExpanded
            container.layoutParams = container.layoutParams.apply {
                width = if (isExpanded) expandedWidth else collapsedWidth
            }
            if (isExpanded) showExpandedViews() else showCollapsedViews()
            singlePlusButton.alpha = 1f
            minusButton.alpha = 1f
            plusButton.alpha = 1f
            countText.alpha = 1f
            countEditText.alpha = 1f
            requestLayout()
        }

    /** Enables the directional numeral roll for accepted taps, drag repetition, and animated updates. */
    var isCountAnimationEnabled = true
        set(value) {
            field = value
            if (!value) countAnimator.cancel()
        }

    var incremental = 1

    // Views
    private val container: MaterialCardView = binding.stepperContainer
    private val singlePlusButton: MaterialButton = binding.singlePlusButton
    private val minusButton: MaterialButton = binding.minusButton
    private val plusButton: MaterialButton = binding.plusButton
    private val countText: TextView = binding.countText
    private val countEditText: EditText = binding.countEditText

    // Coroutine scope for debouncing
    private val viewScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var textChangeJob: Job? = null
    private var debounceDelay = 500L

    // State
    private val countAnimator = StepperCountAnimator()
    private var isPlusAllowed = true
    private var isMinusAllowed = true
    private var displayedCount = 0
    private var count = 0
    private var isExpanded = false
    private var allowTextEditing = true
    private var minValue = 0
    private var maxValue = Int.MAX_VALUE
    private var isUpdatingProgrammatically = false
    private var isAnimating = false
    private var currentAnimator: AnimatorSet? = null
    private val targetBounds = Rect()
    private val editorBounds = Rect()
    private var touchButton: View? = null
    private var touchOriginX = 0f
    private var touchOriginY = 0f
    private val dragFeedbackDelegate = lazy { StepperDragFeedback(this, container) }
    private val dragFeedback by dragFeedbackDelegate
    private val showExpandedRunnable = Runnable { if (isExpanded) showExpandedViews() }
    private val showCollapsedRunnable = Runnable { if (!isExpanded && !isAlwaysExpanded) showCollapsedViews() }
    private val dragRepeatTouchDelegate = lazy {
        StepperDragRepeatTouch(
            owner = this,
            config = { dragRepeatConfig },
            findTarget = ::findDragTarget,
            cancelChildTouch = ::cancelChildTouch,
            canChange = { direction ->
                incremental > 0 && if (direction > 0) count < maxValue && plusButton.isEnabled
                    else count > minValue && minusButton.isEnabled
            },
            changeCount = { direction ->
                val previous = count
                if (direction > 0) {
                    incrementCount()
                    if (!isAlwaysExpanded) expand()
                } else {
                    decrementCount()
                }
                count != previous
            },
            onActivated = { performStepperHaptic(isActivation = true) },
            onFeedback = { feedback ->
                if (feedback != null && isDragFeedbackEnabled) dragFeedback.show(feedback)
                else if (dragFeedbackDelegate.isInitialized()) dragFeedback.hide()
            },
        )
    }
    private val dragRepeatTouch by dragRepeatTouchDelegate

    // Colors

    // Dimensions
    private val collapsedWidth = context.dimenPixelSize(R.dimen.l) ?: 32.dp.toInt()
    private val expandedWidth = context.dimenPixelSize(R.dimen.dimen_90) ?: 90.dp.toInt()

    // Icons
    private var plusIconRes = R.drawable.ic_plus_stepper
    private var minusIconRes = R.drawable.ic_minus_stepper

    // Listener
    private var onCountChangeListener: ((Int) -> Unit)? = null

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.StepperButton, defStyleAttr, 0)
        try {
            buttonTouchTargetMode = StepperButtonTouchTargetMode.entries[
                attributes.getInt(R.styleable.StepperButton_buttonTouchTargetMode, 0).coerceIn(0, 1)
            ]
            isStepperHapticFeedbackEnabled = attributes.getBoolean(
                R.styleable.StepperButton_isStepperHapticFeedbackEnabled, false
            )
            isDragFeedbackEnabled = attributes.getBoolean(R.styleable.StepperButton_isDragFeedbackEnabled, false)
        } finally {
            attributes.recycle()
        }

        // Set icons
        singlePlusButton.setIconResource(plusIconRes)
        plusButton.setIconResource(plusIconRes)
        minusButton.setIconResource(minusIconRes)

        // Setup initial state
        setupInitialState()
        setupClickListeners()
        setupEditText()
        refreshButtonStates()
    }

    private fun setupInitialState() {
        // Start in collapsed state
        container.layoutParams = LayoutParams(collapsedWidth, collapsedWidth, Gravity.CENTER)
        singlePlusButton.visibility = VISIBLE
        minusButton.visibility = GONE
        plusButton.visibility = GONE
        countText.visibility = GONE
        countEditText.visibility = GONE
    }

    private fun setupClickListeners() {
        singlePlusButton.setOnClickListener {
            if (!isAnimating) {
                incrementCount()
                expand()
            }
        }

        plusButton.setOnClickListener {
            incrementCount()
        }

        minusButton.setOnClickListener {
            decrementCount()
        }
    }

    private fun setupEditText() {
        countEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Prevent infinite loop when updating programmatically
                if (isUpdatingProgrammatically) return

                val text = s?.toString() ?: ""

                // Handle empty input - don't update yet
                if (text.isEmpty()) {
                    return
                }

                // Cancel previous debounce job
                textChangeJob?.cancel()

                // Start new debounced job
                textChangeJob = viewScope.launch {
                    delay(debounceDelay)

                    // Validate input
                    val value = text.toIntOrNull()

                    if (value == null) {
                        // Invalid input - revert to last valid value
                        withContext(Dispatchers.Main) {
                            isUpdatingProgrammatically = true
                            countEditText.setText(count.toString())
                            countEditText.setSelection(countEditText.text.length)
                            isUpdatingProgrammatically = false
                        }
                        return@launch
                    }

                    // Clamp value to min/max
                    val clampedValue = value.coerceIn(minValue, maxValue)

                    if (clampedValue != value) {
                        // Show user the clamped value
                        withContext(Dispatchers.Main) {
                            isUpdatingProgrammatically = true
                            countEditText.setText(clampedValue.toString())
                            countEditText.setSelection(countEditText.text.length)
                            isUpdatingProgrammatically = false
                        }
                    }

                    // Update count if changed
                    if (clampedValue != count) {
                        withContext(Dispatchers.Main) {
                            count = clampedValue
                            updateCountDisplay()
                            performStepperHaptic()
                            onCountChangeListener?.invoke(count)

                            // Collapse if count reaches 0
                            if (count == 0) {
                                collapse()
                            }
                        }
                    }
                }
            }
        })

        countEditText.setOnFocusChangeListener { _, hasFocus ->
            countAnimator.cancel()
            if (!hasFocus) {
                hideKeyboard()

                if (count == 0) {
                    collapse()
                }
            }
        }
    }

    private fun expand() {
        if (isExpanded || isAnimating) return
        countAnimator.cancel()
        if (!context.areSystemAnimationsEnabled()) {
            snapExpandedState(true)
            return
        }
        isExpanded = true
        container.isSelected = true
        isAnimating = true

        currentAnimator?.cancel()

        val widthAnimator = ValueAnimator.ofInt(collapsedWidth, expandedWidth).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val params = container.layoutParams
                params.width = animation.animatedValue as Int
                container.layoutParams = params
            }
        }

        val fadeOutAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 150
            addUpdateListener { animation ->
                singlePlusButton.alpha = animation.animatedValue as Float
            }
        }

        val fadeInAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 150
            startDelay = 150
            addUpdateListener { animation ->
                val alpha = animation.animatedValue as Float
                minusButton.alpha = alpha
                plusButton.alpha = alpha
                if (allowTextEditing) {
                    countEditText.alpha = alpha
                } else {
                    countText.alpha = alpha
                }
            }
        }

        currentAnimator = AnimatorSet().apply {
            play(widthAnimator)
            play(fadeOutAnimator).with(widthAnimator)
            play(fadeInAnimator).after(fadeOutAnimator)
            doOnEnd {
                isAnimating = false
            }
            start()
        }

        postDelayed(showExpandedRunnable, 150)
    }

    private fun collapse() {
        if (isAlwaysExpanded || !isExpanded || isAnimating) return
        countAnimator.cancel()
        if (!context.areSystemAnimationsEnabled()) {
            snapExpandedState(false)
            return
        }
        isExpanded = false
        container.isSelected = false
        isAnimating = true

        countEditText.clearFocus()
        hideKeyboard()

        currentAnimator?.cancel()

        val widthAnimator = ValueAnimator.ofInt(expandedWidth, collapsedWidth).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val params = container.layoutParams
                params.width = animation.animatedValue as Int
                container.layoutParams = params
            }
        }

        val fadeOutAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 150
            addUpdateListener { animation ->
                val alpha = animation.animatedValue as Float
                minusButton.alpha = alpha
                plusButton.alpha = alpha
                if (allowTextEditing) {
                    countEditText.alpha = alpha
                } else {
                    countText.alpha = alpha
                }
            }
        }

        val fadeInAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 150
            startDelay = 150
            addUpdateListener { animation ->
                singlePlusButton.alpha = animation.animatedValue as Float
            }
        }

        currentAnimator = AnimatorSet().apply {
            play(widthAnimator)
            play(fadeOutAnimator).with(widthAnimator)
            play(fadeInAnimator).after(fadeOutAnimator)
            doOnEnd {
                isAnimating = false
            }
            start()
        }

        postDelayed(showCollapsedRunnable, 150)
    }

    private fun incrementCount() {
        if (incremental > 0 && count < maxValue) {
            count = (count.toLong() + incremental).coerceAtMost(maxValue.toLong()).toInt()
            updateCountDisplay(animate = true)
            performStepperHaptic()
            onCountChangeListener?.invoke(count)
        }
    }

    private fun decrementCount() {
        if (incremental > 0 && count > minValue) {
            count = (count.toLong() - incremental).coerceAtLeast(minValue.toLong()).toInt()
            updateCountDisplay(animate = true)
            performStepperHaptic()
            onCountChangeListener?.invoke(count)

            if (count == 0) {
                collapse()
            }
        }
    }

    private fun updateCountDisplay(animate: Boolean = false) {
        val previous = displayedCount
        if (previous != count) countAnimator.cancel()
        displayedCount = count
        isUpdatingProgrammatically = true
        countText.text = count.toString()
        if (countEditText.text.toString() != count.toString()) {
            countEditText.setText(count.toString())
        }
        isUpdatingProgrammatically = false
        refreshButtonStates()

        val view = if (allowTextEditing) countEditText else countText
        if (animate && previous != count && isCountAnimationEnabled && isEnabled && isExpanded && !isAnimating &&
            (count != 0 || isAlwaysExpanded) && !countEditText.hasFocus() && isShown &&
            context.areSystemAnimationsEnabled()) {
            countAnimator.start(view, previous, count)
        }
    }

    private fun refreshButtonStates() {
        singlePlusButton.isEnabled = isEnabled && isPlusAllowed && count < maxValue
        plusButton.isEnabled = singlePlusButton.isEnabled
        minusButton.isEnabled = isEnabled && isMinusAllowed && count > minValue
        countText.isEnabled = isEnabled
        countEditText.isEnabled = isEnabled
        container.isEnabled = isEnabled
        container.isSelected = isExpanded
    }

    private fun snapExpandedState(expanded: Boolean) {
        countAnimator.cancel()
        removeCallbacks(showExpandedRunnable)
        removeCallbacks(showCollapsedRunnable)
        currentAnimator?.cancel()
        currentAnimator = null
        isAnimating = false
        isExpanded = expanded
        container.isSelected = expanded
        container.layoutParams = container.layoutParams.apply {
            width = if (expanded) expandedWidth else collapsedWidth
        }
        if (expanded) showExpandedViews() else showCollapsedViews()
        listOf(singlePlusButton, minusButton, plusButton, countText, countEditText).forEach { it.alpha = 1f }
    }

    private fun showExpandedViews() {
        singlePlusButton.visibility = GONE
        minusButton.visibility = VISIBLE
        plusButton.visibility = VISIBLE
        countText.visibility = if (allowTextEditing) GONE else VISIBLE
        countEditText.visibility = if (allowTextEditing) VISIBLE else GONE
    }

    private fun showCollapsedViews() {
        singlePlusButton.visibility = VISIBLE
        minusButton.visibility = GONE
        plusButton.visibility = GONE
        countText.visibility = GONE
        countEditText.visibility = GONE
    }

    private fun findDragTarget(event: MotionEvent): StepperDragRepeatTouch.Target? {
        if (!isDragRepeatEnabled || !isEnabled) return null
        val button = findTouchButton(event) ?: return null
        return StepperDragRepeatTouch.Target(button, button !== minusButton)
    }

    private fun findTouchButton(event: MotionEvent): View? {
        if (!isEnabled || isAnimating) return null
        val x = event.x.toInt()
        val y = event.y.toInt()
        if (allowTextEditing && countEditText.isShown) {
            getDescendantBounds(countEditText, editorBounds)
            if (x >= editorBounds.left && x < editorBounds.right) return null
        }
        val size = if (buttonTouchTargetMode == StepperButtonTouchTargetMode.EXPANDED) 48.dp else 32.dp
        return listOf(singlePlusButton, plusButton, minusButton).firstOrNull { button ->
            if (!button.isShown || !button.isEnabled) return@firstOrNull false
            getDescendantBounds(button, targetBounds)
            val centerX = targetBounds.exactCenterX()
            val centerY = targetBounds.exactCenterY()
            var left = (centerX - size / 2).toInt()
            var right = left + size.toInt()
            if (allowTextEditing && countEditText.isShown) {
                if (button === minusButton) {
                    right = minOf(right, editorBounds.left)
                    if (buttonTouchTargetMode == StepperButtonTouchTargetMode.EXPANDED) left = right - size.toInt()
                } else {
                    left = maxOf(left, editorBounds.right)
                    if (buttonTouchTargetMode == StepperButtonTouchTargetMode.EXPANDED) right = left + size.toInt()
                }
            }
            targetBounds.set(left, (centerY - size / 2).toInt(), right, (centerY + size / 2).toInt())
            targetBounds.intersect(0, 0, width, height) && targetBounds.contains(x, y)
        }
    }

    private fun getDescendantBounds(view: View, bounds: Rect) {
        view.getDrawingRect(bounds)
        offsetDescendantRectToMyCoords(view, bounds)
    }

    private fun dispatchButtonTouch(event: MotionEvent): Boolean {
        val button = touchButton ?: return false
        val forwarded = MotionEvent.obtain(event)
        forwarded.setLocation(button.width / 2f + event.x - touchOriginX, button.height / 2f + event.y - touchOriginY)
        return try {
            button.dispatchTouchEvent(forwarded)
        } finally {
            forwarded.recycle()
        }
    }

    private fun performStepperHaptic(isActivation: Boolean = false) {
        if (!isStepperHapticFeedbackEnabled || !isHapticFeedbackEnabled) return
        val effect = when {
            isActivation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.GESTURE_START
            isActivation -> HapticFeedbackConstants.LONG_PRESS
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
            else -> HapticFeedbackConstants.CLOCK_TICK
        }
        performHapticFeedback(effect)
    }

    private fun cancelChildTouch(event: MotionEvent) {
        val cancellation = MotionEvent.obtain(event)
        cancellation.action = MotionEvent.ACTION_CANCEL
        if (touchButton != null) dispatchButtonTouch(cancellation) else super.dispatchTouchEvent(cancellation)
        cancellation.recycle()
    }

    /** Call when rebinding this view to a different item or dismissing its owner. */
    fun cancelDragRepeat() {
        if (dragRepeatTouchDelegate.isInitialized()) dragRepeatTouch.cancel()
        touchButton?.let { button ->
            val now = SystemClock.uptimeMillis()
            val cancellation = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0f, 0f, 0)
            try {
                button.dispatchTouchEvent(cancellation)
            } finally {
                cancellation.recycle()
            }
        }
    }

    // Public API
    fun setCount(value: Int, animate: Boolean = true) {
        // Ensure we're on the main thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            post { setCount(value, animate) }
            return
        }

        val previous = count
        count = value.coerceIn(minValue, maxValue)
        updateCountDisplay(animate)

        if (animate) {
            if (count > 0 && !isExpanded) {
                expand()
            } else if (count == 0 && isExpanded) {
                collapse()
            }
        } else if (previous != count || isExpanded != (isAlwaysExpanded || count > 0)) {
            snapExpandedState(isAlwaysExpanded || count > 0)
        }
    }

    fun getCount(): Int = count

    fun setOnCountChangeListener(listener: (Int) -> Unit) {
        onCountChangeListener = listener
    }

    fun clearListener() {
        onCountChangeListener = null
    }

    fun setPlusButtonEnabled(enabled: Boolean) {
        isPlusAllowed = enabled
        refreshButtonStates()
        if (dragRepeatTouchDelegate.isInitialized()) dragRepeatTouch.refreshBounds()
    }

    fun setMinusButtonEnabled(enabled: Boolean) {
        isMinusAllowed = enabled
        refreshButtonStates()
        if (dragRepeatTouchDelegate.isInitialized()) dragRepeatTouch.refreshBounds()
    }

    fun setAllowTextEditing(allow: Boolean) {
        if (allowTextEditing == allow) return
        cancelDragRepeat()
        countAnimator.cancel()
        allowTextEditing = allow
        requestLayout()
        if (isExpanded) {
            if (allow) {
                countText.visibility = GONE
                countEditText.visibility = VISIBLE
            } else {
                countText.visibility = VISIBLE
                countEditText.visibility = GONE
            }
        }
    }

    fun setMinValue(value: Int) {
        minValue = value
        if (count < minValue) {
            setCount(minValue)
        }
        refreshButtonStates()
        if (dragRepeatTouchDelegate.isInitialized()) dragRepeatTouch.refreshBounds()
    }

    fun setMaxValue(value: Int) {
        maxValue = value
        if (count > maxValue) {
            setCount(maxValue)
        }
        refreshButtonStates()
        if (dragRepeatTouchDelegate.isInitialized()) dragRepeatTouch.refreshBounds()
    }

    fun setIncrementalValue(value: Int) {
        incremental = value
    }

    fun setDebounceDelay(delayMs: Long) {
        debounceDelay = delayMs
    }

    override fun setEnabled(enabled: Boolean) {
        val hasEnabledChanged = isEnabled != enabled
        super.setEnabled(enabled)
        if (!enabled) cancelDragRepeat()
        if (hasEnabledChanged) countAnimator.cancel()
        refreshButtonStates()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            touchButton = findTouchButton(event)
            touchOriginX = event.x
            touchOriginY = event.y
        }
        val hasButtonTarget = touchButton != null
        val isFinished = event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL
        if ((isDragRepeatEnabled || (dragRepeatTouchDelegate.isInitialized() && dragRepeatTouch.isConsuming)) &&
            dragRepeatTouch.handle(event)) {
            if (isFinished) touchButton = null
            return true
        }
        if (hasButtonTarget) {
            dispatchButtonTouch(event)
            if (isFinished) touchButton = null
            return true
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (buttonTouchTargetMode != StepperButtonTouchTargetMode.EXPANDED) return
        // Reserve space only for opt-in wrap-content hosts. EXACTLY/AT_MOST constraints remain authoritative.
        val extraWidth = if (isExpanded && allowTextEditing) 40.dp else 16.dp
        val desiredWidth = container.layoutParams.width + extraWidth.toInt() + paddingLeft + paddingRight
        val desiredHeight = 48.dp.toInt() + paddingTop + paddingBottom
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(resolveSize(maxOf(measuredWidth, desiredWidth), widthMeasureSpec), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(resolveSize(maxOf(measuredHeight, desiredHeight), heightMeasureSpec), MeasureSpec.EXACTLY),
        )
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        // Child IDs repeat in every stepper; save only the owning view's state.
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    override fun onSaveInstanceState(): Parcelable = Bundle().apply {
        putParcelable(STATE_SUPER, super.onSaveInstanceState())
        putInt(STATE_COUNT, count)
    }

    @Suppress("DEPRECATION")
    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable(STATE_SUPER))
            setCount(state.getInt(STATE_COUNT), animate = false)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility != VISIBLE) {
            countAnimator.cancel()
            cancelDragRepeat()
            if (dragFeedbackDelegate.isInitialized()) dragFeedback.hide(immediately = true)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Cancel all pending jobs and animations
        textChangeJob?.cancel()
        countAnimator.cancel()
        currentAnimator?.cancel()
        removeCallbacks(showExpandedRunnable)
        removeCallbacks(showCollapsedRunnable)
        cancelDragRepeat()
        if (dragRepeatTouchDelegate.isInitialized()) dragRepeatTouch.detach()
        if (dragFeedbackDelegate.isInitialized()) dragFeedback.hide(immediately = true)
        touchButton = null
        viewScope.cancel()
    }

    enum class Type {
        Filled, Outlined,
    }

    companion object {
        private const val STATE_SUPER = "stepper_super_state"
        private const val STATE_COUNT = "stepper_count"
    }
}
