package id.co.edtslib.edtsuikit

import android.os.Bundle
import id.co.edtslib.uikit.recipes.textbadge.klik.KlikVariant
import id.co.edtslib.uikit.recipes.textbadge.poinku.PoinkuVariant

data class TextBadgeConfig(
    val product: TextBadgeProduct = TextBadgeProduct.CORE,
    val variantIndex: Int = 0,
    val sizeIndex: Int = 0,
    val text: String = "Text Badge",
    val iconVisible: Boolean = true,
    val disabled: Boolean = false,
    val borderWidth: Int = 0,
    val cornerRadius: Int = 4,
    val tintColor: Int? = null,
    val customColors: List<Int> = defaultColors,
) {
    fun sizeLabels(): List<String> = when (product) {
        TextBadgeProduct.KLIK -> when (variantIndex) {
            1 -> listOf("Small", "Big")
            else -> listOf("Default")
        }
        TextBadgeProduct.POINKU -> when (variantIndex) {
            0 -> listOf("Full")
            1, 2 -> listOf("Full", "Lite")
            else -> listOf("Default")
        }
        TextBadgeProduct.CORE -> listOf("Medium")
    }

    fun klikVariant(): KlikVariant = when (variantIndex) {
        0 -> KlikVariant.Promo.DEFAULT
        1 -> if (sizeIndex == 1) KlikVariant.Fair.BIG else KlikVariant.Fair.SMALL
        2 -> KlikVariant.Highlight.DEFAULT
        else -> KlikVariant.Quota.DEFAULT
    }

    fun poinkuVariant(): PoinkuVariant = when (variantIndex) {
        0 -> PoinkuVariant.Loyalty.Full
        1 -> if (sizeIndex == 1) PoinkuVariant.Coupon.Lite else PoinkuVariant.Coupon.Full
        2 -> if (sizeIndex == 1) PoinkuVariant.Currency.Lite else PoinkuVariant.Currency.Full
        else -> PoinkuVariant.Freeform
    }

    companion object {
        val defaultColors = listOf(
            0xFF1659AB.toInt(),
            0xFFFFAB00.toInt(),
            0xFFFF3030.toInt(),
            0xFF1A9F67.toInt(),
            0xFF18A4F2.toInt(),
            0xFFED608F.toInt(),
        )
    }
}

enum class TextBadgeProduct(val label: String, val variantLabels: List<String>) {
    CORE("Core", listOf("Default")),
    KLIK("Klik", listOf("Promo", "Fair", "Highlight", "Quota")),
    POINKU("Poinku", listOf("Loyalty", "Coupon", "Currency", "Freeform")),
}

private const val CONFIG_PRODUCT = "product"
private const val CONFIG_VARIANT_INDEX = "variant_index"
private const val CONFIG_SIZE_INDEX = "size_index"
private const val CONFIG_TEXT = "text"
private const val CONFIG_ICON_VISIBLE = "icon_visible"
private const val CONFIG_DISABLED = "disabled"
private const val CONFIG_BORDER_WIDTH = "border_width"
private const val CONFIG_CORNER_RADIUS = "corner_radius"
private const val CONFIG_HAS_TINT = "has_tint"
private const val CONFIG_TINT = "tint"
private const val CONFIG_COLORS = "colors"

fun TextBadgeConfig.toBundle(): Bundle = Bundle().apply {
    putString(CONFIG_PRODUCT, product.name)
    putInt(CONFIG_VARIANT_INDEX, variantIndex)
    putInt(CONFIG_SIZE_INDEX, sizeIndex)
    putString(CONFIG_TEXT, text)
    putBoolean(CONFIG_ICON_VISIBLE, iconVisible)
    putBoolean(CONFIG_DISABLED, disabled)
    putInt(CONFIG_BORDER_WIDTH, borderWidth)
    putInt(CONFIG_CORNER_RADIUS, cornerRadius)
    putBoolean(CONFIG_HAS_TINT, tintColor != null)
    tintColor?.let { putInt(CONFIG_TINT, it) }
    putIntegerArrayList(CONFIG_COLORS, ArrayList(customColors))
}

fun Bundle.toTextBadgeConfig(): TextBadgeConfig = TextBadgeConfig(
    product = getString(CONFIG_PRODUCT)
        ?.let { runCatching { TextBadgeProduct.valueOf(it) }.getOrNull() }
        ?: TextBadgeProduct.CORE,
    variantIndex = getInt(CONFIG_VARIANT_INDEX, 0),
    sizeIndex = getInt(CONFIG_SIZE_INDEX, 0),
    text = getString(CONFIG_TEXT).orEmpty(),
    iconVisible = getBoolean(CONFIG_ICON_VISIBLE, true),
    disabled = getBoolean(CONFIG_DISABLED),
    borderWidth = getInt(CONFIG_BORDER_WIDTH, 0),
    cornerRadius = getInt(CONFIG_CORNER_RADIUS, 4),
    tintColor = if (getBoolean(CONFIG_HAS_TINT)) getInt(CONFIG_TINT) else null,
    customColors = getIntegerArrayList(CONFIG_COLORS)?.toList() ?: TextBadgeConfig.defaultColors,
)