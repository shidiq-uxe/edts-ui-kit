package id.co.edtslib.uikit.stepper

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
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
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.hideKeyboard
import id.co.edtslib.uikit.utils.inflater
import id.co.edtslib.uikit.utils.px
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
    private var count = 0
    private var isExpanded = false
    private var allowTextEditing = true
    private var minValue = 0
    private var maxValue = Int.MAX_VALUE
    private var isUpdatingProgrammatically = false
    private var isAnimating = false
    private var currentAnimator: AnimatorSet? = null

    var incremental = 1

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
        /*// Parse custom attributes
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.Stepp,
            0, 0
        ).apply {
            try {
                plusIconRes = getResourceId(R.styleable.StepperView_plusIcon, plusIconRes)
                minusIconRes = getResourceId(R.styleable.StepperView_minusIcon, minusIconRes)
                allowTextEditing = getBoolean(R.styleable.StepperView_allowTextEditing, false)
                minValue = getInt(R.styleable.StepperView_minValue, 0)
                maxValue = getInt(R.styleable.StepperView_maxValue, Int.MAX_VALUE)
            } finally {
                recycle()
            }
        }*/

        // Set icons
        singlePlusButton.setIconResource(plusIconRes)
        plusButton.setIconResource(plusIconRes)
        minusButton.setIconResource(minusIconRes)

        // Setup initial state
        setupInitialState()
        setupClickListeners()
        setupEditText()
    }

    private fun setupInitialState() {
        // Start in collapsed state
        container.layoutParams = LayoutParams(collapsedWidth, collapsedWidth)
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
        isExpanded = true
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

        postDelayed({
            singlePlusButton.visibility = GONE
            minusButton.visibility = VISIBLE
            plusButton.visibility = VISIBLE
            if (allowTextEditing) {
                countEditText.visibility = VISIBLE
                countText.visibility = GONE
            } else {
                countText.visibility = VISIBLE
                countEditText.visibility = GONE
            }
        }, 150)
    }

    private fun collapse() {
        if (!isExpanded || isAnimating) return
        isExpanded = false
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

        postDelayed({
            singlePlusButton.visibility = VISIBLE
            minusButton.visibility = GONE
            plusButton.visibility = GONE
            countText.visibility = GONE
            countEditText.visibility = GONE
        }, 150)
    }

    private fun incrementCount() {
        if (count < maxValue) {
            count += incremental
            updateCountDisplay()
            onCountChangeListener?.invoke(count)
        }
    }

    private fun decrementCount() {
        if (count > minValue && count - incremental >= minValue) {
            count -= incremental
            updateCountDisplay()
            onCountChangeListener?.invoke(count)

            if (count == 0) {
                collapse()
            }
        }
    }

    private fun updateCountDisplay() {
        isUpdatingProgrammatically = true
        countText.text = count.toString()
        if (allowTextEditing && countEditText.text.toString() != count.toString()) {
            countEditText.setText(count.toString())
        }
        isUpdatingProgrammatically = false
    }

    // Public API
    fun setCount(value: Int, animate: Boolean = true) {
        // Ensure we're on the main thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            post { setCount(value, animate) }
            return
        }

        count = value.coerceIn(minValue, maxValue)
        updateCountDisplay()

        if (animate) {
            if (count > 0 && !isExpanded) {
                expand()
            } else if (count == 0 && isExpanded) {
                collapse()
            }
        }
    }

    fun getCount(): Int = count

    fun setOnCountChangeListener(listener: (Int) -> Unit) {
        onCountChangeListener = listener
    }

    fun clearListener() {
        onCountChangeListener = null
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        singlePlusButton.isEnabled = enabled
        minusButton.isEnabled = enabled
        plusButton.isEnabled = enabled
        countEditText.isEnabled = enabled
    }

    fun setPlusButtonEnabled(enabled: Boolean) {
        singlePlusButton.isEnabled = enabled
        plusButton.isEnabled = enabled
    }

    fun setMinusButtonEnabled(enabled: Boolean) {
        minusButton.isEnabled = enabled
    }

    fun setAllowTextEditing(allow: Boolean) {
        allowTextEditing = allow
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
    }

    fun setMaxValue(value: Int) {
        maxValue = value
        if (count > maxValue) {
            setCount(maxValue)
        }
    }

    fun setIncrementalValue(value: Int) {
        incremental = value
    }

    fun setDebounceDelay(delayMs: Long) {
        debounceDelay = delayMs
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Cancel all pending jobs and animations
        textChangeJob?.cancel()
        currentAnimator?.cancel()
        viewScope.cancel()
    }

    enum class Type {
        Filled, Outlined,
    }
}