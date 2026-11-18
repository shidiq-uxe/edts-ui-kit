package id.co.edtslib.uikit.core.otp

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.InputFilter
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.withStyledAttributes
import androidx.core.widget.TextViewCompat
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.utils.hapticfeedback.HapticFeedback
import id.co.edtslib.uikit.utils.inflater
import id.co.edtslib.uikit.utils.typefaceFromAppearance
import id.co.edtslib.uikit.utils.vibrateAnimation
import id.co.edtslib.uikit.utils.vibratePhone

/** A segmented numeric editor. Count and spacing can be set in XML. */
open class CoreOtpGroup protected constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    @StyleRes themeOverlay: Int
) : LinearLayoutCompat(ContextThemeWrapper(context, themeOverlay), attrs, defStyleAttr) {

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.coreOtpGroupStyle
    ) : this(context, attrs, defStyleAttr, R.style.ThemeOverlay_EDTS_UIKit_CoreOtpGroup)

    var otp: String
        get() = inputController.value
        set(value) {
            inputController.setValue(value, notify = true)
        }

    var shouldAnimateError = true

    var shouldVibrate = true

    var isError = false
        set(value) {
            field = value
            editors.forEach { it.isActivated = value }
            if (value && isEnabled && isLaidOut) {
                if (shouldAnimateError) vibrateAnimation()
                if (shouldVibrate) context.vibratePhone(HapticFeedback.ERROR)
            }
        }

    private val editors = mutableListOf<OtpEditText>()
    private val inputController: OtpInputController
    private var onCompleteListener: ((String) -> Unit)? = null
    private var onChangeListener: ((String) -> Unit)? = null

    init {
        var initialError = false
        orientation = HORIZONTAL
        this.context.withStyledAttributes(attrs, R.styleable.CoreOtpGroup, defStyleAttr) {
            val count = getInt(R.styleable.CoreOtpGroup_otpCount, 4).coerceAtLeast(1)
            val spacing = getDimensionPixelSize(
                R.styleable.CoreOtpGroup_marginBetween,
                this@CoreOtpGroup.context.dimenPixelSize(R.dimen.xxs) ?: 0
            )
            val appearance = getResourceId(
                R.styleable.CoreOtpGroup_coreOtpDigitAppearance,
                R.style.TextAppearance_Inter_Semibold_D1
            )
            initialError = getBoolean(R.styleable.CoreOtpGroup_isError, false)
            shouldAnimateError = getBoolean(R.styleable.CoreOtpGroup_animateError, true)
            shouldVibrate = getBoolean(R.styleable.CoreOtpGroup_vibrateOnError, true)
            createEditors(count, spacing, appearance)
            isEnabled = getBoolean(R.styleable.CoreOtpGroup_android_enabled, isEnabled)
        }
        inputController = OtpInputController(
            editors = editors,
            notifyForEachChange = false,
            onComplete = { onCompleteListener?.invoke(it) },
            onChange = { onChangeListener?.invoke(it) }
        )
        isError = initialError
    }

    fun setOnOtpCompleteListener(listener: ((String) -> Unit)?) {
        onCompleteListener = listener
    }

    fun setOnTextChangedListener(listener: ((String) -> Unit)?) {
        onChangeListener = listener
    }

    fun clear() {
        inputController.clear()
    }

    private fun createEditors(count: Int, spacing: Int, @StyleRes textAppearance: Int) {
        val typeface = context.typefaceFromAppearance(textAppearance)
        val textColors = context.colorStateList(R.color.slr_core_otp_text)
        repeat(count) { index ->
            val editor = (context.inflater.inflate(R.layout.view_core_otp_cell, this, false) as OtpEditText).apply {
                // The group owns saved state and focus restoration across repeated cell IDs.
                id = View.generateViewId()
                TextViewCompat.setTextAppearance(this, textAppearance)
                setTextColor(textColors)
                setHorizontallyScrolling(false)
                if (typeface != null) this.typeface = typeface
                contentDescription = context.getString(R.string.core_otp_digit_description, index + 1, count)
                filters = arrayOf(InputFilter.LengthFilter(count - index))
                (layoutParams as LayoutParams).marginStart = if (index == 0) 0 else spacing
            }
            editors.add(editor)
            addView(editor)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        for (index in 0 until childCount) getChildAt(index).isEnabled = enabled
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    override fun onSaveInstanceState(): Parcelable = Bundle().apply {
        putParcelable(STATE_SUPER, super.onSaveInstanceState())
        putString(STATE_OTP, otp)
        putBoolean(STATE_ERROR, isError)
        putInt(STATE_FOCUS, editors.indexOfFirst { it.hasFocus() })
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is Bundle) {
            super.onRestoreInstanceState(state)
            return
        }
        @Suppress("DEPRECATION")
        super.onRestoreInstanceState(state.getParcelable(STATE_SUPER))
        inputController.setValue(state.getString(STATE_OTP).orEmpty(), notify = false)
        isError = state.getBoolean(STATE_ERROR)
        editors.getOrNull(state.getInt(STATE_FOCUS, -1))?.requestFocus()
    }

    private companion object {
        const val STATE_SUPER = "superState"
        const val STATE_OTP = "otp"
        const val STATE_ERROR = "error"
        const val STATE_FOCUS = "focus"
    }
}
