package id.co.edtslib.edtsuikit.tmh

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.R as UIKitR

/** Display data shared by the listing and both redemption entry points. */
internal data class RedemptionProduct(
    val id: String,
    val name: String,
    val imageRes: Int,
    val price: Long,
    val originalPrice: Long,
    val isInStock: Boolean,
    val packSize: Int = 1,
    val shouldLabelFree: Boolean = false,
) {

    fun toBundle(): Bundle = Bundle().apply {
        putString("id", id)
        putString("name", name)
        putInt("image", imageRes)
        putLong("price", price)
        putLong("original", originalPrice)
        putBoolean("stock", isInStock)
        putInt("pack", packSize)
        putBoolean("free", shouldLabelFree)
    }

    companion object {
        fun fromBundle(bundle: Bundle): RedemptionProduct = RedemptionProduct(
            id = requireNotNull(bundle.getString("id")),
            name = requireNotNull(bundle.getString("name")),
            imageRes = bundle.getInt("image"),
            price = bundle.getLong("price"),
            originalPrice = bundle.getLong("original"),
            isInStock = bundle.getBoolean("stock"),
            packSize = bundle.getInt("pack", 1),
            shouldLabelFree = bundle.getBoolean("free"),
        )
    }
}

internal fun TmhProduct.presentation(context: Context): RedemptionProduct = RedemptionProduct(
    id = id,
    name = context.getString(nameRes),
    imageRes = imageRes,
    price = context.getString(priceRes).rupiahAmount(),
    originalPrice = context.getString(originalPriceRes).rupiahAmount(),
    isInStock = isInStock,
)

/** Layouts keep their own selection, spacing, and disabled badge treatment. */
internal fun RedemptionProduct.bindContent(
    image: ImageView,
    name: TextView,
    price: TextView,
    originalPrice: TextView,
    discount: TextView,
    isUnavailable: Boolean,
    @ColorRes disabledDiscountBackground: Int = UIKitR.color.black_20,
) {
    val context = image.context
    image.setImageResource(imageRes)
    image.setUnavailable(isUnavailable)
    name.text = this.name
    price.text = if (shouldLabelFree && this.price == 0L) context.getString(R.string.fair_gift_free)
        else this.price.rupiahText()
    originalPrice.text = this.originalPrice.rupiahText()
    originalPrice.paintFlags = originalPriceFlags()
    discount.text = context.getString(R.string.tmh_discount_percentage, redemptionDiscountPercent(this.price, this.originalPrice))
    name.setTextColor(context.color(if (isUnavailable) UIKitR.color.black_40 else UIKitR.color.black_70))
    val priceColor = context.color(if (isUnavailable) UIKitR.color.black_40 else UIKitR.color.red_30)
    price.setTextColor(priceColor)
    discount.setTextColor(priceColor)
    discount.backgroundTintList = ColorStateList.valueOf(context.color(
        if (isUnavailable) disabledDiscountBackground else UIKitR.color.red_10
    ))
}
