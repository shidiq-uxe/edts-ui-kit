package id.co.edtslib.uikit.alert

import androidx.annotation.IntegerRes
import androidx.annotation.StyleRes
import id.co.edtslib.uikit.R

data class AlertBoxOptions(
    var alertType: AlertBox.AlertType = AlertBox.AlertType.LIGHT,
    var text: CharSequence? = null,
    @StyleRes var textAppearance: Int = R.style.TextAppearance_Inter_Regular_B2,
)
