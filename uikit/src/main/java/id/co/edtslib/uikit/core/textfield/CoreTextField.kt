package id.co.edtslib.uikit.core.textfield

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.util.SparseArray
import android.view.TouchDelegate
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.res.use
import androidx.core.content.withStyledAttributes
import androidx.core.text.buildSpannedString
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doAfterTextChanged
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewCoreTextFieldBinding
import id.co.edtslib.uikit.utils.TextStyle
import id.co.edtslib.uikit.utils.buildHighlightedMessage
import id.co.edtslib.uikit.utils.colorAttr
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.inflater
import id.co.edtslib.uikit.utils.showKeyboard
import id.co.edtslib.uikit.utils.typefaceFromAppearance
import kotlin.math.roundToInt

/** A single-line editor. The XML recipe supplies appearance, including focus and error colors. */
open class CoreTextField protected constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    @StyleRes themeOverlay: Int
) : LinearLayout(ContextThemeWrapper(context, themeOverlay), attrs, defStyleAttr) {

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.coreTextFieldStyle
    ) : this(context, attrs, defStyleAttr, R.style.ThemeOverlay_EDTS_UIKit_CoreTextField)

    var text: CharSequence
        get() = binding.etInput.text ?: ""
        set(value) {
            binding.etInput.setText(value)
            binding.etInput.setSelection(binding.etInput.length())
        }

    var label: CharSequence? = null
        set(value) {
            field = value
            renderLabel()
        }

    var placeholder: CharSequence?
        get() = binding.etInput.hint
        set(value) {
            binding.etInput.hint = value
        }

    var isFieldRequired = false
        set(value) {
            field = value
            renderLabel()
        }

    var supportingText: CharSequence? = null
        set(value) {
            field = value
            renderSupportingRow()
        }

    var errorText: CharSequence? = null
        set(value) {
            field = value
            renderSupportingRow()
        }

    var counterEnabled = false
        set(value) {
            field = value
            renderSupportingRow()
        }

    var maxLength = 0
        set(value) {
            require(value >= 0) { "maxLength must be non-negative" }
            field = value
            binding.etInput.apply {
                applyTextFieldLengthLimit(value, replaceExisting = true)
                if (value > 0 && length() > value) {
                    val start = selectionStart
                    val end = selectionEnd
                    setText(text)
                    restoreSelection(start, end)
                }
            }
            renderSupportingRow()
        }

    var inputType: Int
        get() = binding.etInput.inputType
        set(value) {
            val start = binding.etInput.selectionStart
            val end = binding.etInput.selectionEnd
            binding.etInput.inputType = value
            binding.etInput.setSingleLine(true)
            passwordVisible = false
            applyPasswordTransformation(start, end)
            renderEndIcon()
        }

    var imeOptions: Int
        get() = binding.etInput.imeOptions
        set(value) {
            binding.etInput.imeOptions = value
        }

    var startIcon: Drawable?
        get() = binding.ivStart.drawable
        set(value) {
            binding.ivStart.setImageDrawable(value)
            binding.ivStart.isVisible = value != null
        }

    var endIcon: Drawable? = null
        set(value) {
            field = value
            renderEndIcon()
        }

    var passwordToggleEnabled = false
        set(value) {
            field = value
            if (!value && passwordVisible) togglePassword()
            renderEndIcon()
        }

    private val binding = ViewCoreTextFieldBinding.inflate(this.context.inflater, this)
    private var labelAppearance = R.style.TextAppearance_Inter_Semibold_H3
    private var inputAppearance = R.style.TextAppearance_Inter_Regular_B1
    private var inputLineHeight = 0
    private var passwordVisible = false
    private var endIconDescription: CharSequence? = null
    private var endIconAction: (() -> Unit)? = null
    private var textChangedListener: ((String) -> Unit)? = null

    private val isPasswordInput: Boolean
        get() {
            val variation = inputType and (InputType.TYPE_MASK_CLASS or InputType.TYPE_MASK_VARIATION)
            return variation == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) ||
                variation == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) ||
                variation == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) ||
                variation == (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)
        }

    init {
        orientation = VERTICAL
        readAppearance(attrs, defStyleAttr)
        readContent(attrs, defStyleAttr)
        renderSupportingRow()

        binding.llInput.setOnClickListener {
            if (binding.etInput.requestFocus()) binding.etInput.showKeyboard()
        }
        binding.llInput.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        binding.etInput.setOnFocusChangeListener { _, focused -> binding.llInput.isSelected = focused }
        binding.etInput.doAfterTextChanged {
            renderSupportingRow()
            textChangedListener?.invoke(it?.toString().orEmpty())
        }
        binding.btnEnd.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateEndIconTouchTarget() }
        ViewCompat.setAccessibilityDelegate(binding.etInput, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.error = errorText?.takeUnless { it.isEmpty() }
                info.isContentInvalid = !errorText.isNullOrEmpty()
                if (maxLength > 0) info.maxTextLength = maxLength
            }
        })
    }

    fun setOnTextChangedListener(listener: ((String) -> Unit)?) {
        textChangedListener = listener
    }

    fun setOnEditorActionListener(listener: TextView.OnEditorActionListener?) {
        binding.etInput.setOnEditorActionListener(listener)
    }

    fun setEndIconAction(contentDescription: CharSequence?, action: (() -> Unit)?) {
        require(action == null || !contentDescription.isNullOrBlank()) {
            "An end icon action requires a content description"
        }
        endIconDescription = contentDescription
        endIconAction = action
        renderEndIcon()
    }

    private fun renderLabel() {
        binding.tvLabel.isVisible = !label.isNullOrEmpty()
        updateInputMargins()
        binding.tvLabel.text = if (label.isNullOrEmpty() || !isFieldRequired) {
            label
        } else {
            val font = context.obtainStyledAttributes(labelAppearance, intArrayOf(android.R.attr.fontFamily)).use {
                it.getResourceId(0, R.font.inter_semibold)
            }
            val textStyle = TextStyle(
                color = context.colorAttr(R.attr.coreTextFieldRequiredColor),
                font = font,
                textSize = binding.tvLabel.textSize,
                lineHeight = binding.tvLabel.lineHeight.toFloat()
            )
            buildSpannedString {
                append(label)
                append(' ')
                append(
                    buildHighlightedMessage(
                        context = context,
                        message = "*",
                        highlightedMessages = listOf("*"),
                        highlightedTextAppearance = listOf(textStyle)
                    )
                )
            }
        }
    }

    private fun updateInputMargins() {
        val nominalGap = if (binding.tvLabel.isVisible) context.dimen(R.dimen.xxs).roundToInt() else 0
        val fontHeight = binding.etInput.paint.getFontMetricsInt(null)
        // Keep the native editor's glyph bounds while fitting the recipe's line spacing.
        val extraHeight = (fontHeight - inputLineHeight).coerceAtLeast(0)
        binding.etInput.updateLayoutParams<LayoutParams> {
            topMargin = nominalGap - extraHeight / 2
            bottomMargin = -extraHeight + extraHeight / 2
        }
    }

    private fun renderSupportingRow() {
        val hasError = !errorText.isNullOrEmpty()
        val message = if (hasError) errorText else supportingText
        val showCounter = counterEnabled && maxLength > 0

        binding.llInput.isActivated = hasError
        binding.llSupporting.isVisible = !message.isNullOrEmpty() || showCounter
        binding.tvSupporting.isActivated = hasError
        binding.tvSupporting.accessibilityLiveRegion = if (hasError) {
            View.ACCESSIBILITY_LIVE_REGION_POLITE
        } else {
            View.ACCESSIBILITY_LIVE_REGION_NONE
        }
        binding.tvSupporting.text = message
        binding.tvCounter.isActivated = hasError
        binding.tvCounter.isVisible = showCounter
        if (showCounter) {
            binding.tvCounter.text = context.getString(R.string.core_text_field_counter, text.length, maxLength)
        }
    }

    private fun renderEndIcon() {
        val showToggle = passwordToggleEnabled && isPasswordInput
        val icon = if (showToggle) {
            val iconRes = if (passwordVisible) {
                com.google.android.material.R.drawable.design_ic_visibility_off
            } else {
                com.google.android.material.R.drawable.design_ic_visibility
            }
            context.getDrawable(iconRes)
        } else {
            endIcon
        }
        val hasAction = icon != null && (showToggle || endIconAction != null)
        binding.btnEnd.apply {
            setImageDrawable(icon)
            isVisible = icon != null
            contentDescription = when {
                showToggle && passwordVisible -> context.getString(R.string.core_text_field_hide_password)
                showToggle -> context.getString(R.string.core_text_field_show_password)
                else -> endIconDescription
            }
            setOnClickListener(
                when {
                    showToggle -> OnClickListener { togglePassword() }
                    hasAction -> OnClickListener { endIconAction?.invoke() }
                    else -> null
                }
            )
            isClickable = hasAction
            isFocusable = hasAction
            importantForAccessibility = if (hasAction) IMPORTANT_FOR_ACCESSIBILITY_YES else IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        updateEndIconTouchTarget()
    }

    private fun updateEndIconTouchTarget() {
        val icon = binding.btnEnd
        if (!icon.isVisible || !icon.isClickable || icon.width == 0) {
            binding.llInput.touchDelegate = null
            return
        }
        val bounds = Rect()
        icon.getHitRect(bounds)
        val padding = context.dimen(R.dimen.xs).toInt()
        bounds.inset(-padding, -padding)
        binding.llInput.touchDelegate = TouchDelegate(bounds, icon)
    }

    private fun togglePassword() {
        passwordVisible = !passwordVisible
        applyPasswordTransformation(binding.etInput.selectionStart, binding.etInput.selectionEnd)
        renderEndIcon()
    }

    private fun applyPasswordTransformation(start: Int, end: Int) {
        if (isPasswordInput) {
            binding.etInput.transformationMethod = if (passwordVisible) null else PasswordTransformationMethod.getInstance()
        }
        context.typefaceFromAppearance(inputAppearance)?.let { binding.etInput.typeface = it }
        restoreSelection(start, end)
        updateInputMargins()
    }

    private fun restoreSelection(start: Int, end: Int) {
        if (start >= 0 && end >= 0) {
            val length = binding.etInput.length()
            binding.etInput.setSelection(start.coerceAtMost(length), end.coerceAtMost(length))
        }
    }

    private fun updateEnabledState() {
        binding.llInput.isEnabled = isEnabled
        binding.etInput.isEnabled = isEnabled
        binding.btnEnd.isEnabled = isEnabled
        binding.tvSupporting.isEnabled = isEnabled
        binding.tvCounter.isEnabled = isEnabled
    }

    private fun readAppearance(attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(attrs, R.styleable.CoreTextField, defStyleAttr, R.style.Widget_EDTS_UIKit_CoreTextField) {
            labelAppearance = getResourceId(R.styleable.CoreTextField_coreTextFieldLabelAppearance, labelAppearance)
            inputAppearance = getResourceId(R.styleable.CoreTextField_coreTextFieldInputAppearance, inputAppearance)
            val supportingAppearance = getResourceId(
                R.styleable.CoreTextField_coreTextFieldSupportingAppearance,
                R.style.TextAppearance_Inter_Regular_B3
            )
            applyFieldTextAppearance(binding.tvLabel, labelAppearance)
            applyFieldTextAppearance(binding.etInput, inputAppearance)
            applyFieldTextAppearance(binding.tvSupporting, supportingAppearance)
            applyFieldTextAppearance(binding.tvCounter, supportingAppearance)

            binding.tvLabel.setTextColor(context.colorAttr(R.attr.coreTextFieldSupportingColor))
            binding.etInput.setTextColor(context.colorStateList(R.color.slr_core_text_field_text))
            binding.etInput.setHintTextColor(context.colorAttr(R.attr.coreTextFieldPlaceholder))
            val supportingColors = context.colorStateList(R.color.slr_core_text_field_supporting)
            binding.tvSupporting.setTextColor(supportingColors)
            binding.tvCounter.setTextColor(supportingColors)

            inputLineHeight = getDimensionPixelSize(
                R.styleable.CoreTextField_coreTextFieldInputLineHeight,
                context.dimen(R.dimen.dimen_18).roundToInt()
            ).scaledLineHeight()
            binding.etInput.minimumHeight = inputLineHeight
            val supportingLineHeight = getDimensionPixelSize(
                R.styleable.CoreTextField_coreTextFieldSupportingLineHeight,
                context.dimen(R.dimen.dimen_16).roundToInt()
            ).scaledLineHeight()
            binding.llSupporting.minimumHeight = supportingLineHeight
            TextViewCompat.setLineHeight(binding.tvSupporting, supportingLineHeight)
            TextViewCompat.setLineHeight(binding.tvCounter, supportingLineHeight)
        }
    }

    private fun readContent(attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(attrs, R.styleable.CoreTextField, defStyleAttr) {
            inputType = getInt(R.styleable.CoreTextField_android_inputType, InputType.TYPE_CLASS_TEXT)
            imeOptions = getInt(R.styleable.CoreTextField_android_imeOptions, EditorInfo.IME_ACTION_NEXT)
            maxLength = getInt(R.styleable.CoreTextField_fieldMaxLength, 0).coerceAtLeast(0)
            counterEnabled = getBoolean(R.styleable.CoreTextField_fieldCounterEnabled, false)
            isFieldRequired = getBoolean(R.styleable.CoreTextField_isFieldRequired, false)
            passwordToggleEnabled = getBoolean(R.styleable.CoreTextField_fieldPasswordToggleEnabled, false)
            label = getText(R.styleable.CoreTextField_fieldLabel)
            placeholder = getText(R.styleable.CoreTextField_android_hint)
            supportingText = getText(R.styleable.CoreTextField_fieldSupportingText)
            errorText = getText(R.styleable.CoreTextField_fieldError)
            startIcon = getDrawable(R.styleable.CoreTextField_fieldStartIcon)
            endIcon = getDrawable(R.styleable.CoreTextField_fieldEndIcon)
            text = getText(R.styleable.CoreTextField_android_text) ?: ""
            isEnabled = getBoolean(R.styleable.CoreTextField_android_enabled, isEnabled)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO
                getString(R.styleable.CoreTextField_android_autofillHints)?.let {
                    binding.etInput.setAutofillHints(*it.split(',').map(String::trim).toTypedArray())
                }
            }
        }
    }

    private fun applyFieldTextAppearance(view: TextView, appearance: Int) {
        TextViewCompat.setTextAppearance(view, appearance)
        context.typefaceFromAppearance(appearance)?.let { view.typeface = it }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        updateEnabledState()
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    override fun onSaveInstanceState(): Parcelable = Bundle().apply {
        putParcelable(STATE_SUPER, super.onSaveInstanceState())
        putCharSequence(STATE_TEXT, text)
        putCharSequence(STATE_ERROR, errorText)
        putBoolean(STATE_FOCUSED, binding.etInput.hasFocus())
        putInt(STATE_SELECTION_START, binding.etInput.selectionStart)
        putInt(STATE_SELECTION_END, binding.etInput.selectionEnd)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is Bundle) {
            super.onRestoreInstanceState(state)
            return
        }
        @Suppress("DEPRECATION")
        super.onRestoreInstanceState(state.getParcelable(STATE_SUPER))
        errorText = state.getCharSequence(STATE_ERROR)
        if (state.getBoolean(STATE_FOCUSED)) binding.etInput.requestFocus()
        binding.etInput.setText(state.getCharSequence(STATE_TEXT))
        restoreSelection(state.getInt(STATE_SELECTION_START), state.getInt(STATE_SELECTION_END))
    }

    private fun Int.scaledLineHeight(): Int = (this * resources.configuration.fontScale).roundToInt()

    private companion object {
        const val STATE_SUPER = "superState"
        const val STATE_TEXT = "text"
        const val STATE_ERROR = "error"
        const val STATE_FOCUSED = "focused"
        const val STATE_SELECTION_START = "selectionStart"
        const val STATE_SELECTION_END = "selectionEnd"
    }
}
