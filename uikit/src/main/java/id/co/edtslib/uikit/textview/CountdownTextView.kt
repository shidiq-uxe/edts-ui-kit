package id.co.edtslib.uikit.textview

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import com.google.android.material.textview.MaterialTextView
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.hours
import id.co.edtslib.uikit.utils.htmlToString
import id.co.edtslib.uikit.utils.inSeconds
import id.co.edtslib.uikit.utils.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
class CountdownTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    enum class FormatType {
        Clock, Text
    }

    private var startTimeInMillis: Long = 0L

    var intervalInMillis: Long = 0L
    var format: FormatType = FormatType.Clock

    var displayAsHtml: Boolean = true

    var remainingText: String? = null
        set(value) {
            field = value
            updateTextDisplay()
        }

    private var hourText: String? = null
    private var minuteText: String? = null
    private var secondText: String? = null

    var delegate: CountdownTextViewDelegate? = null

    private var countdownJob: Job? = null

    init {
        initializeAttributes(attrs)
    }

    private fun initializeAttributes(attrs: AttributeSet?) {
        attrs?.let {
            context.theme.obtainStyledAttributes(it, R.styleable.CountdownTextView, 0, 0).apply {
                try {
                    remainingText = getString(R.styleable.CountdownTextView_remainingText)
                    intervalInMillis = getInt(R.styleable.CountdownTextView_interval, 0).seconds
                    format = FormatType.values()[getInteger(R.styleable.CountdownTextView_format, 0)]
                    hourText = getString(R.styleable.CountdownTextView_hourText)
                    minuteText = getString(R.styleable.CountdownTextView_minuteText)
                    secondText = getString(R.styleable.CountdownTextView_secondText)
                } finally {
                    recycle()
                }
            }
        }
    }

    // Start the countdown
    fun start() {
        isVisible = true
        startTimeInMillis = System.currentTimeMillis()
        resetCountdown()
    }

    // Stop the countdown
    fun stop() {
        countdownJob?.cancel()
        countdownJob = null
    }

    // Reset and start countdown
    fun resetCountdown() {
        updateTextDisplay(intervalInMillis.inSeconds)  // Start with the full interval
        beginCountdown()
    }

    // Main countdown logic
    private fun beginCountdown() {
        stop()
        countdownJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val elapsedMillis = System.currentTimeMillis() - startTimeInMillis
                val remainingMillis = intervalInMillis - elapsedMillis
                val remainingSeconds = remainingMillis.inSeconds

                if (remainingSeconds > 0) {
                    updateTextDisplay(remainingSeconds)
                    delay(1.seconds)
                } else {
                    // When countdown finishes
                    expired()
                    break
                }
            }
        }
    }

    // Display expired state
    private fun expired() {
        isVisible = false
        delegate?.onExpired()
    }

    // Update the displayed text for the remaining time
    private fun updateTextDisplay(secondsRemaining: Long = intervalInMillis.inSeconds) {
        val formattedTime = formatRemainingTime(secondsRemaining)
        val displayedText = if (displayAsHtml) {
            // If HTML mode, format with HTML
            remainingText.toString().format(formattedTime).htmlToString()
        } else {
            // If it's a SpannedString or regular CharSequence, keep it
            val finalText = SpannableString(remainingText)
            replacePlaceholder(finalText, formattedTime)
            finalText
        }

        text = displayedText
    }

    private fun replacePlaceholder(text: SpannableString, replacement: String) {
        val placeholder = "%s"
        val start = text.indexOf(placeholder)
        if (start != -1) {
            text.replaceRange(start, start + placeholder.length, replacement)
        }
    }

    // Format time based on the chosen display format (Clock/Text)
    private fun formatRemainingTime(seconds: Long): String {
        val hours = seconds / 3600L                    // Total hours
        val minutes = (seconds % 1.hours) / 60L        // Remaining minutes after hours
        val secs = seconds % 60L                       // Remaining seconds after minutes

        return when (format) {
            FormatType.Text -> formatTextTime(hours, minutes, secs)
            FormatType.Clock -> formatClockTime(hours, minutes, secs)
        }
    }

    // Format time in a Global text style
    private fun formatTextTime(hours: Long, minutes: Long, seconds: Long): String {
        return buildString {
            if (hours > 0) appendTimeWithLabel(hours, hourText)
            if (minutes > 0 || hours > 0) appendTimeWithLabel(minutes, minuteText)
            appendTimeWithLabel(seconds, secondText)
        }.trim()
    }

    private fun StringBuilder.appendTimeWithLabel(value: Long, unit: String?) {
        if (value > 0) append("$value $unit ")
    }

    // Format time in HH:MM:SS clock style
    private fun formatClockTime(hours: Long, minutes: Long, seconds: Long): String {
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}