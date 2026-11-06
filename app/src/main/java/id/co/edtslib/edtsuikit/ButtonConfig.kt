package id.co.edtslib.edtsuikit

import android.os.Bundle

data class ButtonConfig(
    val destructive: Boolean = false,
    val disabled: Boolean = false,
    val shimmer: Boolean = false,
    val pressed: Boolean = false,
    val cornerRadius: Float? = null,
    val sizeFilter: SizeFilter = SizeFilter.ALL,
    val tintColor: Int? = null,
    val customColors: List<Int> = defaultColors,
) {
    internal fun examples(product: ButtonProduct): List<ButtonRecipeExample> {
        val examples = when (product) {
            ButtonProduct.CORE -> ButtonRecipeExamples.core
            ButtonProduct.KLIK -> ButtonRecipeExamples.klik
            ButtonProduct.POINKU -> ButtonRecipeExamples.poinku
        }
        return if (product == ButtonProduct.CORE || sizeFilter == SizeFilter.ALL) {
            examples
        } else {
            examples.filter { it.sizeFilter == sizeFilter }
        }
    }

    companion object {
        val defaultColors = listOf(
            0xFF1659AB.toInt(),
            0xFFFFAB00.toInt(),
            0xFFFF3030.toInt(),
            0xFF1A9F67.toInt(),
            0xFF18A4F2.toInt(),
            0xFFED608F.toInt(),
            0xFF191919.toInt(),
            0xFF878F99.toInt(),
        )
    }
}

enum class ButtonProduct { CORE, KLIK, POINKU }

enum class SizeFilter { SMALL, MEDIUM, LARGE, ALL }

private const val CONFIG_DESTRUCTIVE = "destructive"
private const val CONFIG_DISABLED = "disabled"
private const val CONFIG_SHIMMER = "shimmer"
private const val CONFIG_PRESSED = "pressed"
private const val CONFIG_CORNER_RADIUS = "corner_radius"
private const val CONFIG_SIZE_FILTER = "size_filter"
private const val CONFIG_HAS_TINT = "has_tint"
private const val CONFIG_TINT = "tint"
private const val CONFIG_COLORS = "colors"

fun ButtonConfig.toBundle(): Bundle = Bundle().apply {
    putBoolean(CONFIG_DESTRUCTIVE, destructive)
    putBoolean(CONFIG_DISABLED, disabled)
    putBoolean(CONFIG_SHIMMER, shimmer)
    putBoolean(CONFIG_PRESSED, pressed)
    cornerRadius?.let { putFloat(CONFIG_CORNER_RADIUS, it) }
    putString(CONFIG_SIZE_FILTER, sizeFilter.name)
    putBoolean(CONFIG_HAS_TINT, tintColor != null)
    tintColor?.let { putInt(CONFIG_TINT, it) }
    putIntegerArrayList(CONFIG_COLORS, ArrayList(customColors))
}

fun Bundle.toButtonConfig(): ButtonConfig = ButtonConfig(
    destructive = getBoolean(CONFIG_DESTRUCTIVE),
    disabled = getBoolean(CONFIG_DISABLED),
    shimmer = getBoolean(CONFIG_SHIMMER),
    pressed = getBoolean(CONFIG_PRESSED),
    cornerRadius = if (containsKey(CONFIG_CORNER_RADIUS)) getFloat(CONFIG_CORNER_RADIUS) else null,
    sizeFilter = getString(CONFIG_SIZE_FILTER)
        ?.let { runCatching { SizeFilter.valueOf(it) }.getOrNull() }
        ?: SizeFilter.ALL,
    tintColor = if (getBoolean(CONFIG_HAS_TINT)) getInt(CONFIG_TINT) else null,
    customColors = getIntegerArrayList(CONFIG_COLORS)?.toList() ?: ButtonConfig.defaultColors,
)
