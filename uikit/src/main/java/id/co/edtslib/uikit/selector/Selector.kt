package id.co.edtslib.uikit.selector

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Checkable
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.radiobutton.MaterialRadioButton
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewSelectorBinding
import kotlin.properties.Delegates

class Selector @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr), Checkable {

    enum class SelectorType {
        CHECKBOX, RADIO_BUTTON
    }

    private val binding = ViewSelectorBinding.inflate(LayoutInflater.from(context), this, true)
    private val checkbox: MaterialCheckBox = binding.checkbox
    private val radioButton: MaterialRadioButton = binding.radioButton

    private var selectorType: SelectorType by Delegates.observable(SelectorType.CHECKBOX) { _, old, new ->
        if (old != new) {
            setupSelector()
        }
    }

    private var internalCheckboxListener: MaterialCheckBox.OnCheckedStateChangedListener? = null

    var delegate: SelectorDelegate? = null

    var title: String? = "Selector Title"
        set(value) {
            field = value
            binding.tvTitle.text = value
        }

    var isTitleVisible: Boolean = true
        set(value) {
            field = value
            binding.tvTitle.isVisible = value
        }

    var subtitle: String? = "Body text goes here"
        set(value) {
            field = value
            binding.tvSubtitle.text = value
        }

    var isSubtitleVisible: Boolean = true
        set(value) {
            field = value
            binding.tvSubtitle.isVisible = value
        }

    init {
        applyAttributes(attrs)
        setupSelector()

        setOnClickListener {
            if (isEnabled) {
                when (selectorType) {
                    SelectorType.CHECKBOX -> toggle()
                    SelectorType.RADIO_BUTTON -> if (!isChecked) toggle()
                }
            }
        }
    }

    private fun setupSelector() {
        internalCheckboxListener?.let {
            checkbox.removeOnCheckedStateChangedListener(it)
        }
        radioButton.setOnCheckedChangeListener(null)

        when (selectorType) {
            SelectorType.CHECKBOX -> {
                checkbox.isVisible = true
                radioButton.isVisible = false

                val tempListener = MaterialCheckBox.OnCheckedStateChangedListener { _, state ->
                    refreshDrawableState()
                    delegate?.onCheckedStateChanged(state)
                }

                checkbox.addOnCheckedStateChangedListener(tempListener)
                internalCheckboxListener = tempListener
            }
            SelectorType.RADIO_BUTTON -> {
                checkbox.isVisible = false
                radioButton.isVisible = true
                radioButton.setOnCheckedChangeListener { _, isChecked ->
                    refreshDrawableState()
                    delegate?.onCheckedChanged(isChecked)
                }
            }
        }
    }

    private fun applyAttributes(attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.Selector, 0, 0).apply {
            try {
                val typeValue = getInt(R.styleable.Selector_selectorType, 0)
                selectorType = if (typeValue == 0) SelectorType.CHECKBOX else SelectorType.RADIO_BUTTON

                title = getString(R.styleable.Selector_titleText) ?: title
                subtitle = getString(R.styleable.Selector_subTitleText) ?: subtitle
                isTitleVisible = getBoolean(R.styleable.Selector_titleVisible, true)
                isSubtitleVisible = getBoolean(R.styleable.Selector_subtitleVisible, true)
            } finally {
                recycle()
            }
        }

        if (selectorType == SelectorType.CHECKBOX) {
            context.obtainStyledAttributes(attrs, intArrayOf(com.google.android.material.R.attr.checkedState)).apply {
                try {
                    checkbox.checkedState = getInt(0, checkbox.checkedState)
                } finally {
                    recycle()
                }
            }
        }

        context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.checked)).apply {
            try {
                val isChecked = getBoolean(0, false)
                when (selectorType) {
                    SelectorType.CHECKBOX -> {
                        if (isChecked) checkbox.checkedState = MaterialCheckBox.STATE_CHECKED
                    }
                    SelectorType.RADIO_BUTTON -> radioButton.isChecked = isChecked
                }
            } finally {
                recycle()
            }
        }

        context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.enabled)).apply {
            try {
                isEnabled = getBoolean(0, true)
            } finally {
                recycle()
            }
        }
    }

    override fun isChecked(): Boolean {
        return when (selectorType) {
            SelectorType.CHECKBOX -> checkbox.checkedState == MaterialCheckBox.STATE_CHECKED
            SelectorType.RADIO_BUTTON -> radioButton.isChecked
        }
    }

    override fun setChecked(checked: Boolean) {
        when (selectorType) {
            SelectorType.CHECKBOX -> {
                checkbox.checkedState = if (checked) MaterialCheckBox.STATE_CHECKED
                else MaterialCheckBox.STATE_UNCHECKED
            }
            SelectorType.RADIO_BUTTON -> radioButton.isChecked = checked
        }
    }

    override fun toggle() {
        when (selectorType) {
            SelectorType.CHECKBOX -> checkbox.toggle()
            SelectorType.RADIO_BUTTON -> {
                if (!isChecked) radioButton.isChecked = true
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        checkbox.isEnabled = enabled
        radioButton.isEnabled = enabled
        binding.tvTitle.isEnabled = enabled
        binding.tvSubtitle.isEnabled = enabled

        refreshDrawableState()
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)

        when (selectorType) {
            SelectorType.CHECKBOX -> {
                if (checkbox.checkedState == MaterialCheckBox.STATE_CHECKED) {
                    mergeDrawableStates(drawableState, intArrayOf(android.R.attr.state_checked))
                } else if (checkbox.checkedState == MaterialCheckBox.STATE_INDETERMINATE) {
                    mergeDrawableStates(drawableState, intArrayOf(com.google.android.material.R.attr.state_indeterminate))
                }
            }
            SelectorType.RADIO_BUTTON -> {
                if (radioButton.isChecked) {
                    mergeDrawableStates(drawableState, intArrayOf(android.R.attr.state_checked))
                }
            }
        }

        return drawableState
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()

        checkbox.refreshDrawableState()
        radioButton.refreshDrawableState()
        binding.tvTitle.refreshDrawableState()
        binding.tvSubtitle.refreshDrawableState()
    }
}