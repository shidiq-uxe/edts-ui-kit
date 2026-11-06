package id.co.edtslib.edtsuikit

import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import id.co.edtslib.uikit.R as UIKitR

internal enum class AlertBoxProduct { CORE, KLIK, POINKU }

internal data class AlertBoxExample(@StyleRes val recipe: Int, @StringRes val label: Int, val isFullWidth: Boolean = false)

internal object AlertBoxExamples {
    val core = listOf(AlertBoxExample(UIKitR.style.Widget_EDTS_UIKit_CoreAlertBox, R.string.guidelines_neutral))
    val klik = listOf(
        AlertBoxExample(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Klik_Border_General, R.string.alert_demo_general),
        AlertBoxExample(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Klik_Border_Info, R.string.alert_demo_info),
        AlertBoxExample(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Klik_Border_Success, R.string.alert_demo_success),
        AlertBoxExample(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Klik_Border_Error, R.string.alert_demo_error),
        AlertBoxExample(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Klik_Border_Warning, R.string.alert_demo_warning),
        AlertBoxExample(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Klik_FullWidth_General, R.string.alert_demo_full_width, true)
    )
    val poinku = listOf(
        AlertBoxExample(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Poinku_Success, R.string.alert_demo_success),
        AlertBoxExample(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Poinku_Warning, R.string.alert_demo_warning),
        AlertBoxExample(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Poinku_Error, R.string.alert_demo_error),
        AlertBoxExample(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Poinku_Info, R.string.alert_demo_info)
    )

    fun forProduct(product: AlertBoxProduct): List<AlertBoxExample> = when (product) {
        AlertBoxProduct.CORE -> core
        AlertBoxProduct.KLIK -> klik
        AlertBoxProduct.POINKU -> poinku
    }
}
