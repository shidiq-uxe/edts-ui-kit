package id.co.edtslib.edtsuikit.tmh

import android.content.Context
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

internal fun TmhProduct.discountPercent(context: Context): Int = redemptionDiscountPercent(
    price = context.getString(priceRes).rupiahAmount(),
    originalPrice = context.getString(originalPriceRes).rupiahAmount(),
)

internal fun redemptionDiscountPercent(price: Long, originalPrice: Long): Int {
    if (originalPrice <= 0L) return 0
    return ((originalPrice - price) * 100.0 / originalPrice).roundToInt().coerceIn(0, 100)
}

/** Parses only the app's literal rupiah fixtures, never a backend monetary value. */
internal fun String.rupiahAmount(): Long = filter(Char::isDigit).toLong()

internal fun Long.rupiahText(): String = "Rp${NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID")).format(this)}"
