package id.co.edtslib.uikit.alert

import android.content.Context
import android.util.AttributeSet
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.AlertType

object AlertBoxAttrsFactory {
    fun initAttrs(context: Context, attrs: AttributeSet?, alertBoxOptions: AlertBoxOptions) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AlertBox)
        val alertType = typedArray.getInt(R.styleable.AlertBox_alertType, 4)
        val alertMessage = typedArray.getString(R.styleable.AlertBox_text) ?: ""
        val alertTextAppearance = typedArray.getResourceId(R.styleable.AlertBox_textAppearance, R.style.TextAppearance_Inter_Regular_B2)

        alertBoxOptions.apply {
            this.alertType = AlertType.values()[alertType]
            this.text = alertMessage
            this.textAppearance = alertTextAppearance
        }

        typedArray.recycle()
    }
}